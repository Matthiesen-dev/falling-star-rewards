package dev.matthiesen.falling_star_rewards.common;

import dev.matthiesen.common.matthiesen_lib_api.abstracts.AbstractCommonMod;
import dev.matthiesen.common.matthiesen_lib_api.core.interfaces.MatthiesenLibServerEventHandler;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.config.FallingStarsConfigManager;
import dev.matthiesen.falling_star_rewards.common.config.AnnouncementsConfig;
import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import dev.matthiesen.falling_star_rewards.common.runtime.StarEventOrchestrator;
import dev.matthiesen.falling_star_rewards.common.runtime.StarEventService;
import dev.matthiesen.libs.faststats.Token;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class FallingStarRewards extends AbstractCommonMod {
    public static final String MOD_ID = "falling_star_rewards";
    private static final String MOD_NAME = "Falling Star Rewards";
    private static @Token final String METRICS_TOKEN = "3b8d656e1efa1d6eaa2ec90c7ad832bd";

    public static final FallingStarRewards INSTANCE = new FallingStarRewards();
    public static FallingStarsConfigManager CONFIG_MANAGER;

    private final StarEventOrchestrator orchestrator = new StarEventOrchestrator();
    private final StarEventService starEventService = new StarEventService();

    public FallingStarRewards() {
        super(MOD_ID, MOD_NAME);
        CONFIG_MANAGER = new FallingStarsConfigManager(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        CONFIG_MANAGER.init();

        reload().run();
        registerServerEventHandler(getServerEventHandler());
        registerCommand(FallingStarCommand.CMD);
        createInfoLog("Initializing Falling Star Rewards");
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    @Override
    public Runnable reload() {
        return () -> {
            loadConfigs();
            MainConfig config = getMainConfig();
            CONFIG_MANAGER.validateRewardsConfigs();
            createInfoLog("Reloaded Config (enabled=" + config.enabled + ")");
        };
    }

    public void loadConfigs() {
        CONFIG_MANAGER.getMainConfigManager().loadConfig();
        CONFIG_MANAGER.getEventsConfigManager().loadConfigs();
        CONFIG_MANAGER.getAnnouncementsConfigManager().loadConfig();
        CONFIG_MANAGER.getRewardsConfigManager().loadConfigs();
        CONFIG_MANAGER.getVisualsConfigManager().loadConfigs();
    }

    public MainConfig getMainConfig() {
        return CONFIG_MANAGER.getMainConfigManager().getConfig();
    }

    public FallingStarsConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }

    public AnnouncementsConfig getAnnouncementsConfig() {
        return CONFIG_MANAGER.getAnnouncementsConfigManager().getConfig();
    }

    public long getNextCycleTick() {
        return orchestrator.getNextCycleTick();
    }

    public int forceCycle(MinecraftServer server, String presetId, boolean bypassActivationChecks) {
        var preset = presetId != null ? CONFIG_MANAGER.loadPresetConfig(presetId) : CONFIG_MANAGER.loadRandomEventPreset();
        if (preset == null) {
            createWarnLog("No event presets available to start a cycle");
            return 0;
        }
        return starEventService.runCycle(server, preset, getAnnouncementsConfig(), bypassActivationChecks);
    }

    public int getActiveDropCount() {
        return starEventService.getActiveDropCount();
    }

    public int cleanupActiveDrops(MinecraftServer server) {
        return starEventService.cleanupActiveDrops(server);
    }

    public MatthiesenLibServerEventHandler getServerEventHandler() {
        return new MatthiesenLibServerEventHandler() {
            @Override
            public void onServerStart(MinecraftServer server) {
                createInfoLog("Server Started");
            }

            @Override
            public void onServerTick(MinecraftServer server) {
                MainConfig config = getMainConfig();
                if (!config.enabled) return;
                var preset = CONFIG_MANAGER.loadRandomEventPreset();
                if (preset == null) {
                    createWarnLog("No event presets available to start a cycle");
                    return;
                }
                AnnouncementsConfig announcementsConfig = getAnnouncementsConfig();
                starEventService.onServerTick(server, preset);
                var gameTick = server.getTickCount();
                if (orchestrator.shouldStartCycle(gameTick, preset)) {
                    int spawned = starEventService.runCycle(server, preset, announcementsConfig);
                    if (spawned > 0) {
                        createInfoLog("Starting star cycle at tick " + gameTick + " (spawned=" + spawned + ")");
                    }
                }
            }

            @Override
            public void onServerStop(MinecraftServer server) {
                createInfoLog("Server Stopped");
            }
        };
    }

    public void createWarnLog(String message) {
        getLogger().warn(message);
    }
}
