package dev.matthiesen.falling_star_rewards.common.config.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class RewardsPresetConfig {
    @SerializedName("entries")
    public RewardEntry[] entries = new RewardEntry[] {
            new RewardEntry("minecraft:amethyst_shard", 20, 1, 3),
            new RewardEntry("minecraft:glowstone_dust", 12, 2, 5),
            new RewardEntry("minecraft:nether_star", 1, 1, 1)
    };

    public static final class RewardEntry {
        @SerializedName("id")
        public String id;

        @SerializedName("weight")
        public int weight;

        @SerializedName("minCount")
        public int minCount;

        @SerializedName("maxCount")
        public int maxCount;

        @SerializedName("customModelData")
        public Integer customModelData;

        @SerializedName("customData")
        public String customData;

        @SuppressWarnings("unused")
        public RewardEntry() {
            this("minecraft:amethyst_shard", 1, 1, 1);
        }

        public RewardEntry(String id, int weight, int minCount, int maxCount) {
            this.id = id;
            this.weight = weight;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.customModelData = null;
            this.customData = null;
        }
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
