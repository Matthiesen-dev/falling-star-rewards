package dev.matthiesen.falling_star_rewards.common.config.def.schedule;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.interfaces.TimeMode;
import dev.matthiesen.falling_star_rewards.common.interfaces.WeatherMode;

import java.util.List;

@SuppressWarnings("unused")
public record Conditions(
        TimeMode timeMode,
        boolean requireSurfaceAccess,
        WeatherMode weatherMode,
        List<String> moonPhases
) {
    public static boolean isValid(Config config) {
        TimeMode timeMode = config.getEnum("timeMode", TimeMode.class);
        boolean requireSurfaceAccess = config.get("requireSurfaceAccess");
        WeatherMode weatherMode = config.getEnum("weatherMode", WeatherMode.class);
        List<String> moonPhases = config.get("moonPhases");

        return timeMode != null
                && weatherMode != null
                && moonPhases != null;
    }

    public static Conditions deserialize(Config config) {
        TimeMode timeMode = config.getEnum("timeMode", TimeMode.class);
        boolean requireSurfaceAccess = config.get("requireSurfaceAccess");
        WeatherMode weatherMode = config.getEnum("weatherMode", WeatherMode.class);
        List<String> moonPhases = config.get("moonPhases");

        return new Conditions(timeMode, requireSurfaceAccess, weatherMode, moonPhases);
    }

    public Config serialize() {
        Config config = Config.inMemory();
        config.set("timeMode", timeMode);
        config.set("requireSurfaceAccess", requireSurfaceAccess);
        config.set("weatherMode", weatherMode);
        config.set("moonPhases", moonPhases);
        return config;
    }
}