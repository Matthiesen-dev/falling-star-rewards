package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.FSConfig;
import dev.matthiesen.falling_star_rewards.common.interfaces.LoadedPreset;
import net.minecraft.server.MinecraftServer;

public final class RuntimeManager {
    private static final StarEventOrchestrator orchestrator = new StarEventOrchestrator();
    private static final StarEventService starEventService = new StarEventService();

    public static long getNextCycleTick() {
        return orchestrator.getNextCycleTick();
    }

    public static int getActiveDropCount() {
        return starEventService.getActiveDropCount();
    }

    public static int cleanupActiveDrops(MinecraftServer server) {
        return starEventService.cleanupActiveDrops(server);
    }

    public static int runCycle(MinecraftServer server, LoadedPreset presetConfig, boolean bypassActivationChecks) {
        return starEventService.runCycle(server, presetConfig, bypassActivationChecks, null);
    }

    public static void tick(MinecraftServer server) {
        var enabledSchedules = FSConfig.resolveEnabledSchedules();
        if (enabledSchedules.isEmpty()) {
            return;
        }

        int gameTick = server.getTickCount();
        LoadedPreset visualsSourcePreset = FSConfig.loadRandomEventPreset();
        if (visualsSourcePreset != null) {
            starEventService.onServerTick(server, visualsSourcePreset);
        }

        for (var schedulePreset : enabledSchedules) {
            String scheduleId = schedulePreset.scheduleId();

            if (!orchestrator.shouldStartCycle(
                    scheduleId,
                    gameTick,
                    schedulePreset.baseIntervalTicks(),
                    schedulePreset.intervalJitterTicks()
            )) {
                continue;
            }

            LoadedPreset preset = FSConfig.loadPresetForSchedule(schedulePreset);
            if (preset == null) {
                FallingStarRewards.INSTANCE.createWarnLog("No valid event presets found for schedule: " + scheduleId);
                continue;
            }

            int spawned = starEventService.runCycle(server, preset, false, schedulePreset);
            if (spawned > 0) {
                FallingStarRewards.INSTANCE.createInfoLog(
                        "Starting star cycle at tick " + gameTick + " for schedule '" + scheduleId + "' (spawned=" + spawned + ")"
                );
            }
        }
    }
}
