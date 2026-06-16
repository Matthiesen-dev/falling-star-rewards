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
        assertEquals("ash", config.visuals.particlePreset);
        assertTrue(config.visuals.impactBurstEnabled);
        assertEquals("firework", config.visuals.impactParticlePreset);
        assertTrue(config.visuals.impactSoundEnabled);
        assertEquals("minecraft:entity.firework_rocket.twinkle", config.visuals.impactSoundId);
        assertTrue(config.visuals.travelSoundEnabled);
        assertEquals("minecraft:entity.phantom.flap", config.visuals.travelSoundId);
        assertEquals(3, config.rewards.entries.length);
        assertEquals("minecraft:nether_star", config.rewards.entries[2].id);
    }

    @Test
    void gsonRoundTripKeepsSchedulerValues() {
        MainConfig config = new MainConfig();
        config.scheduler.baseIntervalTicks = 4000;
        config.scheduler.intervalJitterTicks = 200;
        config.claim.maxActiveDrops = 40;
        config.visuals.particlePreset = "ash";
        config.visuals.impactParticlePreset = "glow";
        config.visuals.impactParticleCount = 20;
        config.visuals.impactSoundId = "minecraft:entity.experience_orb.pickup";
        config.visuals.impactSoundVolume = 0.6F;
        config.visuals.travelSoundEnabled = true;
        config.visuals.travelSoundId = "minecraft:entity.allay.ambient_with_item";
        config.rewards.entries[0].customModelData = 12001;
        config.rewards.entries[0].customData = "{star_token:1b}";

        String json = MainConfig.GSON.toJson(config);
        MainConfig decoded = MainConfig.GSON.fromJson(json, MainConfig.class);

        assertEquals(4000, decoded.scheduler.baseIntervalTicks);
        assertEquals(200, decoded.scheduler.intervalJitterTicks);
        assertEquals(40, decoded.claim.maxActiveDrops);
        assertEquals("ash", decoded.visuals.particlePreset);
        assertEquals("glow", decoded.visuals.impactParticlePreset);
        assertEquals(20, decoded.visuals.impactParticleCount);
        assertEquals("minecraft:entity.experience_orb.pickup", decoded.visuals.impactSoundId);
        assertEquals(0.6F, decoded.visuals.impactSoundVolume);
        assertTrue(decoded.visuals.travelSoundEnabled);
        assertEquals("minecraft:entity.allay.ambient_with_item", decoded.visuals.travelSoundId);
        assertEquals(12001, decoded.rewards.entries[0].customModelData);
        assertEquals("{star_token:1b}", decoded.rewards.entries[0].customData);
    }
}

