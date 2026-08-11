package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.VisualsPreset;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class VisualsServerConfig {

    public ModConfigSpec.ConfigValue<List<? extends Config>> visualsPresets;

    public VisualsServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Falling Star Rewards - Visuals Configuration").push("visuals");

        visualsPresets = builder
                .comment("List of Visuals Presets. Each preset defines the visuals for a falling star event.")
                .defineList(
                        List.of("visualsPresets"),
                        VisualsPreset::getDefaultConfig,
                        null,
                        o -> o instanceof Config && VisualsPreset.isValid(o)
                );

        builder.pop();
    }
}
