package dev.matthiesen.falling_star_rewards.common.interfaces;

import dev.matthiesen.falling_star_rewards.common.config.def.EventPreset;
import dev.matthiesen.falling_star_rewards.common.config.def.RewardPreset;
import dev.matthiesen.falling_star_rewards.common.config.def.VisualsPreset;

public final class LoadedPreset {
    public EventPreset eventConfig;
    public RewardPreset rewardsConfig;
    public VisualsPreset visualsConfig;

    public LoadedPreset(EventPreset eventConfig, RewardPreset rewardsConfig, VisualsPreset visualsConfig) {
        this.eventConfig = eventConfig;
        this.rewardsConfig = rewardsConfig;
        this.visualsConfig = visualsConfig;
    }
}
