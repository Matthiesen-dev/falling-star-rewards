package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.*;
import dev.matthiesen.falling_star_rewards.common.interfaces.ParticlePreset;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FSPresets {
    public static abstract class BaseConfig {
        public ModConfigSpec.IntValue configVersion;

        protected abstract void configure(ModConfigSpec.Builder builder);

        public BaseConfig(ModConfigSpec.Builder builder, String configName, @NotNull String... comments) {
            builder.comment(comments).push(configName);

            configVersion = builder
                    .comment(
                            "The version of the configuration. This is used to determine if the configuration needs to be updated.",
                            "Do not change this value unless you know what you are doing."
                    )
                    .defineInRange("configVersion", 1, 1, Integer.MAX_VALUE);

            configure(builder);
            builder.pop();
        }
    }

    public static final class EventsConfig extends BaseConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> eventPresets;

        public EventsConfig(ModConfigSpec.Builder builder) {
            super(
                    builder,
                    "events",
                    "List of event presets. Each preset defines the configuration for a specific event.",
                    "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#events-presets"
            );
        }

        @Override
        protected void configure(ModConfigSpec.Builder builder) {
            eventPresets = builder
                    .defineList(
                            List.of("eventPresets"),
                            EventPreset::getDefaultConfig,
                            null,
                            o -> o instanceof Config
                    );
        }
    }

    public static final class RewardsConfig extends BaseConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> rewardPresets;

        public RewardsConfig(ModConfigSpec.Builder builder) {
            super(
                    builder,
                    "rewards",
                    "List of reward presets. Each preset defines the configuration for a specific reward.",
                    "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#rewards-presets"

            );
        }

        @Override
        protected void configure(ModConfigSpec.Builder builder) {
            rewardPresets = builder
                    .defineList(
                            List.of("rewardPresets"),
                            RewardPreset::getDefaultConfig,
                            null,
                            o -> o instanceof Config
                    );
        }
    }

    public static final class SchedulesConfig extends BaseConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> schedulePresets;

        public SchedulesConfig(ModConfigSpec.Builder builder) {
            super(
                    builder,
                    "schedules",
                    "List of schedule presets. Each preset defines the configuration for a specific schedule.",
                    "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#scheduling-presets"
            );
        }

        @Override
        protected void configure(ModConfigSpec.Builder builder) {
            schedulePresets = builder
                    .defineList(
                            List.of("schedulePresets"),
                            SchedulePreset::getDefaultConfig,
                            null,
                            o -> o instanceof Config
                    );
        }
    }

    public static final class VisualsConfig extends BaseConfig {
        public ModConfigSpec.ConfigValue<List<? extends Config>> visualsPresets;

        public VisualsConfig(ModConfigSpec.Builder builder) {
            super(
                    builder,
                    "visuals",
                    "List of Visuals Presets. Each preset defines the visuals for a falling star event.",
                    "For full breakdown of configuration options, see: https://mods.matthiesen.dev/falling-star-rewards/config/#visuals-presets",
                    "List of available particle presets:" + ParticlePreset.getPresetsConfigString()
            );
        }

        @Override
        protected void configure(ModConfigSpec.Builder builder) {
            visualsPresets = builder
                    .comment()
                    .defineList(
                            List.of("visualsPresets"),
                            VisualsPreset::getDefaultConfig,
                            null,
                            o -> o instanceof Config
                    );
        }
    }

}
