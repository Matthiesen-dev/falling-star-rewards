package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.config.FSConfig;
import dev.matthiesen.falling_star_rewards.common.config.def.*;
import dev.matthiesen.falling_star_rewards.common.config.def.reward.RewardEntry;
import dev.matthiesen.falling_star_rewards.common.config.def.schedule.Conditions;
import dev.matthiesen.falling_star_rewards.common.interfaces.PresetDeletionRequest;
import dev.matthiesen.falling_star_rewards.common.interfaces.PresetTypes;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public static Component presetEnabledState(String preset, boolean value) {
        return Component.literal("Preset " + preset + " has been " + (value ? "enabled" : "disabled") + ".").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    public static int presetEventEnable(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "name");
        EventPreset preset = FSConfig.getEventPreset(presetId);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Event preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }
        FSConfig.setEventPreset(new EventPreset(
                preset.eventId(),
                true,
                preset.rewardsPresetId(),
                preset.visualsPresetId(),
                preset.commands(),
                preset.spawn(),
                preset.announcement()
        ));
        context.getSource().sendSystemMessage(presetEnabledState(presetId, true));
        return 1;
    }

    public static int presetVisualsEnable(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "name");
        VisualsPreset preset = FSConfig.getVisualsPreset(presetId);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Visuals preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }
        FSConfig.setVisualsPreset(new VisualsPreset(
                preset.visualsId(),
                true,
                preset.particlePreset(),
                preset.fallDistance(),
                preset.emissionIntervalTicks(),
                preset.particlesPerEmission(),
                preset.impact(),
                preset.travelSound()
        ));
        context.getSource().sendSystemMessage(presetEnabledState(presetId, true));
        return 1;
    }

    public static int presetEventDisable(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "name");
        EventPreset preset = FSConfig.getEventPreset(presetId);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Event preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }
        FSConfig.setEventPreset(new EventPreset(
                preset.eventId(),
                false,
                preset.rewardsPresetId(),
                preset.visualsPresetId(),
                preset.commands(),
                preset.spawn(),
                preset.announcement()
        ));
        context.getSource().sendSystemMessage(presetEnabledState(presetId, false));
        return 1;
    }

    public static int presetVisualsDisable(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "name");
        VisualsPreset preset = FSConfig.getVisualsPreset(presetId);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Visuals preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }
        FSConfig.setVisualsPreset(new VisualsPreset(
                preset.visualsId(),
                false,
                preset.particlePreset(),
                preset.fallDistance(),
                preset.emissionIntervalTicks(),
                preset.particlesPerEmission(),
                preset.impact(),
                preset.travelSound()
        ));
        context.getSource().sendSystemMessage(presetEnabledState(presetId, false));
        return 1;
    }

    public static int presetScheduleEnable(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "name");
        SchedulePreset preset = FSConfig.getSchedulePreset(presetId);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Schedule preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }
        FSConfig.setSchedulePreset(new SchedulePreset(
                preset.scheduleId(),
                true,
                preset.baseIntervalTicks(),
                preset.intervalJitterTicks(),
                preset.maxStarsPerCycle(),
                preset.selectionMode(),
                preset.eventEntries(),
                preset.conditions(),
                preset.state()
        ));
        context.getSource().sendSystemMessage(presetEnabledState(presetId, true));
        return 1;
    }

    public static int presetScheduleDisable(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "name");
        SchedulePreset preset = FSConfig.getSchedulePreset(presetId);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Schedule preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }
        FSConfig.setSchedulePreset(new SchedulePreset(
                preset.scheduleId(),
                false,
                preset.baseIntervalTicks(),
                preset.intervalJitterTicks(),
                preset.maxStarsPerCycle(),
                preset.selectionMode(),
                preset.eventEntries(),
                preset.conditions(),
                preset.state()
        ));
        context.getSource().sendSystemMessage(presetEnabledState(presetId, false));
        return 1;
    }

    public static int presetEventsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FSConfig.getEventPresets(),
                "No event presets found.",
                "Event Presets",
                EventPreset::eventId,
                config -> config.enabled() ? "Enabled" : "Disabled"
        );
    }

    public static int presetRewardsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FSConfig.getRewardPresets(),
                "No reward presets found.",
                "Reward Presets",
                RewardPreset::rewardId,
                config -> "Enabled"
        );
    }

    public static int presetVisualsList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FSConfig.getVisualsPresets(),
                "No visuals presets found.",
                "Visual Presets",
                VisualsPreset::visualsId,
                config -> config.enabled() ? "Enabled" : "Disabled"
        );
    }

    public static int presetSchedulesList(CommandContext<CommandSourceStack> context) {
        return presetList(
                context,
                FSConfig.getSchedulePresets(),
                "No schedule presets found.",
                "Schedule Presets",
                SchedulePreset::scheduleId,
                config -> config.enabled() ? "Enabled" : "Disabled"
        );
    }

    @SuppressWarnings("SameReturnValue")
    public static <T> int presetList(
            CommandContext<CommandSourceStack> context,
            List<T> presets,
            String emptyMessage,
            String tableTitle,
            Function<T, String> idResolver,
            Function<T, String> statusResolver
    ) {
        if (presets.isEmpty()) {
            context.getSource().sendSystemMessage(Component.literal(emptyMessage).withStyle(ChatFormatting.YELLOW));
            return 1;
        }

        var chatMessage = new ChatTableBuilder(tableTitle);
        presets.forEach(config -> chatMessage.addRow(idResolver.apply(config), statusResolver.apply(config)));
        context.getSource().sendSystemMessage(chatMessage.build());
        return 1;
    }

    public static int presetEventCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FSConfig::createEventPreset, "event", "Event");
    }

    public static int presetRewardsCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FSConfig::createRewardPreset, "reward", "Reward");
    }

    public static int presetVisualsCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FSConfig::createVisualsPreset, "visuals", "Visuals");
    }

    public static int presetScheduleCreate(CommandContext<CommandSourceStack> context) {
        return presetCreate(context, FSConfig::createSchedulePreset, "schedule", "Schedule");
    }

    public static int presetCreate(
            CommandContext<CommandSourceStack> context,
            Function<String, Boolean> creator,
            String presetType,
            String presetTitle
    ) {
        String name = StringArgumentType.getString(context, "name");
        if (!creator.apply(name)) {
            context.getSource().sendFailure(Component.literal("A " + presetType + " preset with that name already exists.").withStyle(ChatFormatting.RED));
            return 0;
        }
        context.getSource().sendSystemMessage(Component.literal(presetTitle + " preset '" + name + "' created successfully.").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    public static int presetEventSetRewards(CommandContext<CommandSourceStack> context) {
        return presetEventSet(
                context,
                "reward",
                FSConfig::hasRewardPreset,
                (eventPreset, presetId) -> new EventPreset(
                        eventPreset.eventId(),
                        eventPreset.enabled(),
                        presetId,
                        eventPreset.visualsPresetId(),
                        eventPreset.commands(),
                        eventPreset.spawn(),
                        eventPreset.announcement()
                ),
                "Rewards"
        );
    }

    public static int presetEventSetVisuals(CommandContext<CommandSourceStack> context) {
        return presetEventSet(
                context,
                "visuals",
                FSConfig::hasVisualsPreset,
                (eventPreset, presetId) -> new EventPreset(
                        eventPreset.eventId(),
                        eventPreset.enabled(),
                        eventPreset.rewardsPresetId(),
                        presetId,
                        eventPreset.commands(),
                        eventPreset.spawn(),
                        eventPreset.announcement()
                ),
                "Visuals"
        );
    }

    public static int presetEventSet(
            CommandContext<CommandSourceStack> context,
            String targetType,
            Predicate<String> targetExists,
            BiFunction<EventPreset, String, EventPreset> setter,
            String label
    ) {
        String eventPresetId = StringArgumentType.getString(context, "name");
        String presetId = StringArgumentType.getString(context, "preset_id");

        EventPreset eventPreset = FSConfig.getEventPreset(eventPresetId);
        if (eventPreset == null) {
            context.getSource().sendFailure(Component.literal("Event preset not found: " + eventPresetId).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!targetExists.test(presetId)) {
            context.getSource().sendFailure(Component.literal(capitalize(targetType) + " preset not found: " + presetId).withStyle(ChatFormatting.RED));
            return 0;
        }

        FSConfig.setEventPreset(setter.apply(eventPreset, presetId));

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
                FSConfig::getEventPreset,
                "event",
                PresetsCommand::buildEventPresetInfo
        );
    }

    public static int presetRewardsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FSConfig::getRewardPreset,
                "reward",
                PresetsCommand::buildRewardPresetInfo
        );
    }

    public static int presetVisualsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FSConfig::getVisualsPreset,
                "visuals",
                PresetsCommand::buildVisualsPresetInfo
        );
    }

    public static int presetSchedulesInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FSConfig::getSchedulePreset,
                "schedule",
                PresetsCommand::buildSchedulePresetInfo
        );
    }

    public static <T> int presetInfo(
            CommandContext<CommandSourceStack> context,
            Function<String, T> getter,
            String presetType,
            Function<T, Component> infoBuilder
    ) {
        String name = StringArgumentType.getString(context, "name");
        T config = getter.apply(name);
        if (config == null) {
            context.getSource().sendFailure(Component.literal(capitalize(presetType) + " preset not found: " + name).withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource().sendSystemMessage(infoBuilder.apply(config));
        return 1;
    }

    public static Component buildEventPresetInfo(EventPreset preset) {
        return new ChatTableBuilder("Event Preset: " + preset.eventId())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(preset.enabled()))
                .addRow("Rewards Preset", preset.rewardsPresetId())
                .addRow("Visuals Preset", preset.visualsPresetId())
                .addSection("Spawn")
                .addRow("Target Scope", preset.spawn().targetScope().name().toLowerCase(Locale.ROOT))
                .addRow("Min Radius", Integer.toString(preset.spawn().minRadius()))
                .addRow("Max Radius", Integer.toString(preset.spawn().maxRadius()))
                .addRow("Max Location Attempts", Integer.toString(preset.spawn().maxLocationAttempts()))
                .addRow("Allow Water Spawns", Boolean.toString(preset.spawn().allowWaterSpawns()))
                .build();
    }

    public static Component buildSchedulePresetInfo(SchedulePreset preset) {
        Conditions conditions = preset.conditions();
        SchedulePreset.State state = preset.state();
        return new ChatTableBuilder("Schedule Preset: " + preset.scheduleId())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(preset.enabled()))
                .addRow("Base Tick Interval", Integer.toString(preset.baseIntervalTicks()))
                .addRow("Interval Jitter", Integer.toString(preset.intervalJitterTicks()))
                .addRow("Max Stars Per Cycle", Integer.toString(preset.maxStarsPerCycle()))
                .addRow("Selection Mode", preset.selectionMode().name().toLowerCase(Locale.ROOT))
                .addRow("Event Entries", Integer.toString(preset.eventEntries() == null ? 0 : preset.eventEntries().size()))
                .addSection("Conditions")
                .addRow("Time Mode", conditions.timeMode().name().toLowerCase(Locale.ROOT))
                .addRow("Require Surface Access", Boolean.toString(conditions.requireSurfaceAccess()))
                .addRow("Weather Mode", conditions.weatherMode().name().toLowerCase(Locale.ROOT))
                .addRow("Moon Phases", conditions.moonPhases() == null || conditions.moonPhases().isEmpty()
                        ? "Any"
                        : String.join(", ", conditions.moonPhases()))
                .addSection("State")
                .addRow("Rotation Cursor", Integer.toString(state.rotationCursor))
                .build();
    }

    public static Component buildVisualsPresetInfo(VisualsPreset preset) {
        return new ChatTableBuilder("Visuals Preset: " + preset.visualsId())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(preset.enabled()))
                .addRow("Particle Preset", preset.particlePreset().name().toLowerCase(Locale.ROOT))
                .addRow("Fall Distance", Integer.toString(preset.fallDistance()))
                .addRow("Emission Interval Ticks", Integer.toString(preset.emissionIntervalTicks()))
                .addRow("Particles Per Emission", Integer.toString(preset.particlesPerEmission()))
                .addSection("Travel Sound")
                .addRow("Enabled", Boolean.toString(preset.travelSound().enabled()))
                .addRow("Id", preset.travelSound().id())
                .addRow("Volume", Float.toString(preset.travelSound().volume()))
                .addRow("Pitch Min", Float.toString(preset.travelSound().pitchMin()))
                .addRow("Pitch Max", Float.toString(preset.travelSound().pitchMax()))
                .addRow("Interval Ticks", Integer.toString(preset.travelSound().intervalTicks()))
                .addSection("Impact")
                .addRow("Burst Enabled", Boolean.toString(preset.impact().burstEnabled()))
                .addRow("Particle Preset", preset.impact().particlePreset().name().toLowerCase(Locale.ROOT))
                .addRow("Particle Count", Integer.toString(preset.impact().particleCount()))
                .addRow("Spread", Double.toString(preset.impact().spread()))
                .addRow("Sound Enabled", Boolean.toString(preset.impact().soundEnabled()))
                .addRow("Sound Id", preset.impact().soundId())
                .addRow("Sound Volume", Float.toString(preset.impact().soundVolume()))
                .addRow("Sound Pitch Min", Float.toString(preset.impact().soundPitchMin()))
                .addRow("Sound Pitch Max", Float.toString(preset.impact().soundPitchMax()))
                .build();
    }

    public static Component buildRewardPresetInfo(RewardPreset preset) {
        List<RewardEntry> entries = preset.entries() == null ? List.of() : preset.entries();

        ChatTableBuilder builder = new ChatTableBuilder("Reward Preset: " + preset.rewardId())
                .addSection("Summary")
                .addRow("Entries", Integer.toString(entries.size()));

        for (int i = 0; i < entries.size(); i++) {
            RewardEntry entry = entries.get(i);
            builder.addSection("Entry " + (i + 1))
                    .addRow("Id", entry.itemId())
                    .addRow("Weight", Integer.toString(entry.weight()))
                    .addRow("Min Count", Integer.toString(entry.minCount()))
                    .addRow("Max Count", Integer.toString(entry.maxCount()))
                    .addRow("Custom Model Data", entry.customModelData() == null ? "None" : entry.customModelData().toString())
                    .addRow("Custom Data", entry.customData() == null || entry.customData().isBlank() ? "None" : entry.customData());
        }

        return builder.build();
    }

    public static int presetRewardsAddHeldItem(CommandContext<CommandSourceStack> context) {
        String presetName = StringArgumentType.getString(context, "name");
        int weight = IntegerArgumentType.getInteger(context, "weight");
        int min = IntegerArgumentType.getInteger(context, "min");
        int max = IntegerArgumentType.getInteger(context, "max");

        RewardPreset preset = FSConfig.getRewardPreset(presetName);
        if (preset == null) {
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

        RewardEntry entry = buildRewardEntryFromHeldItem(stack, weight, min, max);
        List<RewardEntry> updatedEntries = new ArrayList<>(preset.entries() == null ? List.of() : preset.entries());
        updatedEntries.add(entry);
        FSConfig.setRewardPreset(new RewardPreset(preset.rewardId(), List.copyOf(updatedEntries)));

        context.getSource().sendSystemMessage(
                new ChatTableBuilder("Reward Entry Added From Held Item")
                        .addSection("Preset")
                        .addRow("Name", presetName)
                        .addSection("Entry")
                        .addRow("Item Id", entry.itemId())
                        .addRow("Weight", Integer.toString(entry.weight()))
                        .addRow("Min Count", Integer.toString(entry.minCount()))
                        .addRow("Max Count", Integer.toString(entry.maxCount()))
                        .addRow("Custom Model Data", entry.customModelData() == null ? "None" : entry.customModelData().toString())
                        .addRow("Custom Data", entry.customData() == null || entry.customData().isBlank() ? "None" : entry.customData())
                        .build()
        );
        return 1;
    }

    public static int presetRewardsRemove(CommandContext<CommandSourceStack> context) {
        String presetName = StringArgumentType.getString(context, "name");
        String itemId = StringArgumentType.getString(context, "item_id");

        RewardPreset preset = FSConfig.getRewardPreset(presetName);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Reward preset not found: " + presetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        List<RewardEntry> entries = preset.entries() == null ? List.of() : preset.entries();
        if (entries.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Reward preset '" + presetName + "' has no entries to remove.").withStyle(ChatFormatting.RED));
            return 0;
        }

        int before = entries.size();
        List<RewardEntry> filtered = entries.stream()
                .filter(entry -> entry == null || !itemId.equals(entry.itemId()))
                .toList();
        int removedCount = before - filtered.size();

        if (removedCount == 0) {
            context.getSource().sendFailure(Component.literal("No reward entry with item id '" + itemId + "' was found in preset '" + presetName + "'.").withStyle(ChatFormatting.RED));
            return 0;
        }

        FSConfig.setRewardPreset(new RewardPreset(preset.rewardId(), filtered));

        context.getSource().sendSystemMessage(
                new ChatTableBuilder("Reward Entry Removed")
                        .addSection("Preset")
                        .addRow("Name", presetName)
                        .addSection("Removal")
                        .addRow("Item Id", itemId)
                        .addRow("Entries Removed", Integer.toString(removedCount))
                        .addRow("Remaining Entries", Integer.toString(filtered.size()))
                        .build()
        );
        return 1;
    }

    public static RewardEntry buildRewardEntryFromHeldItem(ItemStack stack, int weight, int min, int max) {
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

        return new RewardEntry(itemId, weight, min, max, customModelData, customData);
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

        RewardPreset preset = FSConfig.getRewardPreset(presetName);
        if (preset == null) {
            context.getSource().sendFailure(Component.literal("Reward preset not found: " + presetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        RewardEntry entry = new RewardEntry(itemId, weight, min, max, customModelData, customData);
        List<RewardEntry> updatedEntries = new ArrayList<>(preset.entries() == null ? List.of() : preset.entries());
        updatedEntries.add(entry);
        FSConfig.setRewardPreset(new RewardPreset(preset.rewardId(), List.copyOf(updatedEntries)));

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
        return presetDelete(context, PresetTypes.EVENT, "event", FSConfig::hasEventPreset);
    }

    public static int presetRewardsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, PresetTypes.REWARDS, "rewards", FSConfig::hasRewardPreset);
    }

    public static int presetVisualsDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, PresetTypes.VISUALS, "visuals", FSConfig::hasVisualsPreset);
    }

    public static int presetSchedulesDelete(CommandContext<CommandSourceStack> context) {
        return presetDelete(context, PresetTypes.SCHEDULE, "schedule", FSConfig::hasSchedulePreset);
    }

    public static int presetDelete(
            CommandContext<CommandSourceStack> context,
            PresetTypes presetType,
            String presetTypeLabel,
            Predicate<String> existsPredicate
    ) {
        FallingStarCommand.pruneExpiredDeletionRequests();
        String name = StringArgumentType.getString(context, "name");
        if (!existsPredicate.test(name)) {
            String capitalizedType = presetTypeLabel.substring(0, 1).toUpperCase(Locale.ROOT) + presetTypeLabel.substring(1);
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
