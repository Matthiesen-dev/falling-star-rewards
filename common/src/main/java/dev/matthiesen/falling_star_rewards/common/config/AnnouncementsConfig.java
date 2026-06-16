package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class AnnouncementsConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("scope")
    public String scope = "nearby";

    @SerializedName("spawnMessage")
    public String spawnMessage = "A falling star has appeared nearby!";

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
