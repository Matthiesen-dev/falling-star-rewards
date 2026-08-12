package dev.matthiesen.falling_star_rewards.common.config.def;

import com.electronwill.nightconfig.core.Config;

import java.util.ArrayList;
import java.util.List;

public record VisualsPreset(
        String visualsId,
        boolean enabled,
        ParticlePreset particlePreset,
        int fallDistance,
        int emissionIntervalTicks,
        int particlesPerEmission,
        Impact impact,
        TravelSound travelSound
) {

    public static List<Config> getDefaultConfig() {
        List<VisualsPreset> presets = new ArrayList<>();

        Impact baseImpact = new Impact(
                true,
                ParticlePreset.FIREWORK,
                13,
                0.4D,
                true,
                "minecraft:entity.firework_rocket.twinkle",
                0.8F,
                0.9F,
                1.2F
        );

        TravelSound baseTravelSound = new TravelSound(
                true,
                "minecraft:entity.phantom.flap",
                0.12F,
                1.3F,
                1.7F,
                12
        );

        presets.add(new VisualsPreset(
                "base",
                true,
                ParticlePreset.END_ROD,
                10,
                3,
                2,
                baseImpact,
                baseTravelSound
        ));

        return presets.stream()
                .map(VisualsPreset::serialize)
                .toList();
    }

    public static boolean isValid(Object object) {
        if (!(object instanceof Config config)) {
            return false;
        }

        String visualsId = config.get("visualsId");
        ParticlePreset particlePreset = config.getEnum("particlePreset", ParticlePreset.class);
        int fallDistance = config.get("fallDistance");
        int emissionIntervalTicks = config.get("emissionIntervalTicks");
        int particlesPerEmission = config.get("particlesPerEmission");
        Config impactConfig = config.get("impact");
        Config travelSoundConfig = config.get("travelSound");

        return visualsId != null && !visualsId.isEmpty()
                && particlePreset != null
                && fallDistance >= 0
                && emissionIntervalTicks >= 0
                && particlesPerEmission >= 0
                && impactConfig != null && Impact.isValid(impactConfig)
                && travelSoundConfig != null && TravelSound.isValid(travelSoundConfig);
    }

    public static VisualsPreset deserialize(Config config) {
        String visualsId = config.get("visualsId");
        boolean enabled = config.get("enabled");
        ParticlePreset particlePreset = config.getEnum("particlePreset", ParticlePreset.class);
        int fallDistance = config.get("fallDistance");
        int emissionIntervalTicks = config.get("emissionIntervalTicks");
        int particlesPerEmission = config.get("particlesPerEmission");
        Config impactConfig = config.get("impact");
        Config travelSoundConfig = config.get("travelSound");

        Impact impact = Impact.deserialize(impactConfig);
        TravelSound travelSound = TravelSound.deserialize(travelSoundConfig);

        return new VisualsPreset(visualsId, enabled, particlePreset, fallDistance, emissionIntervalTicks, particlesPerEmission, impact, travelSound);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("visualsId", visualsId);
        config.set("enabled", enabled);
        config.set("particlePreset", particlePreset.name());
        config.set("fallDistance", fallDistance);
        config.set("emissionIntervalTicks", emissionIntervalTicks);
        config.set("particlesPerEmission", particlesPerEmission);
        config.set("impact", impact.serialize());
        config.set("travelSound", travelSound.serialize());
        return config;
    }

    public enum ParticlePreset {
        ASH,
        GLOW,
        FIREWORK,
        END_ROD
    }

    public record TravelSound(
            boolean enabled,
            String id,
            float volume,
            float pitchMin,
            float pitchMax,
            int intervalTicks
    ) {

        public static boolean isValid(Config config) {
            String id = config.get("id");
            double volume = config.get("volume");
            double pitchMin = config.get("pitchMin");
            double pitchMax = config.get("pitchMax");
            int intervalTicks = config.get("intervalTicks");

            return id != null && !id.isEmpty()
                    && volume >= 0.0
                    && pitchMin >= 0.0
                    && pitchMax >= 0.0
                    && intervalTicks >= 0;
        }

        public static TravelSound deserialize(Config config) {
            boolean enabled = config.get("enabled");
            String id = config.get("id");
            double rawVolume = config.get("volume");
            double rawPitchMin = config.get("pitchMin");
            double rawPitchMax = config.get("pitchMax");
            int intervalTicks = config.get("intervalTicks");

            float volume = (float) rawVolume;
            float pitchMin = (float) rawPitchMin;
            float pitchMax = (float) rawPitchMax;

            return new TravelSound(enabled, id, volume, pitchMin, pitchMax, intervalTicks);
        }

        public Config serialize() {
            Config config = Config.inMemory();
            config.set("enabled", enabled);
            config.set("id", id);
            config.set("volume", volume);
            config.set("pitchMin", pitchMin);
            config.set("pitchMax", pitchMax);
            config.set("intervalTicks", intervalTicks);
            return config;
        }
    }

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
}
