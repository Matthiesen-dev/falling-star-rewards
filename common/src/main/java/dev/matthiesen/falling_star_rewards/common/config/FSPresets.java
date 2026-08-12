package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.EventPreset;
import dev.matthiesen.falling_star_rewards.common.config.def.RewardPreset;
import dev.matthiesen.falling_star_rewards.common.config.def.SchedulePreset;
import dev.matthiesen.falling_star_rewards.common.config.def.VisualsPreset;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class FSPresets {

    public static final class EventsConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> eventPresets;

        public EventsConfig(ModConfigSpec.Builder builder) {
            builder.push("events");
            eventPresets = builder
                    .comment(
                            "List of event presets. Each preset defines the configuration for a specific event.",
                            "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#events-presets"
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

    public static final class RewardsConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> rewardPresets;

        public RewardsConfig(ModConfigSpec.Builder builder) {
            builder.push("rewards");
            rewardPresets = builder
                    .comment(
                            "List of reward presets. Each preset defines the configuration for a specific reward.",
                            "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#rewards-presets"
                    )
                    .defineList(
                            List.of("rewardPresets"),
                            RewardPreset::getDefaultConfig,
                            null,
                            o -> o instanceof Config
                    );
            builder.pop();
        }
    }

    public static final class SchedulesConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> schedulePresets;

        public SchedulesConfig(ModConfigSpec.Builder builder) {
            builder.push("schedules");
            schedulePresets = builder
                    .comment(
                            "List of schedule presets. Each preset defines the configuration for a specific schedule.",
                            "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#scheduling-presets"
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

    public static final class VisualsConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> visualsPresets;

        public VisualsConfig(ModConfigSpec.Builder builder) {
            builder.push("visuals");
            visualsPresets = builder
                    .comment(
                            "List of Visuals Presets. Each preset defines the visuals for a falling star event.",
                            "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#visuals-presets"
                    )
                    .defineList(
                            List.of("visualsPresets"),
                            VisualsPreset::getDefaultConfig,
                            null,
                            o -> o instanceof Config
                    );
            builder.pop();
        }
    }

}
