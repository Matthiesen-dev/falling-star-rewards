package dev.matthiesen.falling_star_rewards.common.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds server-side timing state for future star event spawning.
 * Platform layers should call {@link #shouldStartCycle(String, long, int, int)} for each enabled schedule every tick.
 */
public final class StarEventOrchestrator {
    private final Map<String, Long> nextCycleBySchedule = new HashMap<>();

    public boolean shouldStartCycle(String scheduleId, long currentTick, int baseIntervalTicks, int intervalJitterTicks) {
        long nextCycleTick = nextCycleBySchedule.getOrDefault(scheduleId, 0L);
        if (currentTick < nextCycleTick) {
            return false;
        }

        nextCycleBySchedule.put(scheduleId, currentTick + calculateNextInterval(baseIntervalTicks, intervalJitterTicks));
        return true;
    }

    public long getNextCycleTick() {
        if (nextCycleBySchedule.isEmpty()) {
            return 0L;
        }
        long min = Long.MAX_VALUE;
        for (long value : nextCycleBySchedule.values()) {
            min = Math.min(min, value);
        }
        return min == Long.MAX_VALUE ? 0L : min;
    }

    private long calculateNextInterval(int baseIntervalTicks, int intervalJitterTicks) {
        int base = Math.max(20, baseIntervalTicks);
        int jitter = Math.max(0, intervalJitterTicks);
        if (jitter == 0) {
            return base;
        }

        return base + ThreadLocalRandom.current().nextInt(jitter + 1);
    }
}

