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

    public static final class Claim {
        @SerializedName("lifeTicks")
        public int lifeTicks = 20 * 45;

        @SerializedName("pickupDelayTicks")
        public int pickupDelayTicks = 10;

        @SerializedName("maxActiveDrops")
        public int maxActiveDrops = 64;
    }


    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
