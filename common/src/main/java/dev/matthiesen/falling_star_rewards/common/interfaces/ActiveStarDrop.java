package dev.matthiesen.falling_star_rewards.common.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ActiveStarDrop(ResourceKey<Level> dimension, long startTick, long expireTick) {
}
