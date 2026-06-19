package dev.matthiesen.falling_star_rewards.common.config.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class EventPresetConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("rewardsPresetId")
    public String rewardsPresetId = "base";

    @SerializedName("visualsPresetId")
    public String visualsPresetId = "base";

    @SerializedName("scheduler")
    public Scheduler scheduler = new Scheduler();

    @SerializedName("activation")
    public Activation activation = new Activation();

    @SerializedName("spawn")
    public Spawn spawn = new Spawn();

    @SerializedName("claim")
    public Claim claim = new Claim();

    public static final class Activation {
        @SerializedName("requireNight")
        public boolean requireNight = true;

        @SerializedName("requireSurfaceAccess")
        public boolean requireSurfaceAccess = true;

        @SerializedName("weatherMode")
        public String weatherMode = "any";
    }

    public static final class Spawn {
        @SerializedName("targetScope")
        public String targetScope = "per_player";

        @SerializedName("minRadius")
        public int minRadius = 16;

        @SerializedName("maxRadius")
        public int maxRadius = 48;

        @SerializedName("maxLocationAttempts")
        public int maxLocationAttempts = 12;

        @SerializedName("allowWaterSpawns")
        public boolean allowWaterSpawns = false;
    }

    public static final class Claim {
        @SerializedName("lifeTicks")
        public int lifeTicks = 20 * 45;

        @SerializedName("pickupDelayTicks")
        public int pickupDelayTicks = 10;

        @SerializedName("maxActiveDrops")
        public int maxActiveDrops = 64;
    }

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
