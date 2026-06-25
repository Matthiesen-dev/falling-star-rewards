package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.AnnouncementsConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.SchedulePresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.interfaces.ActiveStarDrop;
import dev.matthiesen.falling_star_rewards.common.interfaces.LoadedPreset;
import dev.matthiesen.falling_star_rewards.common.interfaces.RolledReward;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.TagParser;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class StarEventService {
    private static final int ANNOUNCE_NEARBY_BUFFER = 32;

    private final RewardRoller rewardRoller = new RewardRoller();
    private final Map<UUID, ActiveStarDrop> activeDrops = new HashMap<>();

    public void onServerTick(MinecraftServer server, LoadedPreset config) {
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
                continue;
            }

            if (entity.level() instanceof ServerLevel level) {
                emitFallingStarTrail(level, entity, activeDrop, tick, config.visualsConfig);
            }
        }

        for (UUID entityId : toRemove) {
            activeDrops.remove(entityId);
        }
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

    public int runCycle(
            MinecraftServer server,
            MainConfig mainConfig,
            LoadedPreset presetConfig,
            AnnouncementsConfig announcementsConfig,
            boolean bypassActivationChecks,
            SchedulePresetConfig scheduleConfig
    ) {
        int cappedMaxStars = scheduleConfig == null ? 1 : Math.max(1, scheduleConfig.maxStarsPerCycle);
        int maxActiveDrops = Math.max(1, mainConfig.claim.maxActiveDrops);
        if (activeDrops.size() >= maxActiveDrops) {
            return 0;
        }

        List<ServerPlayer> eligiblePlayers = collectEligiblePlayers(server, scheduleConfig, bypassActivationChecks);
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

            if (spawnStarNearPlayer(player, mainConfig, presetConfig, announcementsConfig)) {
                spawned++;
            }

            if (isGlobalScope(presetConfig)) {
                break;
            }
        }

        return spawned;
    }

    private List<ServerPlayer> collectEligiblePlayers(MinecraftServer server, SchedulePresetConfig scheduleConfig, boolean bypassActivationChecks) {
        List<ServerPlayer> eligible = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isPlayerEligible(player, scheduleConfig, bypassActivationChecks)) {
                eligible.add(player);
            }
        }

        return eligible;
    }

    private boolean isPlayerEligible(ServerPlayer player, SchedulePresetConfig scheduleConfig, boolean bypassActivationChecks) {
        ServerLevel level = player.serverLevel();

        if (bypassActivationChecks || scheduleConfig == null) {
            return true;
        }

        SchedulePresetConfig.Conditions conditions = scheduleConfig.conditions == null
                ? new SchedulePresetConfig.Conditions()
                : scheduleConfig.conditions;

        if (!isTimeEligible(level, conditions.timeMode)) {
            return false;
        }

        if (!isWeatherEligible(level, conditions.weatherMode)) {
            return false;
        }

        if (!isMoonPhaseEligible(level, conditions.moonPhases)) {
            return false;
        }

        return !conditions.requireSurfaceAccess || level.canSeeSky(player.blockPosition());
    }

    private boolean spawnStarNearPlayer(ServerPlayer player, MainConfig mainConfig, LoadedPreset presetConfig, AnnouncementsConfig announcementsConfig) {
        ServerLevel level = player.serverLevel();
        int maxAttempts = Math.max(1, presetConfig.eventConfig.spawn.maxLocationAttempts);

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            BlockPos spawnPos = pickSpawnPosition(player, presetConfig);
            if (spawnPos == null) {
                continue;
            }

            RolledReward rolledReward = rewardRoller.roll(presetConfig.rewardsConfig).orElse(null);
            if (rolledReward == null) {
                return false;
            }

            Item rewardItem = resolveItem(rolledReward.itemId());
            if (rewardItem == null) {
                continue;
            }

            ItemStack stack = new ItemStack(rewardItem, rolledReward.count());
            if (!applyRewardCustomization(stack, rolledReward)) {
                continue;
            }
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    spawnPos.getX() + 0.5,
                    spawnPos.getY() + 0.15,
                    spawnPos.getZ() + 0.5,
                    stack
            );
            itemEntity.setPickUpDelay(Math.max(0, mainConfig.claim.pickupDelayTicks));
            level.addFreshEntity(itemEntity);
            int lifeTicks = Math.max(1, mainConfig.claim.lifeTicks);
            long startTick = level.getServer().getTickCount();
            activeDrops.put(
                    itemEntity.getUUID(),
                    new ActiveStarDrop(level.dimension(), startTick, startTick + lifeTicks)
            );
            emitImpactBurst(level, spawnPos, presetConfig.visualsConfig);
            emitImpactSound(level, spawnPos, presetConfig.visualsConfig);
            announceSpawn(player, presetConfig, announcementsConfig);
            return true;
        }

        return false;
    }

    private BlockPos pickSpawnPosition(ServerPlayer player, LoadedPreset config) {
        ServerLevel level = player.serverLevel();

        int minRadius = Math.max(0, config.eventConfig.spawn.minRadius);
        int maxRadius = Math.max(minRadius, config.eventConfig.spawn.maxRadius);

        double angle = ThreadLocalRandom.current().nextDouble(0.0D, Math.PI * 2.0D);
        double radius = minRadius + ThreadLocalRandom.current().nextDouble((maxRadius - minRadius) + 1.0D);

        int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
        int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        BlockPos spawnPos = new BlockPos(x, y, z);

        if (!level.getWorldBorder().isWithinBounds(spawnPos)) {
            return null;
        }

        if (spawnPos.getY() <= level.getMinBuildHeight()) {
            return null;
        }

        if (!config.eventConfig.spawn.allowWaterSpawns && !level.getFluidState(spawnPos).isEmpty()) {
            return null;
        }

        if (!level.getBlockState(spawnPos).canBeReplaced()) {
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

    private boolean isTimeEligible(ServerLevel level, String timeMode) {
        String mode = timeMode == null ? "any" : timeMode.toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "day" -> level.isDay();
            case "night" -> level.isNight();
            default -> true;
        };
    }

    private boolean isMoonPhaseEligible(ServerLevel level, List<String> configuredPhases) {
        if (configuredPhases == null || configuredPhases.isEmpty()) {
            return true;
        }

        int currentMoonPhase = level.getMoonPhase();
        Set<Integer> acceptedPhases = configuredPhases.stream()
                .map(this::parseMoonPhase)
                .filter(phase -> phase >= 0)
                .collect(java.util.stream.Collectors.toSet());

        if (acceptedPhases.isEmpty()) {
            return true;
        }

        return acceptedPhases.contains(currentMoonPhase);
    }

    private int parseMoonPhase(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            int parsed = Integer.parseInt(normalized);
            return parsed >= 0 && parsed <= 7 ? parsed : -1;
        } catch (NumberFormatException ignored) {
            // Fall through to named parsing.
        }

        return switch (normalized) {
            case "full", "full_moon" -> 0;
            case "waning_gibbous" -> 1;
            case "third_quarter", "last_quarter" -> 2;
            case "waning_crescent" -> 3;
            case "new", "new_moon" -> 4;
            case "waxing_crescent" -> 5;
            case "first_quarter" -> 6;
            case "waxing_gibbous" -> 7;
            default -> -1;
        };
    }

    private boolean isGlobalScope(LoadedPreset config) {
        return "global".equalsIgnoreCase(config.eventConfig.spawn.targetScope);
    }

    private void announceSpawn(ServerPlayer sourcePlayer, LoadedPreset config, AnnouncementsConfig announcementsConfig) {
        if (!announcementsConfig.enabled || announcementsConfig.spawnMessage == null
                || announcementsConfig.spawnMessage.isBlank()) {
            return;
        }

        var useActionBarOverlay = announcementsConfig.useActionBar;

        MinecraftServer server = sourcePlayer.getServer();
        if (server == null) {
            return;
        }

        Component message = Component.literal(announcementsConfig.spawnMessage).withStyle(ChatFormatting.AQUA);
        if ("global".equalsIgnoreCase(announcementsConfig.scope)) {
            server.getPlayerList().broadcastSystemMessage(message, useActionBarOverlay);
            return;
        }

        double maxDistance = Math.max(16, config.eventConfig.spawn.maxRadius + ANNOUNCE_NEARBY_BUFFER);
        double maxDistanceSq = maxDistance * maxDistance;
        for (ServerPlayer viewer : sourcePlayer.serverLevel().players()) {
            if (viewer.distanceToSqr(sourcePlayer) <= maxDistanceSq) {
                viewer.sendSystemMessage(message, useActionBarOverlay);
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

    private boolean applyRewardCustomization(ItemStack stack, RolledReward reward) {
        Integer customModelData = reward.customModelData();
        if (customModelData != null) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(Math.max(0, customModelData)));
        }

        String customDataSnbt = reward.customData();
        if (customDataSnbt == null || customDataSnbt.isBlank()) {
            return true;
        }

        try {
            CompoundTag customDataTag = TagParser.parseTag(customDataSnbt);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, customDataTag);
            return true;
        } catch (CommandSyntaxException syntaxException) {
            FallingStarRewards.INSTANCE.createErrorLog(
                    "Failed to parse reward customData SNBT for " + reward.itemId() + ": "
                            + syntaxException.getMessage(), syntaxException
            );
            return false;
        }
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

    private void emitFallingStarTrail(ServerLevel level, Entity entity, ActiveStarDrop activeDrop, long tick, VisualsPresetConfig visualsPresetConfig) {
        if (!visualsPresetConfig.enabled) {
            return;
        }

        int interval = Math.max(1, visualsPresetConfig.emissionIntervalTicks);
        if ((tick % interval) != 0) {
            return;
        }

        int fallDistance = Math.max(2, visualsPresetConfig.fallDistance);
        long elapsed = Math.max(0L, tick - activeDrop.startTick());
        double phase = (elapsed % fallDistance) / (double) fallDistance;
        double y = entity.getY() + 0.5D + (1.0D - phase) * fallDistance;

        ParticleOptions particle = resolveParticlePreset(visualsPresetConfig.particlePreset);
        int particleCount = Math.max(1, visualsPresetConfig.particlesPerEmission);
        level.sendParticles(particle, entity.getX(), y, entity.getZ(), particleCount, 0.2D, 0.05D, 0.2D, 0.0D);
        emitTravelSound(level, entity.getX(), y, entity.getZ(), tick, visualsPresetConfig);
    }

    private void emitImpactBurst(ServerLevel level, BlockPos spawnPos, VisualsPresetConfig visualsPresetConfig) {
        if (!visualsPresetConfig.enabled || !visualsPresetConfig.impact.burstEnabled) {
            return;
        }

        ParticleOptions particle = resolveParticlePreset(visualsPresetConfig.impact.particlePreset);
        int count = Math.max(1, visualsPresetConfig.impact.particleCount);
        double spread = Math.max(0.0D, visualsPresetConfig.impact.spread);

        level.sendParticles(
                particle,
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 0.2D,
                spawnPos.getZ() + 0.5D,
                count,
                spread,
                spread * 0.4D,
                spread,
                0.01D
        );
    }

    private void emitImpactSound(ServerLevel level, BlockPos spawnPos, VisualsPresetConfig visualsPresetConfig) {
        if (!visualsPresetConfig.enabled || !visualsPresetConfig.impact.soundEnabled) {
            return;
        }

        SoundEvent soundEvent = resolveSoundEvent(visualsPresetConfig.impact.soundId);
        if (soundEvent == null) {
            return;
        }

        float volume = Math.max(0.0F, visualsPresetConfig.impact.soundVolume);
        float minPitch = Math.max(0.1F, visualsPresetConfig.impact.soundPitchMin);
        float maxPitch = Math.max(minPitch, visualsPresetConfig.impact.soundPitchMax);
        float pitch = minPitch;
        if (maxPitch > minPitch) {
            pitch = minPitch + ThreadLocalRandom.current().nextFloat() * (maxPitch - minPitch);
        }

        level.playSound(
                null,
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 0.2D,
                spawnPos.getZ() + 0.5D,
                soundEvent,
                SoundSource.AMBIENT,
                volume,
                pitch
        );
    }

    private void emitTravelSound(ServerLevel level, double x, double y, double z, long tick, VisualsPresetConfig config) {
        if (!config.enabled || !config.travelSound.enabled) {
            return;
        }

        int interval = Math.max(1, config.travelSound.intervalTicks);
        if ((tick % interval) != 0) {
            return;
        }

        SoundEvent soundEvent = resolveSoundEvent(config.travelSound.id);
        if (soundEvent == null) {
            return;
        }

        float volume = Math.max(0.0F, config.travelSound.volume);
        float minPitch = Math.max(0.1F, config.travelSound.pitchMin);
        float maxPitch = Math.max(minPitch, config.travelSound.pitchMax);
        float pitch = minPitch;
        if (maxPitch > minPitch) {
            pitch = minPitch + ThreadLocalRandom.current().nextFloat() * (maxPitch - minPitch);
        }

        level.playSound(null, x, y, z, soundEvent, SoundSource.AMBIENT, volume, pitch);
    }

    private SoundEvent resolveSoundEvent(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null) {
            return null;
        }

        return BuiltInRegistries.SOUND_EVENT.getOptional(resourceLocation).orElse(null);
    }

    private ParticleOptions resolveParticlePreset(String preset) {
        if (preset == null) {
            return ParticleTypes.END_ROD;
        }

        return switch (preset.toLowerCase(Locale.ROOT)) {
            case "ash" -> ParticleTypes.ASH;
            case "glow" -> ParticleTypes.GLOW;
            case "firework" -> ParticleTypes.FIREWORK;
            default -> ParticleTypes.END_ROD;
        };
    }
}


