package dev.matthiesen.falling_star_rewards.common.config.def.visuals;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.interfaces.ParticlePreset;

public record Impact(
        boolean burstEnabled,
        ParticlePreset particlePreset,
        int particleCount,
        double spread,
        boolean soundEnabled,
        String soundId,
        float soundVolume,
        float soundPitchMin,
        float soundPitchMax
) {

    public static boolean isValid(Config config) {
        ParticlePreset particlePreset = config.getEnum("particlePreset", ParticlePreset.class);
        String soundId = config.get("soundId");

        double rawSoundVolume = config.get("soundVolume");
        double rawSoundPitchMin = config.get("soundPitchMin");
        double rawSoundPitchMax = config.get("soundPitchMax");

        float soundVolume = (float) rawSoundVolume;
        float soundPitchMin = (float) rawSoundPitchMin;
        float soundPitchMax = (float) rawSoundPitchMax;

        return particlePreset != null
                && soundId != null && !soundId.isEmpty()
                && soundVolume >= 0.0f
                && soundPitchMin >= 0.0f
                && soundPitchMax >= 0.0f;
    }

    public static Impact deserialize(Config config) {
        boolean burstEnabled = config.get("burstEnabled");
        ParticlePreset particlePreset = config.getEnum("particlePreset", ParticlePreset.class);
        int particleCount = config.get("particleCount");
        double spread = config.get("spread");
        boolean soundEnabled = config.get("soundEnabled");
        String soundId = config.get("soundId");
        double rawSoundVolume = config.get("soundVolume");
        double rawSoundPitchMin = config.get("soundPitchMin");
        double rawSoundPitchMax = config.get("soundPitchMax");
        float soundVolume = (float) rawSoundVolume;
        float soundPitchMin = (float) rawSoundPitchMin;
        float soundPitchMax = (float) rawSoundPitchMax;
        return new Impact(burstEnabled, particlePreset, particleCount, spread, soundEnabled, soundId, soundVolume, soundPitchMin, soundPitchMax);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("burstEnabled", burstEnabled);
        config.set("particlePreset", particlePreset.name());
        config.set("particleCount", particleCount);
        config.set("spread", spread);
        config.set("soundEnabled", soundEnabled);
        config.set("soundId", soundId);
        config.set("soundVolume", soundVolume);
        config.set("soundPitchMin", soundPitchMin);
        config.set("soundPitchMax", soundPitchMax);
        return config;
    }
}