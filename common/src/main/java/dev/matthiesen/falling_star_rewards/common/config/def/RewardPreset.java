package dev.matthiesen.falling_star_rewards.common.config.def;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.reward.RewardEntry;

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
}
