package dev.matthiesen.falling_star_rewards.common.config;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.def.EventPreset;
import dev.matthiesen.falling_star_rewards.common.config.def.RewardPreset;
import dev.matthiesen.falling_star_rewards.common.config.def.SchedulePreset;
import dev.matthiesen.falling_star_rewards.common.config.def.VisualsPreset;
import dev.matthiesen.falling_star_rewards.common.interfaces.LoadedPreset;
import dev.matthiesen.falling_star_rewards.common.runtime.RewardValidator;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class FSConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_CONFIG_SPEC;

    public static final PermissionsStartupConfig PERMISSIONS_START_CONFIG;
    public static final ModConfigSpec PERMISSIONS_START_SPEC;

    public static final EventsServerConfig EVENTS_CONFIG;
    public static final ModConfigSpec EVENTS_CONFIG_SPEC;

    public static final RewardsServerConfig REWARDS_CONFIG;
    public static final ModConfigSpec REWARDS_CONFIG_SPEC;

    public static final ScheduleServerConfig SCHEDULE_CONFIG;
    public static final ModConfigSpec SCHEDULE_CONFIG_SPEC;

    public static final VisualsServerConfig VISUALS_CONFIG;
    public static final ModConfigSpec VISUALS_CONFIG_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = specPair.getLeft();
        SERVER_CONFIG_SPEC = specPair.getRight();

        Pair<PermissionsStartupConfig, ModConfigSpec> permissionsSpecPair = new ModConfigSpec.Builder().configure(PermissionsStartupConfig::new);
        PERMISSIONS_START_CONFIG = permissionsSpecPair.getLeft();
        PERMISSIONS_START_SPEC = permissionsSpecPair.getRight();

        Pair<EventsServerConfig, ModConfigSpec> eventsSpecPair = new ModConfigSpec.Builder().configure(EventsServerConfig::new);
        EVENTS_CONFIG = eventsSpecPair.getLeft();
        EVENTS_CONFIG_SPEC = eventsSpecPair.getRight();

        Pair<RewardsServerConfig, ModConfigSpec> rewardsSpecPair = new ModConfigSpec.Builder().configure(RewardsServerConfig::new);
        REWARDS_CONFIG = rewardsSpecPair.getLeft();
        REWARDS_CONFIG_SPEC = rewardsSpecPair.getRight();

        Pair<ScheduleServerConfig, ModConfigSpec> scheduleSpecPair = new ModConfigSpec.Builder().configure(ScheduleServerConfig::new);
        SCHEDULE_CONFIG = scheduleSpecPair.getLeft();
        SCHEDULE_CONFIG_SPEC = scheduleSpecPair.getRight();

        Pair<VisualsServerConfig, ModConfigSpec> visualsSpecPair = new ModConfigSpec.Builder().configure(VisualsServerConfig::new);
        VISUALS_CONFIG = visualsSpecPair.getLeft();
        VISUALS_CONFIG_SPEC = visualsSpecPair.getRight();
    }

    public static List<EventPreset> getEventPresets() {
        return EVENTS_CONFIG.eventPresets.get()
                .stream()
                .filter(EventPreset::isValid)
                .map(EventPreset::deserialize)
                .toList();
    }

    public static List<RewardPreset> getRewardPresets() {
        return REWARDS_CONFIG.rewardPresets.get()
                .stream()
                .filter(RewardPreset::isValid)
                .map(RewardPreset::deserialize)
                .toList();
    }

    public static List<SchedulePreset> getSchedulePresets() {
        return SCHEDULE_CONFIG.schedulePresets.get()
                .stream()
                .filter(SchedulePreset::isValid)
                .map(SchedulePreset::deserialize)
                .toList();
    }

    public static void setSchedulePreset(SchedulePreset schedulePreset) {
        var originalSchedulePreset = SCHEDULE_CONFIG.schedulePresets.get();

        var updatedSchedulePreset = originalSchedulePreset.stream()
                .map(config -> {
                    if (config.get("scheduleId").equals(schedulePreset.scheduleId())) {
                        return schedulePreset.serialize();
                    } else {
                        return config;
                    }
                })
                .toList();

        SCHEDULE_CONFIG.schedulePresets.set(updatedSchedulePreset);
        SCHEDULE_CONFIG.schedulePresets.save();
    }

    public static List<VisualsPreset> getVisualsPresets() {
        return VISUALS_CONFIG.visualsPresets.get()
                .stream()
                .filter(VisualsPreset::isValid)
                .map(VisualsPreset::deserialize)
                .toList();
    }

    public static void validateRewardsConfigs() {
        var rewardsConfigs = getRewardPresets();
        for (var entry : rewardsConfigs) {
            String configName = entry.rewardId();

            var validator = new RewardValidator();
            validator.validateRewards(entry);

            for (String message : validator.getValidationMessages()) {
                FallingStarRewards.INSTANCE.createWarnLog("[" + configName + "] " + message);
            }
            if (validator.getInvalidEntries() > 0) {
                FallingStarRewards.INSTANCE.createWarnLog("[" + configName + "] Reward validation: " + validator.getValidEntries() + " valid, "
                        + validator.getInvalidEntries() + " invalid");
            } else if (validator.getValidEntries() > 0) {
                FallingStarRewards.INSTANCE.createInfoLog("[" + configName + "] All " + validator.getValidEntries() + " reward entries validated successfully");
            }
        }
    }

    public static int calculateEventPresets() {
        return getEventPresets().size();
    }

    public static int calculateRewardPresets() {
        return getRewardPresets().size();
    }

    public static int calculateSchedulePresets() {
        return getSchedulePresets().size();
    }

    public static int RANDOM(int size) {
        return (int) (Math.random() * size);
    }

    public static EventPreset pickRandom(List<EventPreset> presets) {
        var enabled = presets.stream().filter(v -> v != null && v.enabled()).toList();
        if (enabled.isEmpty()) {
            return null;
        }
        int index = RANDOM(enabled.size());
        return enabled.get(index);
    }

    public static List<SchedulePreset> resolveEnabledSchedules() {
        var enabledSchedules = SERVER_CONFIG.enabledSchedules.get();

        return enabledSchedules.stream()
                .map(scheduleId -> SCHEDULE_CONFIG.schedulePresets.get().stream()
                        .filter(preset -> preset.get("scheduleId").equals(scheduleId))
                        .findFirst()
                        .map(SchedulePreset::deserialize)
                        .orElse(null))
                .filter(preset -> preset != null && preset.enabled())
                .toList();
    }

    public static LoadedPreset loadPresetConfig(String eventPresetId) {
        EventPreset eventPreset = EVENTS_CONFIG.eventPresets.get().stream()
                .filter(preset -> preset.get("eventId").equals(eventPresetId))
                .findFirst()
                .map(EventPreset::deserialize)
                .orElse(null);

        if (eventPreset == null) {
            return null;
        }

        RewardPreset rewardPreset = REWARDS_CONFIG.rewardPresets.get().stream()
                .filter(preset -> preset.get("rewardId").equals(eventPreset.rewardsPresetId()))
                .findFirst()
                .map(RewardPreset::deserialize)
                .orElse(null);

        VisualsPreset visualsPreset = VISUALS_CONFIG.visualsPresets.get().stream()
                .filter(preset -> preset.get("visualsId").equals(eventPreset.visualsPresetId()))
                .findFirst()
                .map(VisualsPreset::deserialize)
                .orElse(null);

        return new LoadedPreset(eventPreset, rewardPreset, visualsPreset);
    }

    public static LoadedPreset loadRandomEventPreset() {
        List<EventPreset> eventPresets = getEventPresets();
        EventPreset randomEventPreset = pickRandom(eventPresets);

        if (randomEventPreset == null) {
            return null;
        }

        RewardPreset rewardPreset = REWARDS_CONFIG.rewardPresets.get().stream()
                .filter(preset -> preset.get("rewardId").equals(randomEventPreset.rewardsPresetId()))
                .findFirst()
                .map(RewardPreset::deserialize)
                .orElse(null);

        VisualsPreset visualsPreset = VISUALS_CONFIG.visualsPresets.get().stream()
                .filter(preset -> preset.get("visualsId").equals(randomEventPreset.visualsPresetId()))
                .findFirst()
                .map(VisualsPreset::deserialize)
                .orElse(null);

        return new LoadedPreset(randomEventPreset, rewardPreset, visualsPreset);
    }

    public static LoadedPreset loadPresetForSchedule(SchedulePreset schedulePreset) {
        String eventPresetId = selectEventPresetId(schedulePreset);
        if (eventPresetId == null) {
            return null;
        }
        return loadPresetConfig(eventPresetId);
    }

    private static String selectEventPresetId(SchedulePreset schedulePreset) {
        List<EventPreset> eventPresets = getEventPresets();

        List<SchedulePreset.EventEntry> eventEntries = schedulePreset.eventEntries() == null
                ? List.of()
                : schedulePreset.eventEntries().stream()
                .filter(entry -> entry != null && entry.enabled())
                .filter(entry -> entry.eventId() != null && !entry.eventId().isBlank())
                .filter(entry -> eventPresets.stream().anyMatch(preset -> preset.eventId().equals(entry.eventId())))
                .toList();

        if (eventEntries.isEmpty()) {
            return null;
        }

        SchedulePreset.SelectionMode selectionMode = schedulePreset.selectionMode();
        return switch (selectionMode) {
            case WEIGHTED -> selectWeighted(eventEntries);
            case ROTATION -> selectRotation(schedulePreset, eventEntries);
            default -> selectRandom(eventEntries);
        };
    }

    private static String selectRandom(List<SchedulePreset.EventEntry> eventEntries) {
        int index = ThreadLocalRandom.current().nextInt(eventEntries.size());
        return eventEntries.get(index).eventId();
    }

    private static String selectWeighted(List<SchedulePreset.EventEntry> eventEntries) {
        int totalWeight = 0;
        for (SchedulePreset.EventEntry entry : eventEntries) {
            totalWeight += entry.weight();
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (SchedulePreset.EventEntry entry : eventEntries) {
            cursor += Math.max(1, entry.weight());
            if (roll < cursor) {
                return entry.eventId();
            }
        }
        return eventEntries.getLast().eventId();
    }

    private static String selectRotation(SchedulePreset schedulePreset, List<SchedulePreset.EventEntry> eventEntries) {
        int current = Math.max(0, schedulePreset.state().rotationCursor);
        int index = current % eventEntries.size();
        String selected = eventEntries.get(index).eventId();
        int next = (index + 1) % eventEntries.size();
        if (next != schedulePreset.state().rotationCursor) {
            schedulePreset.state().rotationCursor = next;
            setSchedulePreset(schedulePreset);
        }
        return selected;
    }
}
