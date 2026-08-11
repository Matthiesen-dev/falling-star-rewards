package dev.matthiesen.falling_star_rewards.common.config.def;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public record RewardPreset(
        String rewardId,
        List<RewardEntry> entries
) {

    public static List<Config> getDefaultConfig() {
        Config config = Config.inMemory();
        config.set("rewardId", "base");
        List<Config> entriesConfig = List.of(
                new RewardEntry("minecraft:amethyst_shard", 20, 1, 3).serialize(),
                new RewardEntry("minecraft:glowstone_dust", 12, 2, 5).serialize(),
                new RewardEntry("minecraft:nether_star", 1, 1, 1).serialize()
        );
        config.set("entries", entriesConfig);
        return List.of(config);
    }

    public static boolean isValid(Object object) {
        if (!(object instanceof Config config)) {
            return false;
        }
        String rewardId = config.get("rewardId");
        List<Config> entriesConfig = config.get("entries");
        if (rewardId == null || rewardId.isEmpty() || entriesConfig == null) {
            return false;
        }
        for (Config entryConfig : entriesConfig) {
            if (!RewardEntry.isValid(entryConfig)) {
                return false;
            }
        }
        return true;
    }

    public static RewardPreset deserialize(Config config) {
        String rewardId = config.get("rewardId");
        List<Config> entriesConfig = config.get("entries");
        List<RewardEntry> entries = entriesConfig.stream()
                .map(RewardEntry::deserialize)
                .toList();
        return new RewardPreset(rewardId, entries);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("rewardId", rewardId);
        List<Config> entriesConfig = entries.stream()
                .map(RewardEntry::serialize)
                .toList();
        config.set("entries", entriesConfig);
        return config;
    }

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
}
