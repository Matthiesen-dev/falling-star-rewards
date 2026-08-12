package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.EventPreset;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class EventsServerConfig {

    public ModConfigSpec.ConfigValue<List<? extends Config>> eventPresets;

    public EventsServerConfig(ModConfigSpec.Builder builder) {
        builder.push("events");

        eventPresets = builder
                .comment(
                        "List of event presets. Each preset defines the configuration for a specific event."
                )
                .defineList(
                        List.of("eventPresets"),
                        EventPreset::getDefaultConfig,
                        null,
                        o -> o instanceof Config
                );

        builder.pop();
    }
}
