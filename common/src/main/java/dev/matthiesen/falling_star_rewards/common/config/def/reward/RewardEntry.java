package dev.matthiesen.falling_star_rewards.common.config.def.reward;

import com.electronwill.nightconfig.core.Config;

public record RewardEntry(
        String itemId,
        int weight,
        int minCount,
        int maxCount
) {

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
        return new RewardEntry(itemId, weight, minCount, maxCount);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("itemId", itemId);
        config.set("weight", weight);
        config.set("minCount", minCount);
        config.set("maxCount", maxCount);
        return config;
    }
}