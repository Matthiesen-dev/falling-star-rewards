package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.FSConfig;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class StatusCommand {
    public static CommandBuilder getStatusSubCommand() {
        return new CommandBuilder("status", FallingStarRewards.getPermissionPredicate(
                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_STATUS
        ))
                .executes(StatusCommand::status)
                .then("brief", brief -> brief
                        .executes(StatusCommand::status)
                )
                .then("full", full -> full
                        .executes(StatusCommand::statusFull)
                );
    }

    public static int status(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(buildStatusTable(false));
        return 1;
    }

    public static int statusFull(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(buildStatusTable(true));
        return 1;
    }

    @SuppressWarnings({"deprecation"})
    private static Component buildStatusTable(boolean full) {
        var mod = FallingStarRewards.INSTANCE;
        var config = FSConfig.SERVER_CONFIG;

        ChatTableBuilder builder = new ChatTableBuilder(
                full ? "Falling Star Rewards Status (Full)" : "Falling Star Rewards Status"
        )
                .addSection("Runtime")
                .addRow("Enabled", Boolean.toString(config.enabled.getAsBoolean()))
                .addRow("Next Cycle Tick", Long.toString(mod.getNextCycleTick()))
                .addRow("Active Drops", Integer.toString(mod.getActiveDropCount()));

        if (full) {
            builder.addRow("Preset Generation (Deprecated)", Boolean.toString(config.enablePresetGeneration.getAsBoolean()));
        }

        builder
                .addSection("Presets")
                .addRow("Available Events", Integer.toString(FSConfig.calculateEventPresets()))
                .addRow("Available Rewards", Integer.toString(FSConfig.calculateRewardPresets()))
                .addRow("Available Schedules", Integer.toString(FSConfig.calculateSchedulePresets()));

        if (full) {
            builder
                    .addSection("Claim")
                    .addRow("Life Ticks", Integer.toString(config.claim_lifeTicks.getAsInt()))
                    .addRow("Pickup Delay Ticks", Integer.toString(config.claim_pickupDelayTicks.getAsInt()))
                    .addRow("Max Active Drops", Integer.toString(config.claim_maxActiveDrops.getAsInt()))

                    .addSection("Scheduling")
                    .addRow("Enabled Schedule IDs", config.enabledSchedules.get() == null || config.enabledSchedules.get().isEmpty()
                            ? "None"
                            : String.join(", ", config.enabledSchedules.get()));
        }

        return builder.build();
    }
}
