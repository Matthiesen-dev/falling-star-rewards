package dev.matthiesen.falling_star_rewards.common.config.def;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.schedule.Conditions;
import dev.matthiesen.falling_star_rewards.common.config.def.schedule.EventEntry;
import dev.matthiesen.falling_star_rewards.common.interfaces.SelectionMode;
import dev.matthiesen.falling_star_rewards.common.interfaces.TimeMode;
import dev.matthiesen.falling_star_rewards.common.interfaces.WeatherMode;

import java.util.List;

@SuppressWarnings("unused")
public record SchedulePreset(
        String scheduleId,
        boolean enabled,
        int baseIntervalTicks,
        int intervalJitterTicks,
        int maxStarsPerCycle,
        SelectionMode selectionMode,
        List<EventEntry> eventEntries,
        Conditions conditions,
        State state
) {

    public static List<Config> getDefaultConfig() {
        List<SchedulePreset> presets = List.of(
                new SchedulePreset(
                        "base",
                        true,
                        20 * 120,
                        20 * 30,
                        1,
                        SelectionMode.RANDOM,
                        List.of(
                                new EventEntry("base", true, 1)
                        ),
                        new Conditions(TimeMode.ANY, true, WeatherMode.ANY, List.of()),
                        new State()
                )
        );

        return presets.stream()
                .map(SchedulePreset::serialize)
                .toList();
    }

    public static boolean isValid(Object object) {
        if (!(object instanceof Config config)) {
            return false;
        }

        String scheduleId = config.get("scheduleId");
        int baseIntervalTicks = config.getInt("baseIntervalTicks");
        int intervalJitterTicks = config.getInt("intervalJitterTicks");
        int maxStarsPerCycle = config.getInt("maxStarsPerCycle");
        SelectionMode selectionMode = config.getEnum("selectionMode", SelectionMode.class);
        List<Config> eventEntriesConfig = config.get("eventEntries");
        Config conditionsConfig = config.get("conditions");
        Config stateConfig = config.get("state");

        return scheduleId != null && !scheduleId.isEmpty()
                && baseIntervalTicks > 0
                && intervalJitterTicks >= 0
                && maxStarsPerCycle > 0
                && selectionMode != null
                && eventEntriesConfig != null
                && conditionsConfig != null
                && stateConfig != null;
    }

    public static SchedulePreset deserialize(Config config) {
        String scheduleId = config.get("scheduleId");
        boolean enabled = config.get("enabled");
        int baseIntervalTicks = config.getInt("baseIntervalTicks");
        int intervalJitterTicks = config.getInt("intervalJitterTicks");
        int maxStarsPerCycle = config.getInt("maxStarsPerCycle");
        SelectionMode selectionMode = config.getEnum("selectionMode", SelectionMode.class);
        List<Config> eventEntriesConfig = config.get("eventEntries");
        List<EventEntry> eventEntries = eventEntriesConfig.stream()
                .map(EventEntry::deserialize)
                .toList();
        Config conditionsConfig = config.get("conditions");
        Conditions conditions = Conditions.deserialize(conditionsConfig);
        Config stateConfig = config.get("state");
        State state = State.deserialize(stateConfig);

        return new SchedulePreset(
                scheduleId,
                enabled,
                baseIntervalTicks,
                intervalJitterTicks,
                maxStarsPerCycle,
                selectionMode,
                eventEntries,
                conditions,
                state
        );
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("scheduleId", scheduleId);
        config.set("enabled", enabled);
        config.set("baseIntervalTicks", baseIntervalTicks);
        config.set("intervalJitterTicks", intervalJitterTicks);
        config.set("maxStarsPerCycle", maxStarsPerCycle);
        config.set("selectionMode", selectionMode);
        config.set("eventEntries", eventEntries.stream().map(EventEntry::serialize).toList());
        config.set("conditions", conditions.serialize());
        config.set("state", state.serialize());
        return config;
    }

    public static class State {
        public int rotationCursor = 0;

        public static State deserialize(Config config) {
            State state = new State();
            state.rotationCursor = config.getInt("rotationCursor");
            return state;
        }

        public Config serialize() {
            Config config = Config.inMemory();
            config.set("rotationCursor", rotationCursor);
            return config;
        }
    }
}
