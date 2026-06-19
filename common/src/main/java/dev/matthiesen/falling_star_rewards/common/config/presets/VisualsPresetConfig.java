package dev.matthiesen.falling_star_rewards.common.config.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class VisualsPresetConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("particlePreset")
    public String particlePreset = "end_rod";

    @SerializedName("fallDistance")
    public int fallDistance = 10;

    @SerializedName("emissionIntervalTicks")
    public int emissionIntervalTicks = 3;

    @SerializedName("particlesPerEmission")
    public int particlesPerEmission = 2;

    @SerializedName("impact")
    public Impact impact = new Impact();

    @SerializedName("travelSound")
    public TravelSound travelSound = new TravelSound();

    public static class TravelSound {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("id")
        public String id = "minecraft:entity.phantom.flap";

        @SerializedName("volume")
        public float volume = 0.12F;

        @SerializedName("pitchMin")
        public float pitchMin = 1.3F;

        @SerializedName("pitchMax")
        public float pitchMax = 1.7F;

        @SerializedName("intervalTicks")
        public int intervalTicks = 12;
    }

    public static class Impact {
        @SerializedName("burstEnabled")
        public boolean burstEnabled = true;

        @SerializedName("particlePreset")
        public String particlePreset = "firework";

        @SerializedName("particleCount")
        public int particleCount = 14;

        @SerializedName("spread")
        public double spread = 0.4D;

        @SerializedName("soundEnabled")
        public boolean soundEnabled = true;

        @SerializedName("soundId")
        public String soundId = "minecraft:entity.firework_rocket.twinkle";

        @SerializedName("soundVolume")
        public float soundVolume = 0.8F;

        @SerializedName("soundPitchMin")
        public float soundPitchMin = 0.9F;

        @SerializedName("soundPitchMax")
        public float soundPitchMax = 1.2F;
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
