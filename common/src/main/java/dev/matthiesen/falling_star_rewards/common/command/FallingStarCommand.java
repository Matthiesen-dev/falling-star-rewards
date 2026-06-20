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
import dev.matthiesen.falling_star_rewards.common.config.presets.RewardsPresetConfig;
import dev.matthiesen.falling_star_rewards.common.config.presets.VisualsPresetConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.nbt.CompoundTag;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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
 *     /fallingstar preset [events|visuals|rewards] info [name]
 *     /fallingstar preset events set [reward|visuals] [name] [preset name]
 *     /fallingstar preset rewards add [name] [item_id] [weight] [min] [max] (custom_model_data) (custom_data)
 *     /fallingstar preset rewards add-held-item [name]
 *
 *     Planned:
 *
 *     /fallingstar preset rewards remove [name] [item_id]
 *</pre>
 */
public final class FallingStarCommand extends AbstractCommand {
    public static final FallingStarCommand CMD = new FallingStarCommand();
    private static final Map<String, PresetDeletionRequest> DELETION_REQUESTS = new LinkedHashMap<>();
    private static final long DELETION_REQUEST_TTL_MS = 5L * 60L * 1000L;
    private static final int HELP_PAGE_COUNT = 4;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        dispatcher.register(
                new CommandBuilder("fallingstar", src -> src.hasPermission(4))
                        .then("help", help -> help
                                .executes(this::help)
                                .argument("page", IntegerArgumentType.integer(1, HELP_PAGE_COUNT), page -> page
                                        .executes(this::helpPage)
                                )
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
                                                        .executes(this::presetEventsInfo)
                                                )
                                        )
                                        .then("set", set -> set
                                                .then("rewards", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .suggests(this::getEventsPresetLists)
                                                                .then(Commands.argument("preset_id", StringArgumentType.string())
                                                                        .suggests(this::getRewardsPresetLists)
                                                                        .executes(this::presetEventSetRewards)
                                                                )
                                                        )
                                                )
                                                .then("visuals", rewards -> rewards
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .suggests(this::getEventsPresetLists)
                                                                .then(Commands.argument("preset_id", StringArgumentType.string())
                                                                        .suggests(this::getVisualsPresetLists)
                                                                        .executes(this::presetEventSetVisuals)
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
                                                        .executes(this::presetRewardsInfo)
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
                                                                                                .executes(this::presetRewardsAdd)
                                                                                                .then(Commands.argument("custom_model_data", IntegerArgumentType.integer())
                                                                                                        .executes(this::presetRewardsAddWithCustomModelData)
                                                                                                        .then(Commands.argument("custom_data", StringArgumentType.string())
                                                                                                                .executes(this::presetRewardsAddWithCustomModelDataAndCustomData)
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
                                                            .executes(this::presetRewardsAddHeldItem)
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
                                                        .executes(this::presetVisualsInfo)
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

    private int presetEventSetRewards(CommandContext<CommandSourceStack> context) {
        return presetEventSet(
                context,
                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(),
                "reward",
                (eventConfig, presetId) -> eventConfig.rewardsPresetId = presetId,
                "Rewards"
        );
    }

    private int presetEventSetVisuals(CommandContext<CommandSourceStack> context) {
        return presetEventSet(
                context,
                FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(),
                "visuals",
                (eventConfig, presetId) -> eventConfig.visualsPresetId = presetId,
                "Visuals"
        );
    }

    private <T> int presetEventSet(
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

    private int presetEventsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager(),
                "event",
                this::buildEventPresetInfo
        );
    }

    private int presetRewardsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager(),
                "reward",
                this::buildRewardPresetInfo
        );
    }

    private int presetVisualsInfo(CommandContext<CommandSourceStack> context) {
        return presetInfo(
                context,
                FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager(),
                "visuals",
                this::buildVisualsPresetInfo
        );
    }

    private <T> int presetInfo(
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

    private Component buildEventPresetInfo(NamedPreset<EventPresetConfig> preset) {
        EventPresetConfig config = preset.config();
        return new ChatTableBuilder("Event Preset: " + preset.name())
                .addSection("General")
                .addRow("Enabled", Boolean.toString(config.enabled))
                .addRow("Rewards Preset", config.rewardsPresetId)
                .addRow("Visuals Preset", config.visualsPresetId)
                .addSection("Activation")
                .addRow("Require Night", Boolean.toString(config.activation.requireNight))
                .addRow("Require Surface Access", Boolean.toString(config.activation.requireSurfaceAccess))
                .addRow("Weather Mode", config.activation.weatherMode)
                .addSection("Spawn")
                .addRow("Target Scope", config.spawn.targetScope)
                .addRow("Min Radius", Integer.toString(config.spawn.minRadius))
                .addRow("Max Radius", Integer.toString(config.spawn.maxRadius))
                .addRow("Max Location Attempts", Integer.toString(config.spawn.maxLocationAttempts))
                .addRow("Allow Water Spawns", Boolean.toString(config.spawn.allowWaterSpawns))
                .build();
    }

    private Component buildVisualsPresetInfo(NamedPreset<VisualsPresetConfig> preset) {
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

    private Component buildRewardPresetInfo(NamedPreset<RewardsPresetConfig> preset) {
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

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Preset";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private record NamedPreset<T>(String name, T config) {
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
        pruneExpiredDeletionRequests();
        String name = StringArgumentType.getString(context, "name");
        if (!manager.getConfigs().containsKey(name)) {
            String capitalizedType = presetTypeLabel.substring(0, 1).toUpperCase() + presetTypeLabel.substring(1);
            context.getSource().sendFailure(Component.literal(capitalizedType + " preset not found: " + name).withStyle(ChatFormatting.RED));
            return 0;
        }
        String eventKey = generateDeletionKey();
        PresetDeletionRequest request = new PresetDeletionRequest(presetType, name, System.currentTimeMillis());
        DELETION_REQUESTS.put(eventKey, request);
        long ttlMinutes = DELETION_REQUEST_TTL_MS / 60_000L;
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

    private int confirmDelete(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "event_id");

        pruneExpiredDeletionRequests();
        PresetDeletionRequest request = DELETION_REQUESTS.remove(key);
        if (request == null) {
            context.getSource().sendFailure(Component.literal("Invalid or expired deletion key: " + key).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (isDeletionRequestExpired(request)) {
            context.getSource().sendFailure(Component.literal("Deletion key has expired. Please run the delete command again.").withStyle(ChatFormatting.RED));
            return 0;
        }

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

    private void pruneExpiredDeletionRequests() {
        DELETION_REQUESTS.entrySet().removeIf(entry -> isDeletionRequestExpired(entry.getValue()));
    }

    private boolean isDeletionRequestExpired(PresetDeletionRequest request) {
        return System.currentTimeMillis() - request.createdAtMs() > DELETION_REQUEST_TTL_MS;
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
        context.getSource().sendSystemMessage(buildHelpTable(1));
        return 1;
    }

    public int helpPage(CommandContext<CommandSourceStack> context) {
        int page = IntegerArgumentType.getInteger(context, "page");
        if (page < 1 || page > HELP_PAGE_COUNT) {
            context.getSource().sendFailure(Component.literal("Help page must be between 1 and " + HELP_PAGE_COUNT + ".").withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource().sendSystemMessage(buildHelpTable(page));
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

    private Component buildHelpTable(int page) {
        return switch (page) {
            case 1 -> new ChatTableBuilder("Falling Star Rewards Commands (Page 1/" + HELP_PAGE_COUNT + ")")
                .addSection("General")
                .addRow("/fallingstar help", "Show this command list")
                .addRow("/fallingstar help <page>", "Show a specific help page")
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
            case 2 -> new ChatTableBuilder("Falling Star Rewards Commands (Page 2/" + HELP_PAGE_COUNT + ")")
                .addSection("Event Presets")
                .addRow("/fallingstar preset events list", "List event presets")
                .addRow("/fallingstar preset events enable <name>", "Enable an event preset")
                .addRow("/fallingstar preset events disable <name>", "Disable an event preset")
                .addRow("/fallingstar preset events create <name>", "Create an event preset")
                .addRow("/fallingstar preset events delete <name>", "Delete an event preset")
                .addRow("/fallingstar preset events info <name>", "Show event preset details")
                .addRow("/fallingstar preset events set rewards <name> <preset_id>", "Set the rewards preset used by an event preset")
                .addRow("/fallingstar preset events set visuals <name> <preset_id>", "Set the visuals preset used by an event preset")
                .build();
            case 3 -> new ChatTableBuilder("Falling Star Rewards Commands (Page 3/" + HELP_PAGE_COUNT + ")")
                .addSection("Reward Presets")
                .addRow("/fallingstar preset rewards list", "List reward presets")
                .addRow("/fallingstar preset rewards create <name>", "Create a reward preset")
                .addRow("/fallingstar preset rewards delete <name>", "Delete a reward preset")
                .addRow("/fallingstar preset rewards info <name>", "Show reward preset details")
                .addRow("/fallingstar preset rewards add <name> <item_id> <weight> <min> <max>", "Add a reward entry")
                .addRow("/fallingstar preset rewards add <name> <item_id> <weight> <min> <max> <custom_model_data>", "Add a reward entry with custom model data")
                .addRow("/fallingstar preset rewards add <name> <item_id> <weight> <min> <max> <custom_model_data> <custom_data>", "Add a reward entry with custom model data and custom data")
                .addRow("/fallingstar preset rewards add-held-item <name>", "Add the held item as a reward entry")
                .addRow("/fallingstar preset rewards remove <name> <item_id>", "Remove a reward entry from a preset")
                .build();
            case 4 -> new ChatTableBuilder("Falling Star Rewards Commands (Page 4/" + HELP_PAGE_COUNT + ")")
                .addSection("Visual Presets")
                .addRow("/fallingstar preset visuals list", "List visuals presets")
                .addRow("/fallingstar preset visuals enable <name>", "Enable a visuals preset")
                .addRow("/fallingstar preset visuals disable <name>", "Disable a visuals preset")
                .addRow("/fallingstar preset visuals create <name>", "Create a visuals preset")
                .addRow("/fallingstar preset visuals delete <name>", "Delete a visuals preset")
                .addRow("/fallingstar preset visuals info <name>", "Show visuals preset details")
                .build();
            default -> throw new IllegalStateException("Unexpected help page: " + page);
        };
    }

    private int presetRewardsAdd(CommandContext<CommandSourceStack> context) {
        return presetRewardsAdd(context, null, null);
    }

    private int presetRewardsAddWithCustomModelData(CommandContext<CommandSourceStack> context) {
        int customModelData = IntegerArgumentType.getInteger(context, "custom_model_data");
        return presetRewardsAdd(context, customModelData, null);
    }

    private int presetRewardsAddWithCustomModelDataAndCustomData(CommandContext<CommandSourceStack> context) {
        int customModelData = IntegerArgumentType.getInteger(context, "custom_model_data");
        String customData = StringArgumentType.getString(context, "custom_data");
        return presetRewardsAdd(context, customModelData, customData);
    }

    private int presetRewardsAdd(CommandContext<CommandSourceStack> context, Integer customModelData, String customData) {
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

    private int presetRewardsAddHeldItem(CommandContext<CommandSourceStack> context) {
        String presetName = StringArgumentType.getString(context, "name");
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

        RewardsPresetConfig.RewardEntry entry = buildRewardEntryFromHeldItem(stack);
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

    private RewardsPresetConfig.RewardEntry buildRewardEntryFromHeldItem(ItemStack stack) {
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

        return new RewardsPresetConfig.RewardEntry(itemId, 1, stack.getCount(), stack.getCount(), customModelData, customData);
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

    public record PresetDeletionRequest(PRESET_TYPES presetType, String presetName, long createdAtMs) {
        public enum PRESET_TYPES {
                EVENT,
                REWARDS,
                VISUALS
            }
        }
}
