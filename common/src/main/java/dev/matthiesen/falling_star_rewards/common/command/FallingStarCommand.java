package dev.matthiesen.falling_star_rewards.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.matthiesen.common.matthiesen_lib_api.command.AbstractCommand;
import dev.matthiesen.common.matthiesen_lib_api.utility.ChatTableBuilder;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class FallingStarCommand extends AbstractCommand {
    public static final FallingStarCommand CMD = new FallingStarCommand();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        dispatcher.register(
                new CommandBuilder("fallingstar", src -> src.hasPermission(4))
                        .then("help", help -> help.executes(this::help))
                        .then("reload", reload -> reload.executes(this::reload))
                        .then("cleanup", cleanup -> cleanup.executes(this::cleanup))
                        .then("status", status -> status
                                .executes(this::status)
                                .then("brief", brief -> brief.executes(this::status))
                                .then("full", full -> full.executes(this::statusFull))
                        )
                        .then("force", force -> force
                                .executes(this::forceOnce)
                                .argument("count", IntegerArgumentType.integer(1, 16),
                                        count -> count.executes(this::forceCount)
                                )
                        )
                        .build()
        );
    }

    @Override
    public int action(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    public int reload(CommandContext<CommandSourceStack> context) {
        FallingStarRewards.INSTANCE.reload().run();
        context.getSource().sendSystemMessage(
                Component.literal("Falling Star Rewards config reloaded!").withStyle(ChatFormatting.GREEN)
        );
        return 1;
    }

    public int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(buildHelpTable());
        return 1;
    }

    public int status(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(buildStatusTable(false));
        return 1;
    }

    public int statusFull(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(buildStatusTable(true));
        return 1;
    }

    private Component buildStatusTable(boolean full) {
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
            builder
                    .addSection("Announcements")
                    .addRow("Enabled", Boolean.toString(announcementsConfig.enabled))
                    .addRow("Scope", announcementsConfig.scope)
                    .addRow("Use Action Bar Overlay", Boolean.toString(announcementsConfig.useActionBar))
                    .addRow("Message", announcementsConfig.spawnMessage);
        }

        return builder.build();
    }

    private Component buildHelpTable() {
        return new ChatTableBuilder("Falling Star Rewards Commands")
                .addSection("General")
                .addRow("/fallingstar help", "Show this command list")
                .addRow("/fallingstar reload", "Reload config from disk")
                .addSection("Status")
                .addRow("/fallingstar status", "Show condensed status output")
                .addRow("/fallingstar status brief", "Alias for condensed status")
                .addRow("/fallingstar status full", "Show detailed status output")
                .addSection("Actions")
                .addRow("/fallingstar force", "Force one spawn cycle")
                .addRow("/fallingstar force <count>", "Force up to <count> spawns")
                .addRow("/fallingstar cleanup", "Remove tracked active drops")
                .build();
    }

    public int cleanup(CommandContext<CommandSourceStack> context) {
        int removed = FallingStarRewards.INSTANCE.cleanupActiveDrops(context.getSource().getServer());
        context.getSource().sendSystemMessage(Component.literal(
                "Removed tracked star drops: " + removed
        ).withStyle(ChatFormatting.YELLOW));
        return removed;
    }

    public int forceOnce(CommandContext<CommandSourceStack> context) {
        return executeForce(context, 1);
    }

    public int forceCount(CommandContext<CommandSourceStack> context) {
        int count = IntegerArgumentType.getInteger(context, "count");
        return executeForce(context, count);
    }

    private int executeForce(CommandContext<CommandSourceStack> context, int count) {
        int spawned = FallingStarRewards.INSTANCE.forceCycle(
                context.getSource().getServer(),
                count,
                true
        );

        context.getSource().sendSystemMessage(Component.literal(
                "Forced falling star cycle complete (spawned=" + spawned + ")"
        ).withStyle(ChatFormatting.GREEN));
        return spawned;
    }
}
