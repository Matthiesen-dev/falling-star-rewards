package dev.matthiesen.falling_star_rewards.common.command.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.common.matthiesen_lib_api.utility.CommandBuilder;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class ForceCommand {
    public static CommandBuilder getForceSubCommand() {
        return new CommandBuilder("force")
                .executes(ForceCommand::forceOnce)
                .argument("preset", StringArgumentType.string(), preset -> preset
                        .suggests(FallingStarCommand::getEventsPresetLists)
                        .executes(ForceCommand::forcePreset)
                );
    }

    public static int forceOnce(CommandContext<CommandSourceStack> context) {
        return executeForce(context, null);
    }

    public static int forcePreset(CommandContext<CommandSourceStack> context) {
        String preset = StringArgumentType.getString(context, "preset");
        return executeForce(context, preset);
    }

    private static int executeForce(CommandContext<CommandSourceStack> context, String presetId) {
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
