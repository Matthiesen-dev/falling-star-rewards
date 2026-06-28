package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.SchedulePresetConfig;
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

    public static int runCycle(MinecraftServer server, MainConfig mainConfig, LoadedPreset presetConfig, boolean bypassActivationChecks) {
        return starEventService.runCycle(server, mainConfig, presetConfig, bypassActivationChecks, null);
    }

    public static void tick(MinecraftServer server, MainConfig config) {
        var enabledSchedules = FallingStarRewards.CONFIG_MANAGER.resolveEnabledSchedules(config);
        if (enabledSchedules.isEmpty()) {
            return;
        }

        int gameTick = server.getTickCount();
        LoadedPreset visualsSourcePreset = FallingStarRewards.CONFIG_MANAGER.loadRandomEventPreset();
        if (visualsSourcePreset != null) {
            starEventService.onServerTick(server, visualsSourcePreset);
        }

        for (var schedulePreset : enabledSchedules) {
            String scheduleId = schedulePreset.name();
            SchedulePresetConfig scheduleConfig = schedulePreset.config();

            if (!orchestrator.shouldStartCycle(
                    scheduleId,
                    gameTick,
                    scheduleConfig.baseIntervalTicks,
                    scheduleConfig.intervalJitterTicks
            )) {
                continue;
            }

            LoadedPreset preset = FallingStarRewards.CONFIG_MANAGER.loadPresetForSchedule(scheduleId, scheduleConfig);
            if (preset == null) {
                FallingStarRewards.INSTANCE.createWarnLog("No valid event presets found for schedule: " + scheduleId);
                continue;
            }

            int spawned = starEventService.runCycle(server, config, preset, false, scheduleConfig);
            if (spawned > 0) {
                FallingStarRewards.INSTANCE.createInfoLog(
                        "Starting star cycle at tick " + gameTick + " for schedule '" + scheduleId + "' (spawned=" + spawned + ")"
                );
            }
        }
    }
}
