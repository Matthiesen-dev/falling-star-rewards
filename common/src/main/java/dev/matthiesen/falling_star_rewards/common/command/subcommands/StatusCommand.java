package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.common.matthiesen_lib_api.utility.ChatTableBuilder;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
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

    private static Component buildStatusTable(boolean full) {
        var mod = FallingStarRewards.INSTANCE;
        var config = mod.getMainConfig();
        var announcementsConfig = mod.getAnnouncementsConfig();

        ChatTableBuilder builder = new ChatTableBuilder(
                full ? "Falling Star Rewards Status (Full)" : "Falling Star Rewards Status"
        )
                .addSection("Runtime")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Next Cycle Tick", Long.toString(mod.getNextCycleTick()))
                .addRow("Active Drops", Integer.toString(mod.getActiveDropCount()));

        if (full) {
            builder.addRow("Preset Generation Enabled", Boolean.toString(config.enablePresetGeneration));
        }

        builder
                .addSection("Presets")
                .addRow("Available Events", Integer.toString(mod.getConfigManager().calculateEventPresets()))
                .addRow("Available Rewards", Integer.toString(mod.getConfigManager().calculateRewardPresets()))
                .addRow("Available Schedules", Integer.toString(mod.getConfigManager().calculateSchedulePresets()));

        if (full) {
            builder
                    .addSection("Announcements")
                    .addRow("Enabled", Boolean.toString(announcementsConfig.enabled))
                    .addRow("Scope", announcementsConfig.scope)
                    .addRow("Use Action Bar Overlay", Boolean.toString(announcementsConfig.useActionBar))
                    .addRow("Message", announcementsConfig.spawnMessage)

                    .addSection("Claim")
                    .addRow("Life Ticks", Integer.toString(config.claim.lifeTicks))
                    .addRow("Pickup Delay Ticks", Integer.toString(config.claim.pickupDelayTicks))
                    .addRow("Max Active Drops", Integer.toString(config.claim.maxActiveDrops))

                    .addSection("Scheduling")
                    .addRow("Enabled Schedule IDs", config.enabledSchedules == null || config.enabledSchedules.isEmpty()
                            ? "None"
                            : String.join(", ", config.enabledSchedules));
        }

        return builder.build();
    }
}
