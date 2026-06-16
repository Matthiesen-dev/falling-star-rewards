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
                        .then(Commands.literal("help").executes(this::help))
                        .then(Commands.literal("reload").executes(this::reload))
                        .then(Commands.literal("status")
                                .executes(this::status)
                                .then(Commands.literal("brief").executes(this::status))
                                .then(Commands.literal("full").executes(this::statusFull)))
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

        KeyValueTableComponentBuilder builder = new KeyValueTableComponentBuilder(
                full ? "Falling Star Rewards Status (Full)" : "Falling Star Rewards Status"
        )
                .addSection("Runtime")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Next Cycle Tick", Long.toString(mod.getNextCycleTick()))
                .addRow("Active Drops", Integer.toString(mod.getActiveDropCount()))
                .addSection("Scheduler")
                .addRow("Base Interval (ticks)", Integer.toString(config.scheduler.baseIntervalTicks))
                .addRow("Jitter (ticks)", Integer.toString(config.scheduler.intervalJitterTicks))
                .addRow("Max Stars/Cycle", Integer.toString(config.scheduler.maxStarsPerCycle))
                .addSection("Claim")
                .addRow("Life Ticks", Integer.toString(config.claim.lifeTicks))
                .addRow("Max Active Drops", Integer.toString(config.claim.maxActiveDrops))
                .addRow("Pickup Delay", Integer.toString(config.claim.pickupDelayTicks))
                .addSection("Visuals")
                .addRow("Particle Preset", config.visuals.particlePreset)
                .addRow("Impact Preset", config.visuals.impactParticlePreset)
                .addSection("Audio")
                .addRow("Impact Sound", config.visuals.impactSoundId)
                .addRow("Travel Sound", config.visuals.travelSoundId);

        if (full) {
            builder
                    .addSection("Activation")
                    .addRow("Require Night", Boolean.toString(config.activation.requireNight))
                    .addRow("Require Surface", Boolean.toString(config.activation.requireSurfaceAccess))
                    .addRow("Weather Mode", config.activation.weatherMode)
                    .addSection("Spawn")
                    .addRow("Scope", config.spawn.targetScope)
                    .addRow("Min Radius", Integer.toString(config.spawn.minRadius))
                    .addRow("Max Radius", Integer.toString(config.spawn.maxRadius))
                    .addRow("Location Attempts", Integer.toString(config.spawn.maxLocationAttempts))
                    .addRow("Allow Water", Boolean.toString(config.spawn.allowWaterSpawns))
                    .addSection("Announcements")
                    .addRow("Enabled", Boolean.toString(config.announcements.enabled))
                    .addRow("Scope", config.announcements.scope)
                    .addRow("Message", config.announcements.spawnMessage)
                    .addSection("Visual FX")
                    .addRow("Trail Enabled", Boolean.toString(config.visuals.enabled))
                    .addRow("Trail Distance", Integer.toString(config.visuals.fallDistance))
                    .addRow("Trail Interval", Integer.toString(config.visuals.emissionIntervalTicks))
                    .addRow("Trail Count", Integer.toString(config.visuals.particlesPerEmission))
                    .addRow("Impact Burst", Boolean.toString(config.visuals.impactBurstEnabled))
                    .addRow("Impact Count", Integer.toString(config.visuals.impactParticleCount))
                    .addRow("Impact Spread", Double.toString(config.visuals.impactSpread))
                    .addSection("Audio Detail")
                    .addRow("Impact Enabled", Boolean.toString(config.visuals.impactSoundEnabled))
                    .addRow("Impact Volume", Float.toString(config.visuals.impactSoundVolume))
                    .addRow("Impact Pitch Min", Float.toString(config.visuals.impactSoundPitchMin))
                    .addRow("Impact Pitch Max", Float.toString(config.visuals.impactSoundPitchMax))
                    .addRow("Travel Enabled", Boolean.toString(config.visuals.travelSoundEnabled))
                    .addRow("Travel Volume", Float.toString(config.visuals.travelSoundVolume))
                    .addRow("Travel Pitch Min", Float.toString(config.visuals.travelSoundPitchMin))
                    .addRow("Travel Pitch Max", Float.toString(config.visuals.travelSoundPitchMax))
                    .addRow("Travel Interval", Integer.toString(config.visuals.travelSoundIntervalTicks))
                    .addSection("Rewards")
                    .addRow("Pool Mode", config.rewards.poolMode)
                    .addRow("Total Entries", Integer.toString(config.rewards.entries.length))
                    .addRow("Valid Entries", Integer.toString(mod.getRewardValidator().getValidEntries()))
                    .addRow("Invalid Entries", Integer.toString(mod.getRewardValidator().getInvalidEntries()));
        }

        return builder.build();
    }

    private Component buildHelpTable() {
        return new KeyValueTableComponentBuilder("Falling Star Rewards Commands")
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
