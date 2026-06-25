package dev.matthiesen.falling_star_rewards.common.config.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class SchedulePresetConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("baseIntervalTicks")
    public int baseIntervalTicks = 20 * 120;

    @SerializedName("intervalJitterTicks")
    public int intervalJitterTicks = 20 * 30;

    @SerializedName("maxStarsPerCycle")
    public int maxStarsPerCycle = 1;

    @SerializedName("selectionMode")
    public String selectionMode = "random";

    @SerializedName("eventEntries")
    public List<EventEntry> eventEntries = List.of(new EventEntry());

    @SerializedName("conditions")
    public Conditions conditions = new Conditions();

    @SerializedName("state")
    public State state = new State();

    public static final class EventEntry {
        @SerializedName("eventPresetId")
        public String eventPresetId = "base";

        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("weight")
        public int weight = 1;
    }

    public static final class Conditions {
        @SerializedName("timeMode")
        public String timeMode = "any";

        @SerializedName("requireSurfaceAccess")
        public boolean requireSurfaceAccess = true;

        @SerializedName("weatherMode")
        public String weatherMode = "any";

        @SerializedName("moonPhases")
        public List<String> moonPhases = List.of();
    }

    public static final class State {
        @SerializedName("rotationCursor")
        public int rotationCursor = 0;
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
