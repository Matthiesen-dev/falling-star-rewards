package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class VisualsConfig {
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

    @SerializedName("impactBurstEnabled")
    public boolean impactBurstEnabled = true;

    @SerializedName("impactParticlePreset")
    public String impactParticlePreset = "firework";

    @SerializedName("impactParticleCount")
    public int impactParticleCount = 14;

    @SerializedName("impactSpread")
    public double impactSpread = 0.4D;

    @SerializedName("impactSoundEnabled")
    public boolean impactSoundEnabled = true;

    @SerializedName("impactSoundId")
    public String impactSoundId = "minecraft:entity.firework_rocket.twinkle";

    @SerializedName("impactSoundVolume")
    public float impactSoundVolume = 0.8F;

    @SerializedName("impactSoundPitchMin")
    public float impactSoundPitchMin = 0.9F;

    @SerializedName("impactSoundPitchMax")
    public float impactSoundPitchMax = 1.2F;

    @SerializedName("travelSoundEnabled")
    public boolean travelSoundEnabled = true;

    @SerializedName("travelSoundId")
    public String travelSoundId = "minecraft:entity.phantom.flap";

    @SerializedName("travelSoundVolume")
    public float travelSoundVolume = 0.12F;

    @SerializedName("travelSoundPitchMin")
    public float travelSoundPitchMin = 1.3F;

    @SerializedName("travelSoundPitchMax")
    public float travelSoundPitchMax = 1.7F;

    @SerializedName("travelSoundIntervalTicks")
    public int travelSoundIntervalTicks = 12;

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
