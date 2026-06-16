package dev.matthiesen.falling_star_rewards.neoforge;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.neoforged.fml.common.Mod;

@Mod(FallingStarRewards.MOD_ID)
public class FallingStarRewardsNeoForge {
    public FallingStarRewardsNeoForge() {
        FallingStarRewards.INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
        FallingStarRewards.INSTANCE.initialize();
    }
}
