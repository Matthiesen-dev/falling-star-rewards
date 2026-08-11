package dev.matthiesen.falling_star_rewards.common.config;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.presets.EventPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.RewardsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.SchedulePresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import dev.matthiesen.matthiesen_core.common.utility.config.ConfigFolderManager;

public final class FallingStarsConfigManager {
    private static final String BASE_ID = "base";

    private final FallingStarRewards MOD_INSTANCE;
    private ConfigFolderManager<EventPresetConfig> EVENTS_CONFIG;
    private ConfigFolderManager<RewardsPresetConfig> REWARDS_CONFIG;
    private ConfigFolderManager<VisualsPresetConfig> VISUALS_CONFIG;
    private ConfigFolderManager<SchedulePresetConfig> SCHEDULES_CONFIG;

    public FallingStarsConfigManager(FallingStarRewards instance) {
        this.MOD_INSTANCE = instance;
    }

    public void init() {
        EVENTS_CONFIG = MOD_INSTANCE.createConfigFolderManager(EventPresetConfig.class, "events");
        REWARDS_CONFIG = MOD_INSTANCE.createConfigFolderManager(RewardsPresetConfig.class, "rewards");
        VISUALS_CONFIG = MOD_INSTANCE.createConfigFolderManager(VisualsPresetConfig.class, "visuals");
        SCHEDULES_CONFIG = MOD_INSTANCE.createConfigFolderManager(SchedulePresetConfig.class, "schedules");

        EVENTS_CONFIG.loadConfigs();
        REWARDS_CONFIG.loadConfigs();
        VISUALS_CONFIG.loadConfigs();
        SCHEDULES_CONFIG.loadConfigs();
        handlePresetGeneration(FSConfig.SERVER_CONFIG.enablePresetGeneration.getAsBoolean());
    }

    public void handlePresetGeneration(boolean enabled) {
        if (enabled) {
            EVENTS_CONFIG.loadConfig(BASE_ID);
            REWARDS_CONFIG.loadConfig(BASE_ID);
            VISUALS_CONFIG.loadConfig(BASE_ID);
            SCHEDULES_CONFIG.loadConfig(BASE_ID);
        }
    }

    public ConfigFolderManager<EventPresetConfig> getEventsConfigManager() {
        return EVENTS_CONFIG;
    }

    public ConfigFolderManager<RewardsPresetConfig> getRewardsConfigManager() {
        return REWARDS_CONFIG;
    }

    public ConfigFolderManager<VisualsPresetConfig> getVisualsConfigManager() {
        return VISUALS_CONFIG;
    }

    public ConfigFolderManager<SchedulePresetConfig> getSchedulesConfigManager() {
        return SCHEDULES_CONFIG;
    }
}
