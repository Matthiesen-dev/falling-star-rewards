package dev.matthiesen.falling_star_rewards.fabric;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.fabricmc.api.ModInitializer;

public class FallingStarRewardsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FallingStarRewards.INSTANCE.createInfoLog("Loading for Fabric Mod Loader");
        FallingStarRewards.INSTANCE.initialize();
    }
}
