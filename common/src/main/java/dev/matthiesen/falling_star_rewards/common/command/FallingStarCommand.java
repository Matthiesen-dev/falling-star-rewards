package dev.matthiesen.falling_star_rewards.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
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
 *     /fallingstar confirm-delete [event_id]
 *     /fallingstar preset [events|visuals] [disable|enable] [name]
 *     /fallingstar preset [events|visuals|rewards] list
 *     /fallingstar preset [events|visuals|rewards] create [name]
 *     /fallingstar preset [events|visuals|rewards] delete [name]
 *
 *     Planned:
 *
 *     /fallingstar preset [events|visuals|rewards] info [name]
 *     /fallingstar preset events set [reward|visuals] [name] [preset name]
 *     /fallingstar preset rewards add [name] [item_id] [weight] [min] [max] (custom_model_data) (custom_data)
 *     /fallingstar preset rewards add-held-item [name]
 *     /fallingstar preset rewards remove [name] [item_id]
 *</pre>
 */
public final class FallingStarCommand extends AbstractCommand {
    public static final FallingStarCommand CMD = new FallingStarCommand();
    private static final Map<String, PresetDeletionRequest> DELETION_REQUESTS = new LinkedHashMap<>();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        dispatcher.register(
                new CommandBuilder("fallingstar", src -> src.hasPermission(4))
                        .then("help", help -> help
                                .executes(this::help)
                        )
                        .then("reload", reload -> reload
                                .executes(this::reload)
                        )
                        .then("cleanup", cleanup -> cleanup
                                .executes(this::cleanup)
                        )
                        .then("status", status -> status
                                .executes(this::status)
                                .then("brief", brief -> brief
                                        .executes(this::status)
                                )
                                .then("full", full -> full
                                        .executes(this::statusFull)
                                )
                        )
                        .then("force", force -> force
                                .executes(this::forceOnce)
                                .argument("preset", StringArgumentType.string(), preset -> preset
                                        .suggests(this::getEventsPresetLists)
                                        .executes(this::forcePreset)
                                )
                        )
                        .then("preset", preset -> preset
                                .then("events", events -> events
                                        .then("enable", enable -> enable
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getEventsPresetLists)
                                                        .executes(this::presetEventEnable)
                                                )
                                        )
                                        .then("disable", enable -> enable
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getEventsPresetLists)
                                                        .executes(this::presetEventDisable)
                                                )
                                        )
                                        .then("list", list -> list
                                                .executes(this::presetEventsList)
                                        )
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .executes(this::presetEventCreate)
                                                )
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getEventsPresetLists)
                                                        .executes(this::presetEventsDelete)
                                                )
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getEventsPresetLists)
                                                        .executes(this::help) // TODO
                                                )
                                        )
                                        .then("set", set -> set
                                                .then("rewards", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .suggests(this::getRewardsPresetLists)
                                                                .then(Commands.argument("preset_id", StringArgumentType.string())
                                                                        .suggests(this::getRewardsPresetLists)
                                                                        .executes(this::help) // TODO
                                                                )
                                                        )
                                                )
                                                .then("visuals", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .suggests(this::getVisualsPresetLists)
                                                                .then(Commands.argument("preset_id", StringArgumentType.string())
                                                                        .suggests(this::getVisualsPresetLists)
                                                                        .executes(this::help) // TODO
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then("rewards", rewards -> rewards
                                        .then("list", list -> list
                                                .executes(this::presetRewardsList)
                                        )
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .executes(this::presetRewardsCreate)
                                                )
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getRewardsPresetLists)
                                                        .executes(this::presetRewardsDelete)
                                                )
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getRewardsPresetLists)
                                                        .executes(this::help) // TODO
                                                )
                                        )
                                        .then("add", add -> add
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .suggests(this::getRewardsPresetLists)
                                                        .then(
                                                                Commands.argument("item_id", StringArgumentType.string())
                                                                        .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                                                .then(Commands.argument("min", IntegerArgumentType.integer())
                                                                                        .then(Commands.argument("max", IntegerArgumentType.integer())
                                                                                                .executes(this::help) // TODO
                                                                                                .then(Commands.argument("custom_model_data", IntegerArgumentType.integer())
                                                                                                        .executes(this::help) // TODO
                                                                                                        .then(Commands.argument("custom_data", StringArgumentType.string())
                                                                                                                .executes(this::help) // TODO
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
                                                            .suggests(this::getRewardsPresetLists)
                                                            .executes(this::help) // TODO
                                                )
                                        )
                                        .then("remove", remove -> remove
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .suggests(this::getRewardsPresetLists)
                                                        .then(Commands.argument("item_id", StringArgumentType.string())
                                                                .executes(this::help) // TODO
                                                        )
                                                )
                                        )
                                )
                                .then("visuals", visuals -> visuals
                                        .then("enable", enable -> enable
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getVisualsPresetLists)
                                                        .executes(this::presetVisualsEnable)
                                                )
                                        )
                                        .then("disable", enable -> enable
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getVisualsPresetLists)
                                                        .executes(this::presetVisualsDisable)
                                                )
                                        )
                                        .then("list", list -> list
                                                .executes(this::presetVisualsList)
                                        )
                                        .then("create", create -> create
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .executes(this::presetVisualsCreate)
                                                )
                                        )
                                        .then("delete", delete -> delete
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getVisualsPresetLists)
                                                        .executes(this::presetVisualsDelete)
                                                )
                                        )
                                        .then("info", info -> info
                                                .argument("name", StringArgumentType.string(), name -> name
                                                        .suggests(this::getVisualsPresetLists)
                                                        .executes(this::help) // TODO
                                                )
                                        )
                                )
                        )
                        .then("confirm-delete", confirmDelete -> confirmDelete
                                .argument("event_id", StringArgumentType.string(), eventId -> eventId
                                        .executes(this::confirmDelete)
                                )
                        )
                        .build()
        );
    }

    private CompletableFuture<Suggestions> getPresetList(SuggestionsBuilder builder, ConfigFolderManager<?> manager) {
        manager.getConfigs().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> getEventsPresetLists(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return getPresetList(builder, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager());
    }

    private CompletableFuture<Suggestions> getRewardsPresetLists(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return getPresetList(builder, FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager());
    }

    private CompletableFuture<Suggestions> getVisualsPresetLists(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return getPresetList(builder, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager());
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

    // This should be a random set of characters roughly 8 characters long
    private String generateDeletionKey() {
        int length = 8;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder key = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            key.append(chars.charAt(random.nextInt(chars.length())));
        }
        return key.toString();
    }

    private int presetEventsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), PresetDeletionRequest.PRESET_TYPES.EVENT, "event");
    }

    private int presetRewardsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(), PresetDeletionRequest.PRESET_TYPES.REWARDS, "rewards");
    }

    private int presetVisualsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), PresetDeletionRequest.PRESET_TYPES.VISUALS, "visuals");
    }

    private <T> int presetDelete(
            CommandContext<CommandSourceStack> context,
            ConfigFolderManager<T> manager,
            PresetDeletionRequest.PRESET_TYPES presetType,
            String presetTypeLabel
    ) {
        String name = StringArgumentType.getString(context, "name");
        if (!manager.getConfigs().containsKey(name)) {
            String capitalizedType = presetTypeLabel.substring(0, 1).toUpperCase() + presetTypeLabel.substring(1);
            context.getSource().sendFailure(Component.literal(capitalizedType + " preset not found: " + name).withStyle(ChatFormatting.RED));
            return 0;
        }
        String eventKey = generateDeletionKey();
        PresetDeletionRequest request = new PresetDeletionRequest(presetType, name);
        DELETION_REQUESTS.put(eventKey, request);
        context.getSource().sendSystemMessage(Component.literal("Are you sure you want to delete the " + presetTypeLabel + " preset '" + name + "'? This action cannot be undone. If you're sure, run the command: /fallingstar confirm-delete " + eventKey).withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private int confirmDelete(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "event_id");

        if (!DELETION_REQUESTS.containsKey(key)) {
            context.getSource().sendFailure(Component.literal("Invalid or expired deletion key: " + key).withStyle(ChatFormatting.RED));
            return 0;
        }

        PresetDeletionRequest request = DELETION_REQUESTS.remove(key);

        // Get the appropriate manager based on the preset type
        ConfigFolderManager<?> manager = switch (request.presetType()) {
            case EVENT -> FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager();
            case REWARDS -> FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager();
            case VISUALS -> FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager();
        };

        // Delete the preset
        boolean deleted = manager.deleteConfig(request.presetName());

        String presetType = request.presetType().name().toLowerCase(Locale.ROOT);
        if (deleted) {
            context.getSource().sendSystemMessage(
                    Component.literal("The " + presetType + " preset '" + request.presetName() + "' has been deleted.").withStyle(ChatFormatting.GREEN)
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                    Component.literal("Failed to delete the " + presetType + " preset '" + request.presetName() + "'.").withStyle(ChatFormatting.RED)
            );
            return 0;
        }
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

    public record PresetDeletionRequest(PRESET_TYPES presetType, String presetName) {
        public enum PRESET_TYPES {
                EVENT,
                REWARDS,
                VISUALS
            }
        }
}
