package dev.matthiesen.falling_star_rewards.common.config;

import dev.matthiesen.common.matthiesen_lib_api.config.ConfigFolderManager;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigManager;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.presets.EventPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.RewardsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.interfaces.LoadedPreset;
import dev.matthiesen.falling_star_rewards.common.runtime.RewardValidator;

import java.util.Map;

public final class FallingStarsConfigManager {
    private final FallingStarRewards MOD_INSTANCE;
    private ConfigManager<MainConfig> MAIN_CONFIG;
    private ConfigManager<AnnouncementsConfig> ANNOUNCEMENTS_CONFIG;
    private ConfigManager<PermissionsConfig> PERMISSIONS_CONFIG;
    private ConfigFolderManager<EventPresetConfig> EVENTS_CONFIG;
    private ConfigFolderManager<RewardsPresetConfig> REWARDS_CONFIG;
    private ConfigFolderManager<VisualsPresetConfig> VISUALS_CONFIG;

    public FallingStarsConfigManager(FallingStarRewards instance) {
        this.MOD_INSTANCE = instance;
    }

    public void init() {
        MAIN_CONFIG = MOD_INSTANCE.createConfigManager(MainConfig.class, "config");
        ANNOUNCEMENTS_CONFIG = MOD_INSTANCE.createConfigManager(AnnouncementsConfig.class, "announcements");
        PERMISSIONS_CONFIG = MOD_INSTANCE.createConfigManager(PermissionsConfig.class, "permissions");
        EVENTS_CONFIG = MOD_INSTANCE.createConfigFolderManager(EventPresetConfig.class, "events");
        REWARDS_CONFIG = MOD_INSTANCE.createConfigFolderManager(RewardsPresetConfig.class, "rewards");
        VISUALS_CONFIG = MOD_INSTANCE.createConfigFolderManager(VisualsPresetConfig.class, "visuals");

        // Load the main config to verify if we need to generate presets
        var config = MAIN_CONFIG.loadConfig();
        ANNOUNCEMENTS_CONFIG.loadConfig();
        PERMISSIONS_CONFIG.loadConfig();
        EVENTS_CONFIG.loadConfigs();
        REWARDS_CONFIG.loadConfigs();
        VISUALS_CONFIG.loadConfigs();
        handlePresetGeneration(config.enablePresetGeneration);
    }

    public void handlePresetGeneration(boolean enabled) {
        if (enabled) {
            EVENTS_CONFIG.loadConfig("base");
            REWARDS_CONFIG.loadConfig("base");
            VISUALS_CONFIG.loadConfig("base");
        }
    }

    public ConfigManager<MainConfig> getMainConfigManager() {
        return MAIN_CONFIG;
    }

    public ConfigManager<AnnouncementsConfig> getAnnouncementsConfigManager() {
        return ANNOUNCEMENTS_CONFIG;
    }

    public ConfigManager<PermissionsConfig> getPermissionsConfigManager() {
        return PERMISSIONS_CONFIG;
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

    public void validateRewardsConfigs() {
        var rewardsConfigs = REWARDS_CONFIG.loadConfigs();
        for (Map.Entry<String, RewardsPresetConfig> entry : rewardsConfigs.entrySet()) {
            String configName = entry.getKey();
            RewardsPresetConfig config = entry.getValue();

            var validator = new RewardValidator();
            validator.validateRewards(config);
            for (String message : validator.getValidationMessages()) {
                MOD_INSTANCE.createWarnLog("[" + configName + "] " + message);
            }
            if (validator.getInvalidEntries() > 0) {
                MOD_INSTANCE.createWarnLog("[" + configName + "] Reward validation: " + validator.getValidEntries() + " valid, "
                        + validator.getInvalidEntries() + " invalid");
            } else if (validator.getValidEntries() > 0) {
                MOD_INSTANCE.createInfoLog("[" + configName + "] All " + validator.getValidEntries() + " reward entries validated successfully");
            }
        }

    }

    public int calculateEventPresets() {
        var eventConfigs = EVENTS_CONFIG.loadConfigs();
        return eventConfigs.size();
    }

    public int calculateRewardPresets() {
        var rewardsConfigs = REWARDS_CONFIG.loadConfigs();
        return rewardsConfigs.size();
    }

    public int RANDOM(int size) {
        return (int) (Math.random() * size);
    }

    public EventPresetConfig pickRandom(Map<String, EventPresetConfig> configMap) {
        if (configMap.isEmpty()) {
            return null;
        }
        int index = RANDOM(configMap.size());
        return configMap.values().stream().filter(v -> v.enabled).skip(index).findFirst().orElse(null);
    }

    public LoadedPreset loadRandomEventPreset() {
        var possibleEvents = EVENTS_CONFIG.getConfigs();
        if (possibleEvents.isEmpty()) {
            return null;
        }
        var selectedEvent = pickRandom(possibleEvents);
        if (selectedEvent == null) {
            return null;
        }
        var rewardsConfig = REWARDS_CONFIG.loadConfig(selectedEvent.rewardsPresetId);
        var visualsConfig = VISUALS_CONFIG.loadConfig(selectedEvent.visualsPresetId);
        return new LoadedPreset(selectedEvent, rewardsConfig, visualsConfig);
    }

    public LoadedPreset loadPresetConfig(String eventPresetId) {
        var eventConfig = EVENTS_CONFIG.loadConfig(eventPresetId);
        var rewardsConfig = REWARDS_CONFIG.loadConfig(eventConfig.rewardsPresetId);
        var visualsConfig = VISUALS_CONFIG.loadConfig(eventConfig.visualsPresetId);
        return new LoadedPreset(eventConfig, rewardsConfig, visualsConfig);
    }
}
