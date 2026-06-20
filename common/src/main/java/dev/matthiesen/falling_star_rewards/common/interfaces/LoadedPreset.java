package dev.matthiesen.falling_star_rewards.common.interfaces;

import dev.matthiesen.falling_star_rewards.common.config.presets.EventPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.RewardsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;

public final class LoadedPreset {
    public EventPresetConfig eventConfig;
    public RewardsPresetConfig rewardsConfig;
    public VisualsPresetConfig visualsConfig;

    public LoadedPreset(EventPresetConfig eventConfig, RewardsPresetConfig rewardsConfig, VisualsPresetConfig visualsConfig) {
        this.eventConfig = eventConfig;
        this.rewardsConfig = rewardsConfig;
        this.visualsConfig = visualsConfig;
    }
}
