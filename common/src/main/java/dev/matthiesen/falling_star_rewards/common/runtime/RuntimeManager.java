package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.AnnouncementsConfig;
import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
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

    public static int runCycle(MinecraftServer server, MainConfig mainConfig, LoadedPreset presetConfig, AnnouncementsConfig announcementsConfig, boolean bypassActivationChecks) {
        return starEventService.runCycle(server, mainConfig, presetConfig, announcementsConfig, bypassActivationChecks);
    }

    public static void tick(MinecraftServer server, MainConfig config, AnnouncementsConfig announcementsConfig, LoadedPreset preset) {
        if (preset == null) {
            FallingStarRewards.INSTANCE.createWarnLog("No event presets available to start a cycle");
            return;
        }
        int gameTick = server.getTickCount();
        starEventService.onServerTick(server, preset);
        if (orchestrator.shouldStartCycle(gameTick, config)) {
            int spawned = starEventService.runCycle(server, config, preset, announcementsConfig);
            if (spawned > 0) {
                FallingStarRewards.INSTANCE.createInfoLog("Starting star cycle at tick " + gameTick + " (spawned=" + spawned + ")");
            }
        }
    }
}
