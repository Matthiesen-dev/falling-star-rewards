package dev.matthiesen.falling_star_rewards.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.matthiesen.common.matthiesen_lib_api.command.AbstractCommand;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class FallingStarCommand extends AbstractCommand {
    public static final FallingStarCommand CMD = new FallingStarCommand();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        dispatcher.register(
                Commands.literal("fallingstar")
                        .requires(src -> src.hasPermission(4))
                        .then(Commands.literal("reload").executes(this::reload))
                        .then(Commands.literal("status").executes(this::status))
                        .then(Commands.literal("cleanup").executes(this::cleanup))
                        .then(Commands.literal("force")
                                .executes(this::forceOnce)
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 16))
                                        .executes(this::forceCount)))
        );
    }

    @Override
    public int action(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    public int reload(CommandContext<CommandSourceStack> context) {
        FallingStarRewards.INSTANCE.reload().run();
        context.getSource().sendSystemMessage(
                Component.literal("Falling Star Rewards config reloaded!")
        );
        return 1;
    }

    public int status(CommandContext<CommandSourceStack> context) {
        var mod = FallingStarRewards.INSTANCE;
        var config = mod.getMainConfig();
        Component statusTable = new KeyValueTableComponentBuilder("Falling Star Rewards Status")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Base Interval (ticks)", Integer.toString(config.scheduler.baseIntervalTicks))
                .addRow("Next Cycle Tick", Long.toString(mod.getNextCycleTick()))
                .addRow("Active Drops", Integer.toString(mod.getActiveDropCount()))
                .addRow("Life Ticks", Integer.toString(config.claim.lifeTicks))
                .addRow("Max Active Drops", Integer.toString(config.claim.maxActiveDrops))
                .addRow("Particle Preset", config.visuals.particlePreset)
                .addRow("Impact Preset", config.visuals.impactParticlePreset)
                .addRow("Impact Sound", config.visuals.impactSoundId)
                .addRow("Travel Sound", config.visuals.travelSoundId)
                .build();

        context.getSource().sendSystemMessage(statusTable);
        return 1;
    }

    public int cleanup(CommandContext<CommandSourceStack> context) {
        int removed = FallingStarRewards.INSTANCE.cleanupActiveDrops(context.getSource().getServer());
        context.getSource().sendSystemMessage(Component.literal(
                "Removed tracked star drops: " + removed
        ));
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
        ));
        return spawned;
    }
}
