package dev.matthiesen.falling_star_rewards.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.common.matthiesen_lib_api.command.AbstractCommand;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigFolderManager;
import dev.matthiesen.common.matthiesen_lib_api.utility.ChatTableBuilder;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.presets.EventPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

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
 *     /fallingstar preset [events|visuals] [disable|enable] [name]
 *     /fallingstar preset [events|visuals|rewards] list
 *     /fallingstar preset [events|visuals|rewards] create [name]
 *
 *     Planned:
 *
 *     /fallingstar preset [events|visuals|rewards] info [name]
 *     /fallingstar preset events set [reward|visuals] [name] [preset name]
 *     /fallingstar preset rewards add [name] [item_id] [weight] [min] [max] (custom_model_data) (custom_data)
 *     /fallingstar preset rewards add-held-item [name]
 *     /fallingstar preset rewards remove [name] [item_id]
 *
 *     /fallingstar preset [events|visuals|rewards] delete [name]
 *     /fallingstar confirm-delete [event_id]
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
                                                                .executes(this::presetEventEnable)))
                                        .then("disable", enable -> enable
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.suggests((ctx, builder) -> {
                                                                    FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(this::presetEventDisable))
                                        )
                                        .then("list", list -> list.executes(this::presetEventsList))
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.executes(this::presetEventCreate))
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.executes(this::help))
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.executes(this::help))
                                        )
                                        .then("set", set -> set
                                                .then("rewards", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .then(Commands.argument("preset_id", StringArgumentType.string())
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
                                                                .then(Commands.argument("preset_id", StringArgumentType.string())
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
                                        .then("list", list -> list.executes(this::presetRewardsList))
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.executes(this::presetRewardsCreate))
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.executes(this::help))
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.executes(this::help))
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
                                        .then("add-held-item", addHeldItem -> addHeldItem
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name
                                                            .suggests((ctx, builder) -> {
                                                                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                return builder.buildFuture();
                                                            })
                                                            .executes(this::help)
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
                                                                .executes(this::presetVisualsEnable)))
                                        .then("disable", enable -> enable
                                                .argument("name", StringArgumentType.string(),
                                                        name -> name.suggests((ctx, builder) -> {
                                                                    FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager().getConfigs().keySet().forEach(builder::suggest);
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(this::presetVisualsDisable)))
                                        .then("list", list -> list.executes(this::presetVisualsList))
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name ->
                                                        name.executes(this::presetVisualsCreate))
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name ->
                                                        name.executes(this::help))
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name ->
                                                        name.executes(this::help))
                                        )
                                )

                        )
                        .then("confirm-delete", confirmDelete -> confirmDelete
                                .argument("event_id", StringArgumentType.string(),
                                        eventId -> eventId.executes(this::help))
                        )
                        .build()
        );
    }

    private <T> void updateConfigAndSave(ConfigFolderManager<T> manager, String preset, T config) {
        manager.setConfig(preset, config);
        manager.saveConfig(preset);
    }

    private Component presetEnabledState(String preset, boolean value) {
        return Component.literal("Preset " + preset + " has been " + (value ? "enabled" : "disabled") + ".").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @SuppressWarnings("unchecked")
    private <T> void presetEnableDisable(CommandContext<CommandSourceStack> context, ConfigFolderManager<T> manager, boolean value) {
        String preset = StringArgumentType.getString(context, "name");
        var presetConfig = manager.getConfig(preset);
        switch (presetConfig) {
            case EventPresetConfig eventPresetConfig -> {
                eventPresetConfig.enabled = value;
                updateConfigAndSave(manager, preset, (T) eventPresetConfig);
                context.getSource().sendSystemMessage(presetEnabledState(preset, value));
            }
            case VisualsPresetConfig visualsPresetConfig -> {
                visualsPresetConfig.enabled = value;
                updateConfigAndSave(manager, preset, (T) visualsPresetConfig);
                context.getSource().sendSystemMessage(presetEnabledState(preset, value));
            }
            default -> context.getSource().sendFailure(Component.literal("Preset not found: " + preset).withStyle(ChatFormatting.RED));
        }
    }

    public int presetEventEnable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), true);
        return 1;
    }

    public int presetVisualsEnable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), true);
        return 1;
    }

    public int presetEventDisable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), false);
        return 1;
    }

    public int presetVisualsDisable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), false);
        return 1;
    }

    public int presetEventsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(),
                "No event presets found.",
                "Event Presets",
                config -> config.enabled ? "Enabled" : "Disabled"
        );
    }

    public int presetRewardsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(),
                "No reward presets found.",
                "Reward Presets",
                config -> "Enabled"
        );
    }

    public int presetVisualsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(),
                "No visuals presets found.",
                "Visual Presets",
                config -> config.enabled ? "Enabled" : "Disabled"
        );
    }

    @SuppressWarnings("SameReturnValue")
    private <T> int presetList(
            CommandContext<CommandSourceStack> context,
            ConfigFolderManager<T> manager,
            String emptyMessage,
            String tableTitle,
            Function<T, String> statusResolver
    ) {
        if (manager.getConfigs().isEmpty()) {
            context.getSource().sendSystemMessage(Component.literal(emptyMessage).withStyle(ChatFormatting.YELLOW));
            return 1;
        }

        var chatMessage = new ChatTableBuilder(tableTitle);
        manager.getConfigs().forEach((name, config) -> chatMessage.addRow(name, statusResolver.apply(config)));
        context.getSource().sendSystemMessage(chatMessage.build());
        return 1;
    }

    private int presetEventCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), "event", "Event");
    }

    private int presetRewardsCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(), "reward", "Reward");
    }

    private int presetVisualsCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), "visuals", "Visuals");
    }

    private <T> int presetCreate(
            CommandContext<CommandSourceStack> context,
            ConfigFolderManager<T> manager,
            String presetType,
            String presetTitle
    ) {
        String name = StringArgumentType.getString(context, "name");
        if (manager.getConfigs().containsKey(name)) {
            context.getSource().sendFailure(Component.literal("A " + presetType + " preset with that name already exists.").withStyle(ChatFormatting.RED));
            return 0;
        }
        manager.loadConfig(name);
        context.getSource().sendSystemMessage(Component.literal(presetTitle + " preset '" + name + "' created successfully.").withStyle(ChatFormatting.GREEN));
        return 1;
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
