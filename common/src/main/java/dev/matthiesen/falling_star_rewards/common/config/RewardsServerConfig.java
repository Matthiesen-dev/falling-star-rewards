package dev.matthiesen.falling_star_rewards.common.config;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.RewardPreset;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class RewardsServerConfig {

    public ModConfigSpec.ConfigValue<List<? extends Config>> rewardPresets;

    public RewardsServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Falling Star Rewards - Rewards Configuration").push("rewards");

        rewardPresets = builder
                .comment(
                        "List of reward presets. Each preset defines the configuration for a specific reward."
                )
                .defineList(
                        List.of("rewardPresets"),
                        RewardPreset::getDefaultConfig,
                        null,
                        o -> o instanceof Config && RewardPreset.isValid(o)
                );

        builder.pop();
    }
}
