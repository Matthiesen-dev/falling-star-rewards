package dev.matthiesen.falling_star_rewards.common.config.def.schedule;

import com.electronwill.nightconfig.core.Config;

@SuppressWarnings("unused")
public record EventEntry(
        String eventId,
        boolean enabled,
        int weight
) {

    public static boolean isValid(Config config) {
        String eventId = config.get("eventId");
        boolean enabled = config.get("enabled");
        int weight = config.getInt("weight");

        return eventId != null && !eventId.isEmpty() && weight > 0;
    }

    public static EventEntry deserialize(Config config) {
        String eventId = config.get("eventId");
        boolean enabled = config.get("enabled");
        int weight = config.getInt("weight");
        return new EventEntry(eventId, enabled, weight);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("eventId", eventId);
        config.set("enabled", enabled);
        config.set("weight", weight);
        return config;
    }
}