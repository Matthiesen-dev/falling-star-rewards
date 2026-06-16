package dev.matthiesen.falling_star_rewards.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualsConfigTest {

    @Test
    void defaultsIncludeParticleAndSoundSettings() {
        VisualsConfig visuals = new VisualsConfig();

        assertTrue(visuals.enabled);
        assertEquals("ash", visuals.particlePreset);
        assertTrue(visuals.impactBurstEnabled);
        assertEquals("firework", visuals.impactParticlePreset);
        assertTrue(visuals.impactSoundEnabled);
        assertEquals("minecraft:entity.firework_rocket.twinkle", visuals.impactSoundId);
        assertTrue(visuals.travelSoundEnabled);
        assertEquals("minecraft:entity.phantom.flap", visuals.travelSoundId);
    }

    @Test
    void gsonRoundTripKeepsVisualOverrides() {
        VisualsConfig visuals = new VisualsConfig();
        visuals.particlePreset = "glow";
        visuals.impactParticleCount = 22;
        visuals.impactSoundId = "minecraft:entity.experience_orb.pickup";
        visuals.travelSoundEnabled = false;

        String json = VisualsConfig.GSON.toJson(visuals);
        VisualsConfig decoded = VisualsConfig.GSON.fromJson(json, VisualsConfig.class);

        assertEquals("glow", decoded.particlePreset);
        assertEquals(22, decoded.impactParticleCount);
        assertEquals("minecraft:entity.experience_orb.pickup", decoded.impactSoundId);
        assertFalse(decoded.travelSoundEnabled);
    }
}


