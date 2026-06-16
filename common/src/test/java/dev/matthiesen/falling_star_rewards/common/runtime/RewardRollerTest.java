package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.RewardsConfig;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardRollerTest {

    @Test
    void emptyWeightsReturnNoReward() {
        RewardsConfig rewardsConfig = new RewardsConfig();
        rewardsConfig.entries = new RewardsConfig.RewardEntry[] {
                new RewardsConfig.RewardEntry("minecraft:diamond", 0, 1, 1)
        };

        RewardRoller roller = new RewardRoller();
        Optional<RolledReward> result = roller.roll(rewardsConfig, new Random(1));

        assertTrue(result.isEmpty());
    }

    @Test
    void fixedCountEntryRollsFixedCount() {
        RewardsConfig rewardsConfig = new RewardsConfig();
        rewardsConfig.entries = new RewardsConfig.RewardEntry[] {
                new RewardsConfig.RewardEntry("minecraft:diamond", 10, 2, 2)
        };

        RewardRoller roller = new RewardRoller();
        RolledReward result = roller.roll(rewardsConfig, new Random(2)).orElseThrow();

        assertEquals("minecraft:diamond", result.itemId());
        assertEquals(2, result.count());
    }

    @Test
    void invalidMinMaxIsNormalized() {
        RewardsConfig rewardsConfig = new RewardsConfig();
        rewardsConfig.entries = new RewardsConfig.RewardEntry[] {
                new RewardsConfig.RewardEntry("minecraft:emerald", 10, 0, -4)
        };

        RewardRoller roller = new RewardRoller();
        RolledReward result = roller.roll(rewardsConfig, new Random(3)).orElseThrow();

        assertEquals(1, result.count());
    }

    @Test
    void customFieldsArePreservedInRolledReward() {
        RewardsConfig rewardsConfig = new RewardsConfig();
        RewardsConfig.RewardEntry entry = new RewardsConfig.RewardEntry("minecraft:paper", 10, 1, 1);
        entry.customModelData = 12001;
        entry.customData = "{star_token:1b}";
        rewardsConfig.entries = new RewardsConfig.RewardEntry[] { entry };

        RewardRoller roller = new RewardRoller();
        RolledReward result = roller.roll(rewardsConfig, new Random(4)).orElseThrow();

        assertEquals("minecraft:paper", result.itemId());
        assertEquals(12001, result.customModelData());
        assertEquals("{star_token:1b}", result.customData());
    }
}

