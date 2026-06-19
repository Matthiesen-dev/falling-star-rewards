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

    @SerializedName("activation")
    public Activation activation = new Activation();

    @SerializedName("spawn")
    public Spawn spawn = new Spawn();

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

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
