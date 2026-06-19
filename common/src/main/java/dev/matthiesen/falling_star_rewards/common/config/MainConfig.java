package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class MainConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("enablePresetGeneration")
    public boolean enablePresetGeneration = true;

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
