package dev.matthiesen.falling_star_rewards.common.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record EventCommandContext(
        LoadedPreset presetConfig,
        ServerPlayer nearbyPlayer,
        BlockPos spawnPos,
        ItemStack rewardItem
) {
    public String ensureNoPreSlash(String input) {
        return input.startsWith("/") ? input.substring(1) : input;
    }

    public String processPlaceholders(String input) {
        return input.replace("%nearbyPlayer%", nearbyPlayer.getName().getString())
                .replace("%spawnPos%", getXYZ(spawnPos))
                .replace("%rewardItem%", rewardItem.getHoverName().getString());
    }

    public String getXYZ(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
