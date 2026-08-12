package dev.matthiesen.falling_star_rewards.common.config.def.visuals;

import com.electronwill.nightconfig.core.Config;

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