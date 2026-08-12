package dev.matthiesen.falling_star_rewards.common.config.def.event;

import dev.matthiesen.falling_star_rewards.common.interfaces.SpawnTargetScope;

public record Spawn(
        SpawnTargetScope targetScope,
        int minRadius,
        int maxRadius,
        int maxLocationAttempts,
        boolean allowWaterSpawns
) {
}
