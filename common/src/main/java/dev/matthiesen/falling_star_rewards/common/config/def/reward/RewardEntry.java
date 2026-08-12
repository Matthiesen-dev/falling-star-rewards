package dev.matthiesen.falling_star_rewards.common.config.def.reward;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.Nullable;

public record RewardEntry(
        String itemId,
        int weight,
        int minCount,
        int maxCount,
        @Nullable Integer customModelData,
        @Nullable String customData
) {
    public RewardEntry(
            String itemId,
            int weight,
            int minCount,
            int maxCount
    ) {
        this(itemId, weight, minCount, maxCount, null, null);
    }

    public static boolean isValid(Config config) {
        String itemId = config.get("itemId");
        int weight = config.getInt("weight");
        int minCount = config.getInt("minCount");
        int maxCount = config.getInt("maxCount");
        return itemId != null && !itemId.isEmpty() && weight > 0 && minCount > 0 && maxCount >= minCount;
    }

    public static RewardEntry deserialize(Config config) {
        String itemId = config.get("itemId");
        int weight = config.getInt("weight");
        int minCount = config.getInt("minCount");
        int maxCount = config.getInt("maxCount");
        Integer customModelData = config.contains("customModelData") ? config.getInt("customModelData") : null;
        String customData = config.contains("customData") ? config.get("customData") : null;

        return new RewardEntry(itemId, weight, minCount, maxCount, customModelData, customData);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("itemId", itemId);
        config.set("weight", weight);
        config.set("minCount", minCount);
        config.set("maxCount", maxCount);
        if (customModelData != null) {
            config.set("customModelData", customModelData);
        }
        if (customData != null) {
            config.set("customData", customData);
        }
        return config;
    }
}