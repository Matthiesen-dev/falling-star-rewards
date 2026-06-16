package dev.matthiesen.falling_star_rewards.common;

import dev.matthiesen.common.matthiesen_lib_api.abstracts.AbstractCommonMod;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigManager;
import dev.matthiesen.common.matthiesen_lib_api.core.interfaces.MatthiesenLibServerEventHandler;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
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

    private final ConfigManager<MainConfig> MAIN_CONFIG_MANAGER =
            createConfigManager(MainConfig.class, "config");
    private final StarEventOrchestrator orchestrator = new StarEventOrchestrator();
    private final StarEventService starEventService = new StarEventService();

    public FallingStarRewards() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public void initialize() {
        super.initialize();
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
            MAIN_CONFIG_MANAGER.loadConfig();
            MainConfig config = getMainConfig();
            createInfoLog("Reloaded Config (enabled=" + config.enabled
                    + ", baseIntervalTicks=" + config.scheduler.baseIntervalTicks + ")");
        };
    }

    public MainConfig getMainConfig() {
        return MAIN_CONFIG_MANAGER.getConfig();
    }

    public long getNextCycleTick() {
        return orchestrator.getNextCycleTick();
    }

    public int forceCycle(MinecraftServer server, int maxStars, boolean bypassActivationChecks) {
        return starEventService.runCycle(server, getMainConfig(), maxStars, bypassActivationChecks);
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
                starEventService.onServerTick(server, config);
                var gameTick = server.getTickCount();
                if (orchestrator.shouldStartCycle(gameTick, config)) {
                    int spawned = starEventService.runCycle(server, config);
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
}
