package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.MainConfig;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds server-side timing state for future star event spawning.
 * Platform layers should call {@link #shouldStartCycle(long, MainConfig)} every server tick.
 */
public final class StarEventOrchestrator {
    private long nextCycleTick = 0L;

    public boolean shouldStartCycle(long currentTick, MainConfig config) {
        if (!config.enabled) {
            return false;
        }

        if (currentTick < nextCycleTick) {
            return false;
        }

        nextCycleTick = currentTick + calculateNextInterval(config);
        return true;
    }

    public long getNextCycleTick() {
        return nextCycleTick;
    }

    private long calculateNextInterval(MainConfig config) {
        int base = Math.max(20, config.scheduler.baseIntervalTicks);
        int jitter = Math.max(0, config.scheduler.intervalJitterTicks);
        if (jitter == 0) {
            return base;
        }

        return base + ThreadLocalRandom.current().nextInt(jitter + 1);
    }
}

