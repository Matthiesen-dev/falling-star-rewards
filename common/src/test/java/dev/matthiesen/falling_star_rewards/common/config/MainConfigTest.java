package dev.matthiesen.falling_star_rewards.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainConfigTest {

    @Test
    void defaultsIncludeRewardEntries() {
        MainConfig config = new MainConfig();

        assertTrue(config.enabled);
        assertEquals(64, config.claim.maxActiveDrops);
        assertTrue(config.activation.requireNight);
        assertEquals("per_player", config.spawn.targetScope);
    }

    @Test
    void gsonRoundTripKeepsSchedulerValues() {
        MainConfig config = new MainConfig();
        config.scheduler.baseIntervalTicks = 4000;
        config.scheduler.intervalJitterTicks = 200;
        config.claim.maxActiveDrops = 40;
        config.spawn.maxLocationAttempts = 7;
        config.activation.weatherMode = "thunder";

        String json = MainConfig.GSON.toJson(config);
        MainConfig decoded = MainConfig.GSON.fromJson(json, MainConfig.class);

        assertEquals(4000, decoded.scheduler.baseIntervalTicks);
        assertEquals(200, decoded.scheduler.intervalJitterTicks);
        assertEquals(40, decoded.claim.maxActiveDrops);
        assertEquals(7, decoded.spawn.maxLocationAttempts);
        assertEquals("thunder", decoded.activation.weatherMode);
    }
}

