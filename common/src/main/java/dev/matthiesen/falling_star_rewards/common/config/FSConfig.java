package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.def.*;
import dev.matthiesen.falling_star_rewards.common.config.def.schedule.EventEntry;
import dev.matthiesen.falling_star_rewards.common.interfaces.runtime.LoadedPreset;
import dev.matthiesen.falling_star_rewards.common.interfaces.SelectionMode;
import dev.matthiesen.falling_star_rewards.common.runtime.RewardValidator;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class FSConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_CONFIG_SPEC;

    public static final PermissionsStartupConfig PERMISSIONS_START_CONFIG;
    public static final ModConfigSpec PERMISSIONS_START_SPEC;

    public static final FSPresets.EventsConfig EVENTS_CONFIG;
    public static final ModConfigSpec EVENTS_CONFIG_SPEC;

    public static final FSPresets.RewardsConfig REWARDS_CONFIG;
    public static final ModConfigSpec REWARDS_CONFIG_SPEC;

    public static final FSPresets.SchedulesConfig SCHEDULE_CONFIG;
    public static final ModConfigSpec SCHEDULE_CONFIG_SPEC;

    public static final FSPresets.VisualsConfig VISUALS_CONFIG;
    public static final ModConfigSpec VISUALS_CONFIG_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = specPair.getLeft();
        SERVER_CONFIG_SPEC = specPair.getRight();

        Pair<PermissionsStartupConfig, ModConfigSpec> permissionsSpecPair = new ModConfigSpec.Builder().configure(PermissionsStartupConfig::new);
        PERMISSIONS_START_CONFIG = permissionsSpecPair.getLeft();
        PERMISSIONS_START_SPEC = permissionsSpecPair.getRight();

        Pair<FSPresets.EventsConfig, ModConfigSpec> eventsSpecPair = new ModConfigSpec.Builder().configure(FSPresets.EventsConfig::new);
        EVENTS_CONFIG = eventsSpecPair.getLeft();
        EVENTS_CONFIG_SPEC = eventsSpecPair.getRight();

        Pair<FSPresets.RewardsConfig, ModConfigSpec> rewardsSpecPair = new ModConfigSpec.Builder().configure(FSPresets.RewardsConfig::new);
        REWARDS_CONFIG = rewardsSpecPair.getLeft();
        REWARDS_CONFIG_SPEC = rewardsSpecPair.getRight();

        Pair<FSPresets.SchedulesConfig, ModConfigSpec> scheduleSpecPair = new ModConfigSpec.Builder().configure(FSPresets.SchedulesConfig::new);
        SCHEDULE_CONFIG = scheduleSpecPair.getLeft();
        SCHEDULE_CONFIG_SPEC = scheduleSpecPair.getRight();

        Pair<FSPresets.VisualsConfig, ModConfigSpec> visualsSpecPair = new ModConfigSpec.Builder().configure(FSPresets.VisualsConfig::new);
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

    public static List<String> getEventPresetIds() {
        return getEventPresets().stream().map(EventPreset::eventId).toList();
    }

    public static boolean hasEventPreset(String eventId) {
        return getEventPresets().stream().anyMatch(preset -> preset.eventId().equals(eventId));
    }

    public static EventPreset getEventPreset(String eventId) {
        return getEventPresets().stream()
                .filter(preset -> preset.eventId().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    public static void setEventPreset(EventPreset eventPreset) {
        var original = EVENTS_CONFIG.eventPresets.get();
        boolean exists = original.stream().anyMatch(config -> Objects.equals(config.get("eventId"), eventPreset.eventId()));
        List<Config> updated = original.stream()
                .map(config -> Objects.equals(config.get("eventId"), eventPreset.eventId()) ? eventPreset.serialize() : config)
                .toList();

        if (!exists) {
            updated = new ArrayList<>(updated);
            updated.add(eventPreset.serialize());
        }

        EVENTS_CONFIG.eventPresets.set(updated);
        EVENTS_CONFIG.eventPresets.save();
    }

    public static boolean createEventPreset(String eventId) {
        if (hasEventPreset(eventId)) {
            return false;
        }
        EventPreset base = EventPreset.deserialize(EventPreset.getDefaultConfig().getFirst());
        setEventPreset(new EventPreset(
                eventId,
                base.enabled(),
                base.rewardsPresetId(),
                base.visualsPresetId(),
                base.commands(),
                base.spawn(),
                base.announcement()
        ));
        return true;
    }

    public static boolean deleteEventPreset(String eventId) {
        var original = EVENTS_CONFIG.eventPresets.get();
        List<Config> filtered = original.stream()
                .filter(config -> !Objects.equals(config.get("eventId"), eventId))
                .map(config -> (Config) config)
                .toList();
        if (filtered.size() == original.size()) {
            return false;
        }
        EVENTS_CONFIG.eventPresets.set(filtered);
        EVENTS_CONFIG.eventPresets.save();
        return true;
    }

    public static List<RewardPreset> getRewardPresets() {
        return REWARDS_CONFIG.rewardPresets.get()
                .stream()
                .filter(RewardPreset::isValid)
                .map(RewardPreset::deserialize)
                .toList();
    }

    public static List<String> getRewardPresetIds() {
        return getRewardPresets().stream().map(RewardPreset::rewardId).toList();
    }

    public static boolean hasRewardPreset(String rewardId) {
        return getRewardPresets().stream().anyMatch(preset -> preset.rewardId().equals(rewardId));
    }

    public static RewardPreset getRewardPreset(String rewardId) {
        return getRewardPresets().stream()
                .filter(preset -> preset.rewardId().equals(rewardId))
                .findFirst()
                .orElse(null);
    }

    public static void setRewardPreset(RewardPreset rewardPreset) {
        var original = REWARDS_CONFIG.rewardPresets.get();
        boolean exists = original.stream().anyMatch(config -> Objects.equals(config.get("rewardId"), rewardPreset.rewardId()));
        List<Config> updated = original.stream()
                .map(config -> Objects.equals(config.get("rewardId"), rewardPreset.rewardId()) ? rewardPreset.serialize() : config)
                .toList();

        if (!exists) {
            updated = new ArrayList<>(updated);
            updated.add(rewardPreset.serialize());
        }

        REWARDS_CONFIG.rewardPresets.set(updated);
        REWARDS_CONFIG.rewardPresets.save();
    }

    public static boolean createRewardPreset(String rewardId) {
        if (hasRewardPreset(rewardId)) {
            return false;
        }
        RewardPreset base = RewardPreset.deserialize(RewardPreset.getDefaultConfig().getFirst());
        setRewardPreset(new RewardPreset(rewardId, base.entries()));
        return true;
    }

    public static boolean deleteRewardPreset(String rewardId) {
        var original = REWARDS_CONFIG.rewardPresets.get();
        List<Config> filtered = original.stream()
                .filter(config -> !Objects.equals(config.get("rewardId"), rewardId))
                .map(config -> (Config) config)
                .toList();
        if (filtered.size() == original.size()) {
            return false;
        }
        REWARDS_CONFIG.rewardPresets.set(filtered);
        REWARDS_CONFIG.rewardPresets.save();
        return true;
    }

    public static List<SchedulePreset> getSchedulePresets() {
        return SCHEDULE_CONFIG.schedulePresets.get()
                .stream()
                .filter(SchedulePreset::isValid)
                .map(SchedulePreset::deserialize)
                .toList();
    }

    public static List<String> getSchedulePresetIds() {
        return getSchedulePresets().stream().map(SchedulePreset::scheduleId).toList();
    }

    public static boolean hasSchedulePreset(String scheduleId) {
        return getSchedulePresets().stream().anyMatch(preset -> preset.scheduleId().equals(scheduleId));
    }

    public static SchedulePreset getSchedulePreset(String scheduleId) {
        return getSchedulePresets().stream()
                .filter(preset -> preset.scheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);
    }

    public static void setSchedulePreset(SchedulePreset schedulePreset) {
        var originalSchedulePreset = SCHEDULE_CONFIG.schedulePresets.get();
        boolean exists = originalSchedulePreset.stream().anyMatch(config -> Objects.equals(config.get("scheduleId"), schedulePreset.scheduleId()));
        List<Config> updatedSchedulePreset = originalSchedulePreset.stream()
                .map(config -> Objects.equals(config.get("scheduleId"), schedulePreset.scheduleId()) ? schedulePreset.serialize() : config)
                .toList();

        if (!exists) {
            updatedSchedulePreset = new ArrayList<>(updatedSchedulePreset);
            updatedSchedulePreset.add(schedulePreset.serialize());
        }

        SCHEDULE_CONFIG.schedulePresets.set(updatedSchedulePreset);
        SCHEDULE_CONFIG.schedulePresets.save();
    }

    public static boolean createSchedulePreset(String scheduleId) {
        if (hasSchedulePreset(scheduleId)) {
            return false;
        }
        SchedulePreset base = SchedulePreset.deserialize(SchedulePreset.getDefaultConfig().getFirst());
        setSchedulePreset(new SchedulePreset(
                scheduleId,
                base.enabled(),
                base.baseIntervalTicks(),
                base.intervalJitterTicks(),
                base.maxStarsPerCycle(),
                base.selectionMode(),
                base.eventEntries(),
                base.conditions(),
                base.state()
        ));
        return true;
    }

    public static boolean deleteSchedulePreset(String scheduleId) {
        var original = SCHEDULE_CONFIG.schedulePresets.get();
        List<Config> filtered = original.stream()
                .filter(config -> !Objects.equals(config.get("scheduleId"), scheduleId))
                .map(config -> (Config) config)
                .toList();
        if (filtered.size() == original.size()) {
            return false;
        }
        SCHEDULE_CONFIG.schedulePresets.set(filtered);
        SCHEDULE_CONFIG.schedulePresets.save();
        return true;
    }

    public static List<VisualsPreset> getVisualsPresets() {
        return VISUALS_CONFIG.visualsPresets.get()
                .stream()
                .filter(VisualsPreset::isValid)
                .map(VisualsPreset::deserialize)
                .toList();
    }

    public static List<String> getVisualsPresetIds() {
        return getVisualsPresets().stream().map(VisualsPreset::visualsId).toList();
    }

    public static boolean hasVisualsPreset(String visualsId) {
        return getVisualsPresets().stream().anyMatch(preset -> preset.visualsId().equals(visualsId));
    }

    public static VisualsPreset getVisualsPreset(String visualsId) {
        return getVisualsPresets().stream()
                .filter(preset -> preset.visualsId().equals(visualsId))
                .findFirst()
                .orElse(null);
    }

    public static void setVisualsPreset(VisualsPreset visualsPreset) {
        var original = VISUALS_CONFIG.visualsPresets.get();
        boolean exists = original.stream().anyMatch(config -> Objects.equals(config.get("visualsId"), visualsPreset.visualsId()));
        List<Config> updated = original.stream()
                .map(config -> Objects.equals(config.get("visualsId"), visualsPreset.visualsId()) ? visualsPreset.serialize() : config)
                .toList();

        if (!exists) {
            updated = new ArrayList<>(updated);
            updated.add(visualsPreset.serialize());
        }

        VISUALS_CONFIG.visualsPresets.set(updated);
        VISUALS_CONFIG.visualsPresets.save();
    }

    public static boolean createVisualsPreset(String visualsId) {
        if (hasVisualsPreset(visualsId)) {
            return false;
        }
        VisualsPreset base = VisualsPreset.deserialize(VisualsPreset.getDefaultConfig().getFirst());
        setVisualsPreset(new VisualsPreset(
                visualsId,
                base.enabled(),
                base.particlePreset(),
                base.fallDistance(),
                base.emissionIntervalTicks(),
                base.particlesPerEmission(),
                base.impact(),
                base.travelSound()
        ));
        return true;
    }

    public static boolean deleteVisualsPreset(String visualsId) {
        var original = VISUALS_CONFIG.visualsPresets.get();
        List<Config> filtered = original.stream()
                .filter(config -> !Objects.equals(config.get("visualsId"), visualsId))
                .map(config -> (Config) config)
                .toList();
        if (filtered.size() == original.size()) {
            return false;
        }
        VISUALS_CONFIG.visualsPresets.set(filtered);
        VISUALS_CONFIG.visualsPresets.save();
        return true;
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

        List<EventEntry> eventEntries = schedulePreset.eventEntries() == null
                ? List.of()
                : schedulePreset.eventEntries().stream()
                .filter(entry -> entry != null && entry.enabled())
                .filter(entry -> entry.eventId() != null && !entry.eventId().isBlank())
                .filter(entry -> eventPresets.stream().anyMatch(preset -> preset.eventId().equals(entry.eventId())))
                .toList();

        if (eventEntries.isEmpty()) {
            return null;
        }

        SelectionMode selectionMode = schedulePreset.selectionMode();
        return switch (selectionMode) {
            case WEIGHTED -> selectWeighted(eventEntries);
            case ROTATION -> selectRotation(schedulePreset, eventEntries);
            default -> selectRandom(eventEntries);
        };
    }

    private static String selectRandom(List<EventEntry> eventEntries) {
        int index = ThreadLocalRandom.current().nextInt(eventEntries.size());
        return eventEntries.get(index).eventId();
    }

    private static String selectWeighted(List<EventEntry> eventEntries) {
        int totalWeight = 0;
        for (EventEntry entry : eventEntries) {
            totalWeight += entry.weight();
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (EventEntry entry : eventEntries) {
            cursor += Math.max(1, entry.weight());
            if (roll < cursor) {
                return entry.eventId();
            }
        }
        return eventEntries.getLast().eventId();
    }

    private static String selectRotation(SchedulePreset schedulePreset, List<EventEntry> eventEntries) {
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
