package dev.matthiesen.falling_star_rewards.common;

import dev.matthiesen.common.matthiesen_lib_api.abstracts.AbstractCommonMod;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigManager;
import dev.matthiesen.common.matthiesen_lib_api.core.interfaces.MatthiesenLibServerEventHandler;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.config.AnnouncementsConfig;
import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import dev.matthiesen.falling_star_rewards.common.config.RewardsConfig;
import dev.matthiesen.falling_star_rewards.common.config.VisualsConfig;
import dev.matthiesen.falling_star_rewards.common.runtime.StarEventOrchestrator;
import dev.matthiesen.falling_star_rewards.common.runtime.StarEventService;
import dev.matthiesen.falling_star_rewards.common.runtime.RewardValidator;
import dev.matthiesen.libs.faststats.Token;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class FallingStarRewards extends AbstractCommonMod {
    public static final String MOD_ID = "falling_star_rewards";
    private static final String MOD_NAME = "Falling Star Rewards";
    private static @Token final String METRICS_TOKEN = "3b8d656e1efa1d6eaa2ec90c7ad832bd";

    public static final FallingStarRewards INSTANCE = new FallingStarRewards();

    private final ConfigManager<AnnouncementsConfig> ANNOUNCEMENTS_CONFIG_MANAGER =
            createConfigManager(AnnouncementsConfig.class, "announcements");
    private final ConfigManager<MainConfig> MAIN_CONFIG_MANAGER =
            createConfigManager(MainConfig.class, "config");
    private final ConfigManager<RewardsConfig> REWARDS_CONFIG_MANAGER =
            createConfigManager(RewardsConfig.class, "rewards");
    private final ConfigManager<VisualsConfig> VISUALS_CONFIG_MANAGER =
            createConfigManager(VisualsConfig.class, "visuals");

    private final StarEventOrchestrator orchestrator = new StarEventOrchestrator();
    private final StarEventService starEventService = new StarEventService();
    private final RewardValidator rewardValidator = new RewardValidator();

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
            ANNOUNCEMENTS_CONFIG_MANAGER.loadConfig();
            MAIN_CONFIG_MANAGER.loadConfig();
            REWARDS_CONFIG_MANAGER.loadConfig();
            VISUALS_CONFIG_MANAGER.loadConfig();
            MainConfig config = getMainConfig();
            RewardsConfig rewardsConfig = getRewardsConfig();
            rewardValidator.validateRewards(rewardsConfig);
            for (String message : rewardValidator.getValidationMessages()) {
                createWarnLog(message);
            }
            if (rewardValidator.getInvalidEntries() > 0) {
                createWarnLog("Reward validation: " + rewardValidator.getValidEntries() + " valid, "
                        + rewardValidator.getInvalidEntries() + " invalid");
            } else if (rewardValidator.getValidEntries() > 0) {
                createInfoLog("All " + rewardValidator.getValidEntries() + " reward entries validated successfully");
            }
            createInfoLog("Reloaded Config (enabled=" + config.enabled
                    + ", baseIntervalTicks=" + config.scheduler.baseIntervalTicks + ")");
        };
    }

    public MainConfig getMainConfig() {
        return MAIN_CONFIG_MANAGER.getConfig();
    }

    public RewardsConfig getRewardsConfig() {
        return REWARDS_CONFIG_MANAGER.getConfig();
    }

    public VisualsConfig getVisualsConfig() {
        return VISUALS_CONFIG_MANAGER.getConfig();
    }

    public AnnouncementsConfig getAnnouncementsConfig() {
        return ANNOUNCEMENTS_CONFIG_MANAGER.getConfig();
    }

    public long getNextCycleTick() {
        return orchestrator.getNextCycleTick();
    }

    public int forceCycle(MinecraftServer server, int maxStars, boolean bypassActivationChecks) {
        return starEventService.runCycle(server, getMainConfig(), getRewardsConfig(), getVisualsConfig(), getAnnouncementsConfig(), maxStars, bypassActivationChecks);
    }

    public int getActiveDropCount() {
        return starEventService.getActiveDropCount();
    }

    public int cleanupActiveDrops(MinecraftServer server) {
        return starEventService.cleanupActiveDrops(server);
    }

    public RewardValidator getRewardValidator() {
        return rewardValidator;
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
                RewardsConfig rewardsConfig = getRewardsConfig();
                VisualsConfig visualsConfig = getVisualsConfig();
                AnnouncementsConfig announcementsConfig = getAnnouncementsConfig();
                starEventService.onServerTick(server, visualsConfig);
                var gameTick = server.getTickCount();
                if (orchestrator.shouldStartCycle(gameTick, config)) {
                    int spawned = starEventService.runCycle(server, config, rewardsConfig, visualsConfig, announcementsConfig);
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
