package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class StarEventService {
    private static final int ANNOUNCE_NEARBY_BUFFER = 32;

    private final RewardRoller rewardRoller = new RewardRoller();
    private final Map<UUID, ActiveStarDrop> activeDrops = new HashMap<>();

    public void onServerTick(MinecraftServer server) {
        if (activeDrops.isEmpty()) {
            return;
        }

        long tick = server.getTickCount();
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, ActiveStarDrop> entry : activeDrops.entrySet()) {
            UUID entityId = entry.getKey();
            ActiveStarDrop activeDrop = entry.getValue();
            Entity entity = resolveTrackedEntity(server, activeDrop, entityId);

            if (entity == null || !entity.isAlive()) {
                toRemove.add(entityId);
                continue;
            }

            if (tick >= activeDrop.expireTick()) {
                entity.discard();
                toRemove.add(entityId);
            }
        }

        for (UUID entityId : toRemove) {
            activeDrops.remove(entityId);
        }
    }

    public int runCycle(MinecraftServer server, MainConfig config) {
        int maxStars = Math.max(1, config.scheduler.maxStarsPerCycle);
        return runCycle(server, config, maxStars, false);
    }

    public int getActiveDropCount() {
        return activeDrops.size();
    }

    public int cleanupActiveDrops(MinecraftServer server) {
        if (activeDrops.isEmpty()) {
            return 0;
        }

        int removed = 0;
        List<UUID> trackedIds = new ArrayList<>(activeDrops.keySet());
        for (UUID entityId : trackedIds) {
            ActiveStarDrop activeDrop = activeDrops.get(entityId);
            if (activeDrop == null) {
                continue;
            }

            Entity entity = resolveTrackedEntity(server, activeDrop, entityId);
            if (entity != null && entity.isAlive()) {
                entity.discard();
            }
            removed++;
            activeDrops.remove(entityId);
        }

        return removed;
    }

    public int runCycle(MinecraftServer server, MainConfig config, int maxStars, boolean bypassActivationChecks) {
        int cappedMaxStars = Math.max(1, maxStars);
        int maxActiveDrops = Math.max(1, config.claim.maxActiveDrops);
        if (activeDrops.size() >= maxActiveDrops) {
            return 0;
        }

        List<ServerPlayer> eligiblePlayers = collectEligiblePlayers(server, config, bypassActivationChecks);
        if (eligiblePlayers.isEmpty()) {
            return 0;
        }

        Collections.shuffle(eligiblePlayers, ThreadLocalRandom.current());

        int spawned = 0;

        for (ServerPlayer player : eligiblePlayers) {
            if (spawned >= cappedMaxStars) {
                break;
            }

            if (activeDrops.size() >= maxActiveDrops) {
                break;
            }

            if (spawnStarNearPlayer(player, config)) {
                spawned++;
            }

            if (isGlobalScope(config)) {
                break;
            }
        }

        return spawned;
    }

    private List<ServerPlayer> collectEligiblePlayers(MinecraftServer server, MainConfig config, boolean bypassActivationChecks) {
        List<ServerPlayer> eligible = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isPlayerEligible(player, config, bypassActivationChecks)) {
                eligible.add(player);
            }
        }

        return eligible;
    }

    private boolean isPlayerEligible(ServerPlayer player, MainConfig config, boolean bypassActivationChecks) {
        ServerLevel level = player.serverLevel();

        if (bypassActivationChecks) {
            return true;
        }

        if (config.activation.requireNight && !level.isNight()) {
            return false;
        }

        if (!isWeatherEligible(level, config.activation.weatherMode)) {
            return false;
        }

        if (config.activation.requireSurfaceAccess && !level.canSeeSky(player.blockPosition())) {
            return false;
        }

        return true;
    }

    private boolean spawnStarNearPlayer(ServerPlayer player, MainConfig config) {
        ServerLevel level = player.serverLevel();
        int maxAttempts = Math.max(1, config.spawn.maxLocationAttempts);

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            BlockPos spawnPos = pickSpawnPosition(player, config);
            if (spawnPos == null) {
                continue;
            }

            RolledReward rolledReward = rewardRoller.roll(config).orElse(null);
            if (rolledReward == null) {
                return false;
            }

            Item rewardItem = resolveItem(rolledReward.itemId());
            if (rewardItem == null) {
                continue;
            }

            ItemStack stack = new ItemStack(rewardItem, rolledReward.count());
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    spawnPos.getX() + 0.5,
                    spawnPos.getY() + 0.15,
                    spawnPos.getZ() + 0.5,
                    stack
            );
            itemEntity.setPickUpDelay(Math.max(0, config.claim.pickupDelayTicks));
            level.addFreshEntity(itemEntity);
            int lifeTicks = Math.max(1, config.claim.lifeTicks);
            activeDrops.put(itemEntity.getUUID(), new ActiveStarDrop(level.dimension(), level.getServer().getTickCount() + lifeTicks));
            announceSpawn(player, config);
            return true;
        }

        return false;
    }

    private BlockPos pickSpawnPosition(ServerPlayer player, MainConfig config) {
        ServerLevel level = player.serverLevel();

        int minRadius = Math.max(0, config.spawn.minRadius);
        int maxRadius = Math.max(minRadius, config.spawn.maxRadius);

        double angle = ThreadLocalRandom.current().nextDouble(0.0D, Math.PI * 2.0D);
        double radius = minRadius + ThreadLocalRandom.current().nextDouble((maxRadius - minRadius) + 1.0D);

        int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
        int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        BlockPos spawnPos = new BlockPos(x, y, z);
        BlockPos groundPos = spawnPos.below();

        if (!level.getWorldBorder().isWithinBounds(spawnPos)) {
            return null;
        }

        if (spawnPos.getY() <= level.getMinBuildHeight()) {
            return null;
        }

        if (!config.spawn.allowWaterSpawns && !level.getFluidState(spawnPos).isEmpty()) {
            return null;
        }

        if (!level.getBlockState(spawnPos).canBeReplaced()) {
            return null;
        }

        if (!level.getBlockState(groundPos).blocksMotion()) {
            return null;
        }

        return spawnPos;
    }

    private boolean isWeatherEligible(ServerLevel level, String weatherMode) {
        String mode = weatherMode == null ? "any" : weatherMode.toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "clear" -> !level.isRaining();
            case "rain" -> level.isRaining() && !level.isThundering();
            case "thunder" -> level.isThundering();
            default -> true;
        };
    }

    private boolean isGlobalScope(MainConfig config) {
        return "global".equalsIgnoreCase(config.spawn.targetScope);
    }

    private void announceSpawn(ServerPlayer sourcePlayer, MainConfig config) {
        if (!config.announcements.enabled || config.announcements.spawnMessage == null
                || config.announcements.spawnMessage.isBlank()) {
            return;
        }

        MinecraftServer server = sourcePlayer.getServer();
        if (server == null) {
            return;
        }

        Component message = Component.literal(config.announcements.spawnMessage);
        if ("global".equalsIgnoreCase(config.announcements.scope)) {
            server.getPlayerList().broadcastSystemMessage(message, false);
            return;
        }

        double maxDistance = Math.max(16, config.spawn.maxRadius + ANNOUNCE_NEARBY_BUFFER);
        double maxDistanceSq = maxDistance * maxDistance;
        for (ServerPlayer viewer : sourcePlayer.serverLevel().players()) {
            if (viewer.distanceToSqr(sourcePlayer) <= maxDistanceSq) {
                viewer.sendSystemMessage(message);
            }
        }
    }

    private Item resolveItem(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null) {
            return null;
        }

        return BuiltInRegistries.ITEM.getOptional(resourceLocation).orElse(null);
    }

    private Entity resolveTrackedEntity(MinecraftServer server, ActiveStarDrop activeDrop, UUID entityId) {
        ServerLevel level = server.getLevel(activeDrop.dimension());
        if (level != null) {
            return level.getEntity(entityId);
        }

        for (ServerLevel fallback : server.getAllLevels()) {
            Entity entity = fallback.getEntity(entityId);
            if (entity != null) {
                return entity;
            }
        }

        return null;
    }

    private record ActiveStarDrop(ResourceKey<Level> dimension, long expireTick) {
    }
}


