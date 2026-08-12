package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.SchedulePreset;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ScheduleServerConfig {

    public ModConfigSpec.ConfigValue<List<? extends Config>> schedulePresets;

    public ScheduleServerConfig(ModConfigSpec.Builder builder) {
        builder.push("schedules");

        schedulePresets = builder
                .comment(
                        "List of schedule presets. Each preset defines the configuration for a specific schedule."
                )
                .defineList(
                        List.of("schedulePresets"),
                        SchedulePreset::getDefaultConfig,
                        null,
                        o -> o instanceof Config
                );

        builder.pop();
    }
}
