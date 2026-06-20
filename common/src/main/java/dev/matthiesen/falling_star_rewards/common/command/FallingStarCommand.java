package dev.matthiesen.falling_star_rewards.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.matthiesen.common.matthiesen_lib_api.command.AbstractCommand;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigFolderManager;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.interfaces.PresetDeletionRequest;
import dev.matthiesen.falling_star_rewards.common.command.subcommands.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
 *     /fallingstar preset rewards add-held-item [name] [weight] [min] [max]
 *     /fallingstar preset rewards remove [name] [item_id]
 *
 *     Planned:
 *
 *     N/A
 *</pre>
 */
public final class FallingStarCommand extends AbstractCommand {
    public static final FallingStarCommand CMD = new FallingStarCommand();
    private static final Map<String, PresetDeletionRequest> DELETION_REQUESTS = new LinkedHashMap<>();
    private static final long DELETION_REQUEST_TTL_MS = 5L * 60L * 1000L;

    public static void pruneExpiredDeletionRequests() {
        DELETION_REQUESTS.entrySet().removeIf(entry -> isDeletionRequestExpired(entry.getValue()));
    }

    public static boolean isDeletionRequestExpired(PresetDeletionRequest request) {
        return System.currentTimeMillis() - request.createdAtMs() > DELETION_REQUEST_TTL_MS;
    }

    public static long getDeletionRequestTtlMinutes() {
        return DELETION_REQUEST_TTL_MS / 60_000L;
    }

    public static void putDeletionRequest(String key, PresetDeletionRequest request) {
        DELETION_REQUESTS.put(key, request);
    }

    public static PresetDeletionRequest removeDeletionRequest(String key) {
        return DELETION_REQUESTS.remove(key);
    }

    public static CompletableFuture<Suggestions> getPresetList(SuggestionsBuilder builder, ConfigFolderManager<?> manager) {
        manager.getConfigs().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    @SuppressWarnings("unused")
    public static CompletableFuture<Suggestions> getEventsPresetLists(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return getPresetList(builder, FallingStarRewards.CONFIG_MANAGER.getEventsConfigManager());
    }

    @SuppressWarnings("unused")
    public static CompletableFuture<Suggestions> getRewardsPresetLists(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return getPresetList(builder, FallingStarRewards.CONFIG_MANAGER.getRewardsConfigManager());
    }

    @SuppressWarnings("unused")
    public static CompletableFuture<Suggestions> getVisualsPresetLists(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return getPresetList(builder, FallingStarRewards.CONFIG_MANAGER.getVisualsConfigManager());
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        dispatcher.register(
                new CommandBuilder("fallingstar", src -> src.hasPermission(4))
                        .then(getReloadSubCommand())
                        .then(getCleanupSubCommand())
                        .then(HelpCommand.getHelpSubCommand())
                        .then(StatusCommand.getStatusSubCommand())
                        .then(ForceCommand.getForceSubCommand())
                        .then(ConfirmDeleteCommand.getConfirmDeleteSubCommand())
                        .then(PresetsCommand.getPresetSubCommand())
                        .build()
        );
    }

    public CommandBuilder getReloadSubCommand() {
        return new CommandBuilder("reload")
                .executes(this::reload);
    }

    public CommandBuilder getCleanupSubCommand() {
        return new CommandBuilder("cleanup")
                .executes(this::cleanup);
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

    public int cleanup(CommandContext<CommandSourceStack> context) {
        int removed = FallingStarRewards.INSTANCE.cleanupActiveDrops(context.getSource().getServer());
        context.getSource().sendSystemMessage(Component.literal(
                "Removed tracked star drops: " + removed
        ).withStyle(ChatFormatting.YELLOW));
        return removed;
    }
}
