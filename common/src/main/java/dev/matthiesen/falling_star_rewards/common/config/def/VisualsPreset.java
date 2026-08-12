package dev.matthiesen.falling_star_rewards.common.config.def;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.visuals.Impact;
import dev.matthiesen.falling_star_rewards.common.config.def.visuals.TravelSound;
import dev.matthiesen.falling_star_rewards.common.interfaces.ParticlePreset;

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
}
