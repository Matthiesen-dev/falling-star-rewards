package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class MainConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("enablePresetGeneration")
    public boolean enablePresetGeneration = true;

    @SerializedName("enabledSchedules")
    public List<String> enabledSchedules = List.of("base");

    @SerializedName("claim")
    public Claim claim = new Claim();

    // TODO Remove this once the new schedules system is in place
    @SerializedName("scheduler")
    public Scheduler scheduler = new Scheduler();

    public static final class Claim {
        @SerializedName("lifeTicks")
        public int lifeTicks = 20 * 45;

        @SerializedName("pickupDelayTicks")
        public int pickupDelayTicks = 10;

        @SerializedName("maxActiveDrops")
        public int maxActiveDrops = 64;
    }

    // TODO Remove this once the new schedules system is in place
    public static final class Scheduler {
        @SerializedName("baseIntervalTicks")
        public int baseIntervalTicks = 20 * 120;

        @SerializedName("intervalJitterTicks")
        public int intervalJitterTicks = 20 * 30;

        @SerializedName("maxStarsPerCycle")
        public int maxStarsPerCycle = 1;
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
