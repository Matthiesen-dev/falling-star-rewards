package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigFolderManager;
import dev.matthiesen.common.matthiesen_lib_api.utility.ChatTableBuilder;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.interfaces.NamedPreset;
import dev.matthiesen.falling_star_rewards.common.interfaces.PresetDeletionRequest;
import dev.matthiesen.falling_star_rewards.common.interfaces.PresetTypes;
import dev.matthiesen.falling_star_rewards.common.config.presets.EventPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.RewardsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.SchedulePresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class PresetsCommand {
    public static CommandBuilder getPresetSubCommand() {
        return new CommandBuilder("preset")
                .then(getPresetEventsSubCommand())
                .then(getPresetRewardsSubCommand())
                .then(getPresetVisualsSubCommand())
                .then(getPresetSchedulesSubCommand());
    }

    public static CommandBuilder addEnableDisableCommands(
            CommandBuilder cmdBuilder,
            SuggestionProvider<CommandSourceStack> suggestionsFuture,
            Command<CommandSourceStack> enableCommand,
            Command<CommandSourceStack> disableCommand
    ) {
        return cmdBuilder.then("enable", enable -> enable
                .requires(FallingStarRewards.getPermissionPredicate(
                        FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_ENABLE
                ))
                .argument("name", StringArgumentType.string(), name -> name
                        .suggests(suggestionsFuture)
                        .executes(enableCommand)
                )
        ).then("disable", disable -> disable
                .requires(FallingStarRewards.getPermissionPredicate(
                        FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_DISABLE
                ))
                .argument("name", StringArgumentType.string(), name -> name
                        .suggests(suggestionsFuture)
                        .executes(disableCommand)
                )
        );
    }

    public static CommandBuilder addGenericCommands(
            CommandBuilder cmdBuilder,
            SuggestionProvider<CommandSourceStack> suggestionsFuture,
            Command<CommandSourceStack> listCommand,
            Command<CommandSourceStack> createCommand,
            Command<CommandSourceStack> deleteCommand,
            Command<CommandSourceStack> infoCommand
    ) {
        return cmdBuilder
                .then("list", list -> list
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_LIST
                        ))
                        .executes(listCommand)
                )
                .then("create", create -> create
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_CREATE
                        ))
                        .argument("name", StringArgumentType.string(), name -> name
                                .executes(createCommand)
                        )
                )
                .then("delete", delete -> delete
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_DELETE
                        ))
                        .argument("name", StringArgumentType.string(), name -> name
                                .suggests(suggestionsFuture)
                                .executes(deleteCommand)
                        )
                )
                .then("info", info -> info
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_INFO
                        ))
                        .argument("name", StringArgumentType.string(), name -> name
                                .suggests(suggestionsFuture)
                                .executes(infoCommand)
                        )
                );
    }

    public static CommandBuilder getPresetEventsSubCommand() {
        var builder = new CommandBuilder("events");
        builder = addEnableDisableCommands(
                builder,
                FallingStarCommand::getEventsPresetLists,
                PresetsCommand::presetEventEnable,
                PresetsCommand::presetEventDisable
        );
        builder = addGenericCommands(
                builder,
                FallingStarCommand::getEventsPresetLists,
                PresetsCommand::presetEventsList,
                PresetsCommand::presetEventCreate,
                PresetsCommand::presetEventsDelete,
                PresetsCommand::presetEventsInfo
        );
        return builder
                .then("set", set -> set
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_SET
                        ))
                        .then("rewards", rewards -> rewards
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(FallingStarCommand::getEventsPresetLists)
                                        .then(Commands.argument("preset_id", StringArgumentType.string())
                                                .suggests(FallingStarCommand::getRewardsPresetLists)
                                                .executes(PresetsCommand::presetEventSetRewards)
                                        )
                                )
                        )
                        .then("visuals", rewards -> rewards
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(FallingStarCommand::getEventsPresetLists)
                                        .then(Commands.argument("preset_id", StringArgumentType.string())
                                                .suggests(FallingStarCommand::getVisualsPresetLists)
                                                .executes(PresetsCommand::presetEventSetVisuals)
                                        )
                                )
                        )
                );
    }

    public static CommandBuilder getPresetRewardsSubCommand() {
        var builder = new CommandBuilder("rewards");
        builder = addGenericCommands(
                builder,
                FallingStarCommand::getRewardsPresetLists,
                PresetsCommand::presetRewardsList,
                PresetsCommand::presetRewardsCreate,
                PresetsCommand::presetRewardsDelete,
                PresetsCommand::presetRewardsInfo
        );
        return builder
                .then("add", add -> add
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_ADD
                        ))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(FallingStarCommand::getRewardsPresetLists)
                                .then(
                                        Commands.argument("item_id", StringArgumentType.string())
                                                .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                        .then(Commands.argument("min", IntegerArgumentType.integer())
                                                                .then(Commands.argument("max", IntegerArgumentType.integer())
                                                                        .executes(PresetsCommand::presetRewardsAdd)
                                                                        .then(Commands.argument("custom_model_data", IntegerArgumentType.integer())
                                                                                .executes(PresetsCommand::presetRewardsAddWithCustomModelData)
                                                                                .then(Commands.argument("custom_data", StringArgumentType.string())
                                                                                        .executes(PresetsCommand::presetRewardsAddWithCustomModelDataAndCustomData)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                )
                        )
                )
                .then("add-held-item", addHeldItem -> addHeldItem
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_ADD
                        ))
                        .argument("name", StringArgumentType.string(),
                                name -> name
                                        .suggests(FallingStarCommand::getRewardsPresetLists)
                                        .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                .then(Commands.argument("min", IntegerArgumentType.integer())
                                                        .then(Commands.argument("max", IntegerArgumentType.integer())
                                                                .executes(PresetsCommand::presetRewardsAddHeldItem)
                                                        )
                                                )
                                        )
                        )
                )
                .then("remove", remove -> remove
                        .requires(FallingStarRewards.getPermissionPredicate(
                                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_PRESET_REMOVE
                        ))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(FallingStarCommand::getRewardsPresetLists)
                                .then(Commands.argument("item_id", StringArgumentType.string())
                                        .executes(PresetsCommand::presetRewardsRemove)
                                )
                        )
                );
    }

    public static CommandBuilder getPresetVisualsSubCommand() {
        var builder = new CommandBuilder("visuals");
        builder = addEnableDisableCommands(
                builder,
                FallingStarCommand::getVisualsPresetLists,
                PresetsCommand::presetVisualsEnable,
                PresetsCommand::presetVisualsDisable
        );
        return addGenericCommands(
                builder,
                FallingStarCommand::getVisualsPresetLists,
                PresetsCommand::presetVisualsList,
                PresetsCommand::presetVisualsCreate,
                PresetsCommand::presetVisualsDelete,
                PresetsCommand::presetVisualsInfo
        );
    }

    public static CommandBuilder getPresetSchedulesSubCommand() {
        var builder = new CommandBuilder("schedules");
        builder = addEnableDisableCommands(
                builder,
                FallingStarCommand::getSchedulePresetLists,
                PresetsCommand::presetScheduleEnable,
                PresetsCommand::presetScheduleDisable
        );
        return addGenericCommands(
                builder,
                FallingStarCommand::getSchedulePresetLists,
                PresetsCommand::presetSchedulesList,
                PresetsCommand::presetScheduleCreate,
                PresetsCommand::presetSchedulesDelete,
                PresetsCommand::presetSchedulesInfo
        );
    }

    public static <T> void updateConfigAndSave(ConfigFolderManager<T> manager, String preset, T config) {
        manager.setConfig(preset, config);
        manager.saveConfig(preset);
    }

    public static Component presetEnabledState(String preset, boolean value) {
        return Component.literal("Preset " + preset + " has been " + (value ? "enabled" : "disabled") + ".").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @SuppressWarnings("unchecked")
    public static <T> void presetEnableDisable(CommandContext<CommandSourceStack> context, ConfigFolderManager<T> manager, boolean value) {
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
            case SchedulePresetConfig schedulePresetConfig -> {
                schedulePresetConfig.enabled = value;
                updateConfigAndSave(manager, preset, (T) schedulePresetConfig);
                context.getSource().sendSystemMessage(presetEnabledState(preset, value));
            }
            default -> context.getSource().sendFailure(Component.literal("Preset not found: " + preset).withStyle(ChatFormatting.RED));
        }
    }

    public static int presetEventEnable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), true);
        return 1;
    }

    public static int presetVisualsEnable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), true);
        return 1;
    }

    public static int presetEventDisable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), false);
        return 1;
    }

    public static int presetVisualsDisable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), false);
        return 1;
    }

    public static int presetScheduleEnable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getSchedulesConfigManager(), true);
        return 1;
    }

    public static int presetScheduleDisable(CommandContext<CommandSourceStack> context) {
        presetEnableDisable(context, FallingStarRewards.CONFIG_MANAGER.getSchedulesConfigManager(), false);
        return 1;
    }

    public static int presetEventsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(),
                "No event presets found.",
                "Event Presets",
                config -> config.enabled ? "Enabled" : "Disabled"
        );
    }

    public static int presetRewardsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(),
                "No reward presets found.",
                "Reward Presets",
                config -> "Enabled"
        );
    }

    public static int presetVisualsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(),
                "No visuals presets found.",
                "Visual Presets",
                config -> config.enabled ? "Enabled" : "Disabled"
        );
    }

    public static int presetSchedulesList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FallingStarRewards.CONFIG_MANAGER.getSchedulesConfigManager(),
                "No schedule presets found.",
                "Schedule Presets",
                config -> config.enabled ? "Enabled" : "Disabled"
        );
    }

    @SuppressWarnings("SameReturnValue")
    public static <T> int presetList(
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

    public static int presetEventCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), "event", "Event");
    }

    public static int presetRewardsCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(), "reward", "Reward");
    }

    public static int presetVisualsCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), "visuals", "Visuals");
    }

    public static int presetScheduleCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FallingStarRewards.CONFIG_MANAGER.getSchedulesConfigManager(), "schedule", "Schedule");
    }

    public static <T> int presetCreate(
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

    public static int presetEventSetRewards(CommandContext<CommandSourceStack> context) {
        return presetEventSet(
                context,
                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(),
                "reward",
                (eventConfig, presetId) -> eventConfig.rewardsPresetId = presetId,
                "Rewards"
        );
    }

    public static int presetEventSetVisuals(CommandContext<CommandSourceStack> context) {
        return presetEventSet(
                context,
                FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(),
                "visuals",
                (eventConfig, presetId) -> eventConfig.visualsPresetId = presetId,
                "Visuals"
        );
    }

    public static <T> int presetEventSet(
            CommandContext<CommandSourceStack> context,
            ConfigFolderManager<T> targetManager,
            String targetType,
            BiConsumer<EventPresetConfig, String> setter,
            String label
    ) {
        String eventPresetId = StringArgumentType.getString(context, "name");
        String presetId = StringArgumentType.getString(context, "preset_id");

        var eventManager = FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager();
        if (!eventManager.hasConfig(eventPresetId)) {
            context.getSource().sendFailure(Component.literal("Event preset not found: " + eventPresetId).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!targetManager.hasConfig(presetId)) {
            context.getSource().sendFailure(Component.literal(capitalize(targetType) + " preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }

        EventPresetConfig eventConfig = eventManager.getConfig(eventPresetId);
        setter.accept(eventConfig, presetId);
        updateConfigAndSave(eventManager, eventPresetId, eventConfig);

        context.getSource().sendSystemMessage(Component.literal(
                label + " preset for event '" + eventPresetId + "' has been set to '" + presetId + "'."
        ).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    public static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Preset";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    public static int presetEventsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(),
                "event",
                PresetsCommand::buildEventPresetInfo
        );
    }

    public static int presetRewardsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(),
                "reward",
                PresetsCommand::buildRewardPresetInfo
        );
    }

    public static int presetVisualsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(),
                "visuals",
                PresetsCommand::buildVisualsPresetInfo
        );
    }

    public static int presetSchedulesInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getSchedulesConfigManager(),
                "schedule",
                PresetsCommand::buildSchedulePresetInfo
        );
    }

    public static <T> int presetInfo(
            CommandContext<CommandSourceStack> context,
            ConfigFolderManager<T> manager,
            String presetType,
            Function<NamedPreset<T>, Component> infoBuilder
    ) {
        String name = StringArgumentType.getString(context, "name");
        if (!manager.hasConfig(name)) {
            context.getSource().sendFailure(Component.literal(capitalize(presetType) + " preset not found: " + name).withStyle(ChatFormatting.RED));
            return 0;
        }

        T config = manager.getConfig(name);
        context.getSource().sendSystemMessage(infoBuilder.apply(new NamedPreset<>(name, config)));
        return 1;
    }

    public static Component buildEventPresetInfo(NamedPreset<EventPresetConfig> preset) {
        EventPresetConfig config = preset.config();
        return new ChatTableBuilder("Event Preset: " + preset.name())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Rewards Preset", config.rewardsPresetId)
                .addRow("Visuals Preset", config.visualsPresetId)
                .addSection("Spawn")
                .addRow("Target Scope", config.spawn.targetScope)
                .addRow("Min Radius", Integer.toString(config.spawn.minRadius))
                .addRow("Max Radius", Integer.toString(config.spawn.maxRadius))
                .addRow("Max Location Attempts", Integer.toString(config.spawn.maxLocationAttempts))
                .addRow("Allow Water Spawns", Boolean.toString(config.spawn.allowWaterSpawns))
                .build();
    }

    public static Component buildSchedulePresetInfo(NamedPreset<SchedulePresetConfig> preset) {
        SchedulePresetConfig config = preset.config();
        SchedulePresetConfig.Conditions conditions = config.conditions == null ? new SchedulePresetConfig.Conditions() : config.conditions;
        SchedulePresetConfig.State state = config.state == null ? new SchedulePresetConfig.State() : config.state;
        return new ChatTableBuilder("Schedule Preset: " + preset.name())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Base Tick Interval", Integer.toString(config.baseIntervalTicks))
                .addRow("Interval Jitter", Integer.toString(config.intervalJitterTicks))
                .addRow("Max Stars Per Cycle", Integer.toString(config.maxStarsPerCycle))
                .addRow("Selection Mode", config.selectionMode)
                .addRow("Event Entries", Integer.toString(config.eventEntries == null ? 0 : config.eventEntries.size()))
                .addSection("Conditions")
                .addRow("Time Mode", conditions.timeMode)
                .addRow("Require Surface Access", Boolean.toString(conditions.requireSurfaceAccess))
                .addRow("Weather Mode", conditions.weatherMode)
                .addRow("Moon Phases", conditions.moonPhases == null || conditions.moonPhases.isEmpty()
                        ? "Any"
                        : String.join(", ", conditions.moonPhases))
                .addSection("State")
                .addRow("Rotation Cursor", Integer.toString(state.rotationCursor))
                .build();
    }

    public static Component buildVisualsPresetInfo(NamedPreset<VisualsPresetConfig> preset) {
        VisualsPresetConfig config = preset.config();
        return new ChatTableBuilder("Visuals Preset: " + preset.name())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Particle Preset", config.particlePreset)
                .addRow("Fall Distance", Integer.toString(config.fallDistance))
                .addRow("Emission Interval Ticks", Integer.toString(config.emissionIntervalTicks))
                .addRow("Particles Per Emission", Integer.toString(config.particlesPerEmission))
                .addSection("Travel Sound")
                .addRow("Enabled", Boolean.toString(config.travelSound.enabled))
                .addRow("Id", config.travelSound.id)
                .addRow("Volume", Float.toString(config.travelSound.volume))
                .addRow("Pitch Min", Float.toString(config.travelSound.pitchMin))
                .addRow("Pitch Max", Float.toString(config.travelSound.pitchMax))
                .addRow("Interval Ticks", Integer.toString(config.travelSound.intervalTicks))
                .addSection("Impact")
                .addRow("Burst Enabled", Boolean.toString(config.impact.burstEnabled))
                .addRow("Particle Preset", config.impact.particlePreset)
                .addRow("Particle Count", Integer.toString(config.impact.particleCount))
                .addRow("Spread", Double.toString(config.impact.spread))
                .addRow("Sound Enabled", Boolean.toString(config.impact.soundEnabled))
                .addRow("Sound Id", config.impact.soundId)
                .addRow("Sound Volume", Float.toString(config.impact.soundVolume))
                .addRow("Sound Pitch Min", Float.toString(config.impact.soundPitchMin))
                .addRow("Sound Pitch Max", Float.toString(config.impact.soundPitchMax))
                .build();
    }

    public static Component buildRewardPresetInfo(NamedPreset<RewardsPresetConfig> preset) {
        RewardsPresetConfig config = preset.config();
        ChatTableBuilder builder = new ChatTableBuilder("Reward Preset: " + preset.name())
                .addSection("Summary")
                .addRow("Entries", Integer.toString(config.entries.length));

        for (int i = 0; i < config.entries.length; i++) {
            RewardsPresetConfig.RewardEntry entry = config.entries[i];
            builder.addSection("Entry " + (i + 1))
                    .addRow("Id", entry.id)
                    .addRow("Weight", Integer.toString(entry.weight))
                    .addRow("Min Count", Integer.toString(entry.minCount))
                    .addRow("Max Count", Integer.toString(entry.maxCount))
                    .addRow("Custom Model Data", entry.customModelData == null ? "None" : entry.customModelData.toString())
                    .addRow("Custom Data", entry.customData == null || entry.customData.isBlank() ? "None" : entry.customData);
        }

        return builder.build();
    }

    public static int presetRewardsAddHeldItem(CommandContext<CommandSourceStack> context) {
        String presetName = StringArgumentType.getString(context, "name");
        int weight = IntegerArgumentType.getInteger(context, "weight");
        int min = IntegerArgumentType.getInteger(context, "min");
        int max = IntegerArgumentType.getInteger(context, "max");
        ConfigFolderManager<RewardsPresetConfig> manager = FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager();

        if (!manager.hasConfig(presetName)) {
            context.getSource().sendFailure(Component.literal("Reward preset not found: " + presetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("This command can only be used by a player holding an item.").withStyle(ChatFormatting.RED));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.literal("You must hold an item in your main hand to use this command.").withStyle(ChatFormatting.RED));
            return 0;
        }

        RewardsPresetConfig.RewardEntry entry = buildRewardEntryFromHeldItem(stack, weight, min, max);
        RewardsPresetConfig config = manager.getConfig(presetName);
        config.entries = config.entries == null ? new RewardsPresetConfig.RewardEntry[] { entry } : Arrays.copyOf(config.entries, config.entries.length + 1);
        config.entries[config.entries.length - 1] = entry;
        updateConfigAndSave(manager, presetName, config);

        context.getSource().sendSystemMessage(
                new ChatTableBuilder("Reward Entry Added From Held Item")
                        .addSection("Preset")
                        .addRow("Name", presetName)
                        .addSection("Entry")
                        .addRow("Item Id", entry.id)
                        .addRow("Weight", Integer.toString(entry.weight))
                        .addRow("Min Count", Integer.toString(entry.minCount))
                        .addRow("Max Count", Integer.toString(entry.maxCount))
                        .addRow("Custom Model Data", entry.customModelData == null ? "None" : entry.customModelData.toString())
                        .addRow("Custom Data", entry.customData == null || entry.customData.isBlank() ? "None" : entry.customData)
                        .build()
        );
        return 1;
    }

    public static int presetRewardsRemove(CommandContext<CommandSourceStack> context) {
        String presetName = StringArgumentType.getString(context, "name");
        String itemId = StringArgumentType.getString(context, "item_id");
        ConfigFolderManager<RewardsPresetConfig> manager = FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager();

        if (!manager.hasConfig(presetName)) {
            context.getSource().sendFailure(Component.literal("Reward preset not found: " + presetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        RewardsPresetConfig config = manager.getConfig(presetName);
        if (config.entries == null || config.entries.length == 0) {
            context.getSource().sendFailure(Component.literal("Reward preset '" + presetName + "' has no entries to remove.").withStyle(ChatFormatting.RED));
            return 0;
        }

        int removedCount = 0;
        RewardsPresetConfig.RewardEntry[] filtered = new RewardsPresetConfig.RewardEntry[config.entries.length];
        int keptCount = 0;
        for (RewardsPresetConfig.RewardEntry entry : config.entries) {
            boolean matches = entry != null && itemId.equals(entry.id);
            if (matches) {
                removedCount++;
                continue;
            }

            filtered[keptCount++] = entry;
        }

        filtered = Arrays.copyOf(filtered, keptCount);

        if (removedCount == 0) {
            context.getSource().sendFailure(Component.literal("No reward entry with item id '" + itemId + "' was found in preset '" + presetName + "'.").withStyle(ChatFormatting.RED));
            return 0;
        }

        config.entries = filtered;
        updateConfigAndSave(manager, presetName, config);

        context.getSource().sendSystemMessage(
                new ChatTableBuilder("Reward Entry Removed")
                        .addSection("Preset")
                        .addRow("Name", presetName)
                        .addSection("Removal")
                        .addRow("Item Id", itemId)
                        .addRow("Entries Removed", Integer.toString(removedCount))
                        .addRow("Remaining Entries", Integer.toString(config.entries.length))
                        .build()
        );
        return 1;
    }

    public static RewardsPresetConfig.RewardEntry buildRewardEntryFromHeldItem(ItemStack stack, int weight, int min, int max) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer customModelData = null;
        if (stack.has(DataComponents.CUSTOM_MODEL_DATA)) {
            CustomModelData modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (modelData != null) {
                customModelData = modelData.value();
            }
        }

        String customData = null;
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                CompoundTag tag = data.copyTag();
                customData = tag.toString();
            }
        }

        return new RewardsPresetConfig.RewardEntry(itemId, weight, min, max, customModelData, customData);
    }

    public static int presetRewardsAdd(CommandContext<CommandSourceStack> context) {
        return presetRewardsAdd(context, null, null);
    }

    public static int presetRewardsAddWithCustomModelData(CommandContext<CommandSourceStack> context) {
        int customModelData = IntegerArgumentType.getInteger(context, "custom_model_data");
        return presetRewardsAdd(context, customModelData, null);
    }

    public static int presetRewardsAddWithCustomModelDataAndCustomData(CommandContext<CommandSourceStack> context) {
        int customModelData = IntegerArgumentType.getInteger(context, "custom_model_data");
        String customData = StringArgumentType.getString(context, "custom_data");
        return presetRewardsAdd(context, customModelData, customData);
    }

    public static int presetRewardsAdd(CommandContext<CommandSourceStack> context, Integer customModelData, String customData) {
        String presetName = StringArgumentType.getString(context, "name");
        String itemId = StringArgumentType.getString(context, "item_id");
        int weight = IntegerArgumentType.getInteger(context, "weight");
        int min = IntegerArgumentType.getInteger(context, "min");
        int max = IntegerArgumentType.getInteger(context, "max");

        ConfigFolderManager<RewardsPresetConfig> manager = FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager();
        if (!manager.hasConfig(presetName)) {
            context.getSource().sendFailure(Component.literal("Reward preset not found: " + presetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        RewardsPresetConfig config = manager.getConfig(presetName);
        RewardsPresetConfig.RewardEntry entry = new RewardsPresetConfig.RewardEntry(itemId, weight, min, max, customModelData, customData);

        config.entries = config.entries == null ? new RewardsPresetConfig.RewardEntry[] { entry } : Arrays.copyOf(config.entries, config.entries.length + 1);
        config.entries[config.entries.length - 1] = entry;
        updateConfigAndSave(manager, presetName, config);

        context.getSource().sendSystemMessage(
                new ChatTableBuilder("Reward Entry Added")
                        .addSection("Preset")
                        .addRow("Name", presetName)
                        .addSection("Entry")
                        .addRow("Item Id", itemId)
                        .addRow("Weight", Integer.toString(weight))
                        .addRow("Min Count", Integer.toString(min))
                        .addRow("Max Count", Integer.toString(max))
                        .addRow("Custom Model Data", customModelData == null ? "None" : customModelData.toString())
                        .addRow("Custom Data", customData == null || customData.isBlank() ? "None" : customData)
                        .build()
        );
        return 1;
    }

    // This should be a random set of characters roughly 8 characters long
    public static String generateDeletionKey() {
        int length = 8;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder key = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            key.append(chars.charAt(random.nextInt(chars.length())));
        }
        return key.toString();
    }

    public static int presetEventsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(), PresetTypes.EVENT, "event");
    }

    public static int presetRewardsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(), PresetTypes.REWARDS, "rewards");
    }

    public static int presetVisualsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(), PresetTypes.VISUALS, "visuals");
    }

    public static int presetSchedulesDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, FallingStarRewards.CONFIG_MANAGER.getSchedulesConfigManager(), PresetTypes.SCHEDULE, "schedule");
    }

    public static <T> int presetDelete(
            CommandContext<CommandSourceStack> context,
            ConfigFolderManager<T> manager,
            PresetTypes presetType,
            String presetTypeLabel
    ) {
        FallingStarCommand.pruneExpiredDeletionRequests();
        String name = StringArgumentType.getString(context, "name");
        if (!manager.getConfigs().containsKey(name)) {
            String capitalizedType = presetTypeLabel.substring(0, 1).toUpperCase() + presetTypeLabel.substring(1);
            context.getSource().sendFailure(Component.literal(capitalizedType + " preset not found: " + name).withStyle(ChatFormatting.RED));
            return 0;
        }
        String eventKey = generateDeletionKey();
        PresetDeletionRequest request = new PresetDeletionRequest(presetType, name, System.currentTimeMillis());
        FallingStarCommand.putDeletionRequest(eventKey, request);
        long ttlMinutes = FallingStarCommand.getDeletionRequestTtlMinutes();
        context.getSource().sendSystemMessage(Component
                .literal(
                        "Are you sure you want to delete the " + presetTypeLabel + " preset '" + name +
                                "'? This action cannot be undone. If you're sure, run the command: /fallingstar confirm-delete " +
                                eventKey + " (expires in " + ttlMinutes + " minutes)."
                )
                .withStyle(ChatFormatting.YELLOW)
        );
        return 1;
    }
}
