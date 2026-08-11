package dev.matthiesen.falling_star_rewards.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ServerConfig {

    // General Options
    public ModConfigSpec.BooleanValue enabled;
    @Deprecated
    public ModConfigSpec.BooleanValue enablePresetGeneration;
    public ModConfigSpec.ConfigValue<List<? extends String>> enabledSchedules;

    // Claim Options
    public ModConfigSpec.IntValue claim_lifeTicks;
    public ModConfigSpec.IntValue claim_pickupDelayTicks;
    public ModConfigSpec.IntValue claim_maxActiveDrops;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("General configuration settings").push("general");
        enabled = builder.comment("Enable or disable Falling Star Rewards")
                .define("enabled", true);
        enablePresetGeneration = builder.comment("[Deprecated] No longer used after FSConfig hard cutover. Will be removed in a future release.")
                .define("enablePresetGeneration", true);
        enabledSchedules = builder.comment("List of enabled schedules for Falling Star Rewards")
                .defineList(
                        "enabledSchedules",
                        List.of("base"),
                        () -> "",
                        o -> o instanceof String
                );

        builder.comment("Claim configuration settings").push("claim");
        claim_lifeTicks = builder.comment("The lifespan of a claim in ticks (20 ticks = 1 second)")
                .defineInRange("lifeTicks", 20 * 45, 1, Integer.MAX_VALUE);
        claim_pickupDelayTicks = builder.comment("The delay before a claim can be picked up in ticks (20 ticks = 1 second)")
                .defineInRange("pickupDelayTicks", 10, 0, Integer.MAX_VALUE);
        claim_maxActiveDrops = builder.comment("The maximum number of active drops allowed at once")
                .defineInRange("maxActiveDrops", 64, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();
    }
}
