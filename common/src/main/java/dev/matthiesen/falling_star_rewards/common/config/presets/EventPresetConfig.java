package dev.matthiesen.falling_star_rewards.common.config.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class EventPresetConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("rewardsPresetId")
    public String rewardsPresetId = "base";

    @SerializedName("visualsPresetId")
    public String visualsPresetId = "base";

    @SerializedName("spawn")
    public Spawn spawn = new Spawn();

    @SerializedName("announcement")
    public Announcement announcement = new Announcement();

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

    public static final class Announcement {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("scope")
        public String scope = "nearby";

        @SerializedName("useActionBar")
        public boolean useActionBar = false;

        public List<String> messages = List.of(
                "A falling star has appeared nearby!",
                "A falling star has appeared in the sky!",
                "A falling star has appeared in the world!"
        );
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
