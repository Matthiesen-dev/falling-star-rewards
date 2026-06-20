package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.common.matthiesen_lib_api.config.ConfigFolderManager;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.interfaces.PresetDeletionRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class ConfirmDeleteCommand {
    public static CommandBuilder getConfirmDeleteSubCommand() {
        return new CommandBuilder("confirm-delete", FallingStarRewards.getPermissionPredicate(
                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_CONFIRM_DELETE
        ))
                .argument("event_id", StringArgumentType.string(), eventId -> eventId
                        .executes(ConfirmDeleteCommand::confirmDelete)
                );
    }

    public static int confirmDelete(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "event_id");

        FallingStarCommand.pruneExpiredDeletionRequests();
        PresetDeletionRequest request = FallingStarCommand.removeDeletionRequest(key);
        if (request == null) {
            context.getSource().sendFailure(Component.literal("Invalid or expired deletion key: " + key).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (FallingStarCommand.isDeletionRequestExpired(request)) {
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
}
