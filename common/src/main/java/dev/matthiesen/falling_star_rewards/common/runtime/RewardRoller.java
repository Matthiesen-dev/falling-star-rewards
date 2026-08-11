package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.def.RewardPreset;
import dev.matthiesen.falling_star_rewards.common.interfaces.RolledReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class RewardRoller {

    public Optional<RolledReward> roll(RewardPreset config) {
        return roll(config, ThreadLocalRandom.current());
    }

    public Optional<RolledReward> roll(RewardPreset config, RandomGenerator random) {
        List<RewardPreset.RewardEntry> candidates = new ArrayList<>();
        int totalWeight = 0;

        for (var entry : config.entries()) {
            if (entry == null || entry.itemId() == null || entry.itemId().isBlank()) {
                continue;
            }

            int weight = Math.max(0, entry.weight());
            if (weight == 0) {
                continue;
            }

            candidates.add(entry);
            totalWeight += weight;
        }

        if (candidates.isEmpty() || totalWeight <= 0) {
            return Optional.empty();
        }

        int rolledWeight = random.nextInt(totalWeight);
        int cursor = 0;
        for (var candidate : candidates) {
            cursor += Math.max(0, candidate.weight());
            if (rolledWeight < cursor) {
                int min = Math.max(1, candidate.minCount());
                int max = Math.max(min, candidate.maxCount());
                int count = min + random.nextInt((max - min) + 1);
                return Optional.of(new RolledReward(
                        candidate.itemId(),
                        count,
                        candidate.customModelData(),
                        candidate.customData()
                ));
            }
        }

        return Optional.empty();
    }
}

