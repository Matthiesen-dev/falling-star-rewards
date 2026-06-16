package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardRollerTest {

    @Test
    void emptyWeightsReturnNoReward() {
        MainConfig config = new MainConfig();
        config.rewards.entries = new MainConfig.RewardEntry[] {
                new MainConfig.RewardEntry("minecraft:diamond", 0, 1, 1)
        };

        RewardRoller roller = new RewardRoller();
        Optional<RolledReward> result = roller.roll(config, new Random(1));

        assertTrue(result.isEmpty());
    }

    @Test
    void fixedCountEntryRollsFixedCount() {
        MainConfig config = new MainConfig();
        config.rewards.entries = new MainConfig.RewardEntry[] {
                new MainConfig.RewardEntry("minecraft:diamond", 10, 2, 2)
        };

        RewardRoller roller = new RewardRoller();
        RolledReward result = roller.roll(config, new Random(2)).orElseThrow();

        assertEquals("minecraft:diamond", result.itemId());
        assertEquals(2, result.count());
    }

    @Test
    void invalidMinMaxIsNormalized() {
        MainConfig config = new MainConfig();
        config.rewards.entries = new MainConfig.RewardEntry[] {
                new MainConfig.RewardEntry("minecraft:emerald", 10, 0, -4)
        };

        RewardRoller roller = new RewardRoller();
        RolledReward result = roller.roll(config, new Random(3)).orElseThrow();

        assertEquals(1, result.count());
    }

    @Test
    void customFieldsArePreservedInRolledReward() {
        MainConfig config = new MainConfig();
        MainConfig.RewardEntry entry = new MainConfig.RewardEntry("minecraft:paper", 10, 1, 1);
        entry.customModelData = 12001;
        entry.customData = "{star_token:1b}";
        config.rewards.entries = new MainConfig.RewardEntry[] { entry };

        RewardRoller roller = new RewardRoller();
        RolledReward result = roller.roll(config, new Random(4)).orElseThrow();

        assertEquals("minecraft:paper", result.itemId());
        assertEquals(12001, result.customModelData());
        assertEquals("{star_token:1b}", result.customData());
    }
}

