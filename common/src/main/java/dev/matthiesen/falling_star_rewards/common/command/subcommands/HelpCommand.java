package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.common.matthiesen_lib_api.utility.ChatTableBuilder;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class HelpCommand {
    private static final int HELP_PAGE_COUNT = 4;

    public static CommandBuilder getHelpSubCommand() {
        return new CommandBuilder("help", FallingStarRewards.getPermissionPredicate(
                FallingStarRewards.getPermissions().COMMAND_FALLINGSTAR_HELP
        ))
                .executes(HelpCommand::help)
                .argument("page", IntegerArgumentType.integer(1, HELP_PAGE_COUNT), page -> page
                        .executes(HelpCommand::helpPage)
                );
    }

    public static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(buildHelpTable(1));
        return 1;
    }

    public static int helpPage(CommandContext<CommandSourceStack> context) {
        int page = IntegerArgumentType.getInteger(context, "page");
        if (page < 1 || page > HELP_PAGE_COUNT) {
            context.getSource().sendFailure(Component.literal("Help page must be between 1 and " + HELP_PAGE_COUNT + ".").withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource().sendSystemMessage(buildHelpTable(page));
        return 1;
    }

    private static Component buildHelpTable(int page) {
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
                    .addRow("/fallingstar preset events set {rewards|visuals} <name> <preset_id>", "Sets the rewards or visuals preset used by an event preset")
                    .build();
            case 3 -> new ChatTableBuilder("Falling Star Rewards Commands (Page 3/" + HELP_PAGE_COUNT + ")")
                    .addSection("Reward Presets")
                    .addRow("/fallingstar preset rewards list", "List reward presets")
                    .addRow("/fallingstar preset rewards create <name>", "Create a reward preset")
                    .addRow("/fallingstar preset rewards delete <name>", "Delete a reward preset")
                    .addRow("/fallingstar preset rewards info <name>", "Show reward preset details")
                    .addRow("/fallingstar preset rewards add <name> <item_id> <weight> <min> <max> (custom_model_data) (custom_data)", "Add a reward entry with optional custom model data and custom data")
                    .addRow("/fallingstar preset rewards add-held-item <name> <weight> <min> <max>", "Add the held item as a reward entry")
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
}
