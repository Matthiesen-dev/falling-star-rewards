package dev.matthiesen.falling_star_rewards.common.config;

import dev.matthiesen.common.matthiesen_lib_api.config.ConfigFolderManager;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigManager;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.presets.EventPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.RewardsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.SchedulePresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.interfaces.LoadedPreset;
import dev.matthiesen.falling_star_rewards.common.interfaces.NamedPreset;
import dev.matthiesen.falling_star_rewards.common.runtime.RewardValidator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class FallingStarsConfigManager {
    private static final String BASE_ID = "base";

    private final FallingStarRewards MOD_INSTANCE;
    private ConfigManager<MainConfig> MAIN_CONFIG;
    private ConfigManager<PermissionsConfig> PERMISSIONS_CONFIG;
    private ConfigFolderManager<EventPresetConfig> EVENTS_CONFIG;
    private ConfigFolderManager<RewardsPresetConfig> REWARDS_CONFIG;
    private ConfigFolderManager<VisualsPresetConfig> VISUALS_CONFIG;
    private ConfigFolderManager<SchedulePresetConfig> SCHEDULES_CONFIG;

    public FallingStarsConfigManager(FallingStarRewards instance) {
        this.MOD_INSTANCE = instance;
    }

    public void init() {
        MAIN_CONFIG = MOD_INSTANCE.createConfigManager(MainConfig.class, "config");
        PERMISSIONS_CONFIG = MOD_INSTANCE.createConfigManager(PermissionsConfig.class, "permissions");
        EVENTS_CONFIG = MOD_INSTANCE.createConfigFolderManager(EventPresetConfig.class, "events");
        REWARDS_CONFIG = MOD_INSTANCE.createConfigFolderManager(RewardsPresetConfig.class, "rewards");
        VISUALS_CONFIG = MOD_INSTANCE.createConfigFolderManager(VisualsPresetConfig.class, "visuals");

        SCHEDULES_CONFIG = MOD_INSTANCE.createConfigFolderManager(SchedulePresetConfig.class, "schedules");

        // Load the main config to verify if we need to generate presets
        var config = MAIN_CONFIG.loadConfig();
        PERMISSIONS_CONFIG.loadConfig();
        EVENTS_CONFIG.loadConfigs();
        REWARDS_CONFIG.loadConfigs();
        VISUALS_CONFIG.loadConfigs();
        SCHEDULES_CONFIG.loadConfigs();
        handlePresetGeneration(config.enablePresetGeneration);
    }

    public void handlePresetGeneration(boolean enabled) {
        if (enabled) {
            EVENTS_CONFIG.loadConfig(BASE_ID);
            REWARDS_CONFIG.loadConfig(BASE_ID);
            VISUALS_CONFIG.loadConfig(BASE_ID);
            SCHEDULES_CONFIG.loadConfig(BASE_ID);
        }
    }

    public ConfigManager<MainConfig> getMainConfigManager() {
        return MAIN_CONFIG;
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

    public ConfigFolderManager<SchedulePresetConfig> getSchedulesConfigManager() {
        return SCHEDULES_CONFIG;
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

    public int calculateSchedulePresets() {
        var scheduleConfigs = SCHEDULES_CONFIG.loadConfigs();
        return scheduleConfigs.size();
    }

    public int RANDOM(int size) {
        return (int) (Math.random() * size);
    }

    public EventPresetConfig pickRandom(Map<String, EventPresetConfig> configMap) {
        var enabled = configMap.values().stream().filter(v -> v != null && v.enabled).toList();
        if (enabled.isEmpty()) {
            return null;
        }
        int index = RANDOM(enabled.size());
        return enabled.get(index);
    }

    public List<NamedPreset<SchedulePresetConfig>> resolveEnabledSchedules(MainConfig mainConfig) {
        List<NamedPreset<SchedulePresetConfig>> enabledSchedules = new ArrayList<>();
        if (mainConfig.enabledSchedules == null || mainConfig.enabledSchedules.isEmpty()) {
            return enabledSchedules;
        }

        LinkedHashSet<String> dedupedIds = new LinkedHashSet<>(mainConfig.enabledSchedules);
        for (String scheduleId : dedupedIds) {
            if (scheduleId == null || scheduleId.isBlank() || !SCHEDULES_CONFIG.hasConfig(scheduleId)) {
                continue;
            }
            SchedulePresetConfig schedule = SCHEDULES_CONFIG.getConfig(scheduleId);
            if (schedule.enabled) {
                enabledSchedules.add(new NamedPreset<>(scheduleId, schedule));
            }
        }

        return enabledSchedules;
    }

    public LoadedPreset loadPresetForSchedule(String scheduleId, SchedulePresetConfig scheduleConfig) {
        String eventPresetId = selectEventPresetId(scheduleId, scheduleConfig);
        if (eventPresetId == null) {
            return null;
        }
        return loadPresetConfig(eventPresetId);
    }

    public String selectEventPresetId(String scheduleId, SchedulePresetConfig scheduleConfig) {
        List<SchedulePresetConfig.EventEntry> eligibleEntries = scheduleConfig.eventEntries == null
                ? List.of()
                : scheduleConfig.eventEntries.stream()
                .filter(entry -> entry != null && entry.enabled)
                .filter(entry -> entry.eventPresetId != null && !entry.eventPresetId.isBlank())
                .filter(entry -> EVENTS_CONFIG.hasConfig(entry.eventPresetId) && EVENTS_CONFIG.getConfig(entry.eventPresetId).enabled)
                .toList();
        if (eligibleEntries.isEmpty()) {
            return null;
        }

        String mode = scheduleConfig.selectionMode == null ? "random" : scheduleConfig.selectionMode.toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "weighted" -> selectWeighted(eligibleEntries);
            case "rotation" -> selectRotation(scheduleId, scheduleConfig, eligibleEntries);
            default -> selectRandom(eligibleEntries);
        };
    }

    private String selectRandom(List<SchedulePresetConfig.EventEntry> entries) {
        int index = ThreadLocalRandom.current().nextInt(entries.size());
        return entries.get(index).eventPresetId;
    }

    private String selectWeighted(List<SchedulePresetConfig.EventEntry> entries) {
        int totalWeight = 0;
        for (SchedulePresetConfig.EventEntry entry : entries) {
            totalWeight += Math.max(1, entry.weight);
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (SchedulePresetConfig.EventEntry entry : entries) {
            cursor += Math.max(1, entry.weight);
            if (roll < cursor) {
                return entry.eventPresetId;
            }
        }
        return entries.getLast().eventPresetId;
    }

    private String selectRotation(String scheduleId, SchedulePresetConfig scheduleConfig, List<SchedulePresetConfig.EventEntry> entries) {
        if (scheduleConfig.state == null) {
            scheduleConfig.state = new SchedulePresetConfig.State();
        }

        int current = Math.max(0, scheduleConfig.state.rotationCursor);
        int index = current % entries.size();
        String selected = entries.get(index).eventPresetId;

        int next = (index + 1) % entries.size();
        if (next != scheduleConfig.state.rotationCursor) {
            scheduleConfig.state.rotationCursor = next;
            SCHEDULES_CONFIG.setConfig(scheduleId, scheduleConfig);
            SCHEDULES_CONFIG.saveConfig(scheduleId);
        }

        return selected;
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
        if (eventPresetId == null || eventPresetId.isBlank() || !EVENTS_CONFIG.hasConfig(eventPresetId)) {
            return null;
        }
        var eventConfig = EVENTS_CONFIG.loadConfig(eventPresetId);
        if (!eventConfig.enabled) {
            return null;
        }
        var rewardsConfig = REWARDS_CONFIG.loadConfig(eventConfig.rewardsPresetId);
        var visualsConfig = VISUALS_CONFIG.loadConfig(eventConfig.visualsPresetId);
        return new LoadedPreset(eventConfig, rewardsConfig, visualsConfig);
    }
}
