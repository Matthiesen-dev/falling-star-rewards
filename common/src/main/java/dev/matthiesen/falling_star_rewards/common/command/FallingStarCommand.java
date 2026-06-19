package dev.matthiesen.falling_star_rewards.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.common.matthiesen_lib_api.command.AbstractCommand;
import dev.matthiesen.common.matthiesen_lib_api.utility.ChatTableBuilder;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Falling Star Commands
 *<pre>
 *     Current:
 *
 *     /fallingstar help
 *     /fallingstar reload
 *     /fallingstar cleanup
 *     /fallingstar status
 *     /fallingstar status brief
 *     /fallingstar status full
 *     /fallingstar force
 *     /fallingstar force [preset]
 *
 *     Planned:
 *
 *     /fallingstar preset [events|visuals] [disable|enable] [name]
 *     /fallingstar preset [events|visuals|rewards] list
 *     /fallingstar preset [events|visuals|rewards] create [name]
 *     /fallingstar preset [events|visuals|rewards] delete [name]
 *     /fallingstar preset [events|visuals|rewards] info [name]
 *     /fallingstar preset events set [reward|visuals] [name] [preset name]
 *     /fallingstar preset rewards add [name] [item_id] [weight] [min] [max] (custom_model_data) (custom_data)
 *     /fallingstar preset rewards remove [name] [item_id]
 *</pre>
 */
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
                                .argument("preset", StringArgumentType.string(),
                                        preset -> preset.suggests((ctx, builder) -> {
                                                    FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                })
                                                .executes(this::forcePreset))
                        )

                        .then("preset", preset -> preset

                                .then("events", events -> events
                                        .then("enable", enable -> enable
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.suggests((ctx, builder) -> {
                                                                    FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(this::help)))
                                        .then("disable", enable -> enable
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.suggests((ctx, builder) -> {
                                                                    FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(this::help))
                                        )
                                        .then("list", list -> list.executes(this::help))
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("set", set -> set
                                                .then("rewards", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .then(Commands.argument("preset-id", StringArgumentType.string())
                                                                        .suggests((ctx, builder) -> {
                                                                            FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                            return builder.buildFuture();
                                                                        })
                                                                        .executes(this::help)
                                                                )
                                                        )
                                                )
                                                .then("visuals", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .then(Commands.argument("preset-id", StringArgumentType.string())
                                                                        .suggests((ctx, builder) -> {
                                                                            FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                            return builder.buildFuture();
                                                                        })
                                                                        .executes(this::help)
                                                                )
                                                        )
                                                )
                                        )
                                )

                                .then("rewards", rewards -> rewards
                                        .then("list", list -> list.executes(this::help))
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("add", add -> add
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .suggests((ctx, builder) -> {
                                                            FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                            return builder.buildFuture();
                                                        })
                                                        .then(
                                                                Commands.argument("item_id", StringArgumentType.string())
                                                                        .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                                                .then(Commands.argument("min", IntegerArgumentType.integer())
                                                                                        .then(Commands.argument("max", IntegerArgumentType.integer())
                                                                                                .executes(this::help)
                                                                                                .then(Commands.argument("custom_model_data", IntegerArgumentType.integer())
                                                                                                        .executes(this::help)
                                                                                                        .then(Commands.argument("custom_data", StringArgumentType.string())
                                                                                                                .executes(this::help)
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                        )
                                                )
                                        )
                                        .then("remove", remove -> remove
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .suggests((ctx, builder) -> {
                                                            FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                            return builder.buildFuture();
                                                        })
                                                        .then(Commands.argument("item_id", StringArgumentType.string())
                                                                .executes(this::help)
                                                        )
                                                )
                                        )
                                )

                                .then("visuals", visuals -> visuals
                                        .then("enable", enable -> enable
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.suggests((ctx, builder) -> {
                                                                    FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(this::help)))
                                        .then("disable", enable -> enable
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.suggests((ctx, builder) -> {
                                                                    FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(this::help)))
                                        .then("list", list -> list.executes(this::help))
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name -> name.executes(this::help))
                                        )
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
            builder.addRow("Preset Generation Enabled", Boolean.toString(config.enablePresetGeneration));
        }

        builder
                .addSection("Presets")
                .addRow("Available Events", Integer.toString(mod.getConfigManager().calculateEventPresets()))
                .addRow("Available Rewards", Integer.toString(mod.getConfigManager().calculateRewardPresets()));

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
                    .addRow("Base Tick Interval", Integer.toString(config.scheduler.baseIntervalTicks))
                    .addRow("Interval Jitter", Integer.toString(config.scheduler.intervalJitterTicks))
                    .addRow("Max Stars Per Cycle", Integer.toString(config.scheduler.maxStarsPerCycle));
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
                .addRow("/fallingstar force <preset>", "Force a spawn cycle with the specified preset")
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
        return executeForce(context, null);
    }

    public int forcePreset(CommandContext<CommandSourceStack> context) {
        String preset = StringArgumentType.getString(context, "preset");
        return executeForce(context, preset);
    }

    private int executeForce(CommandContext<CommandSourceStack> context, String presetId) {
        int spawned = FallingStarRewards.INSTANCE.forceCycle(
                context.getSource().getServer(),
                presetId,
                true
        );

        context.getSource().sendSystemMessage(Component.literal(
                "Forced falling star cycle complete"
        ).withStyle(ChatFormatting.GREEN));
        return spawned;
    }
}
