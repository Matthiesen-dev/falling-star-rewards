package dev.matthiesen.falling_star_rewards.common.config;

import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PermissionsStartupConfig {

    // Command Permissions
    public ModConfigSpec.IntValue command_fallingstar;
    public ModConfigSpec.IntValue command_fallingstar_help;
    public ModConfigSpec.IntValue command_fallingstar_reload;
    public ModConfigSpec.IntValue command_fallingstar_cleanup;
    public ModConfigSpec.IntValue command_fallingstar_status;
    public ModConfigSpec.IntValue command_fallingstar_force;
    public ModConfigSpec.IntValue command_fallingstar_confirm_delete;

    // Preset Command Permissions
    public ModConfigSpec.IntValue command_fallingstar_preset_enable;
    public ModConfigSpec.IntValue command_fallingstar_preset_disable;
    public ModConfigSpec.IntValue command_fallingstar_preset_list;
    public ModConfigSpec.IntValue command_fallingstar_preset_create;
    public ModConfigSpec.IntValue command_fallingstar_preset_delete;
    public ModConfigSpec.IntValue command_fallingstar_preset_info;
    public ModConfigSpec.IntValue command_fallingstar_preset_set;
    public ModConfigSpec.IntValue command_fallingstar_preset_add;
    public ModConfigSpec.IntValue command_fallingstar_preset_remove;

    public PermissionsStartupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Permission Configuration").push("permissions");
        builder.comment("Command Permissions").push("command");

        command_fallingstar = builder.comment("Permission level for the /fallingstar command")
                .defineInRange("fallingstar", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_help = builder.comment("Permission level for the /fallingstar help command")
                .defineInRange("fallingstar_help", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_reload = builder.comment("Permission level for the /fallingstar reload command")
                .defineInRange("fallingstar_reload", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_cleanup = builder.comment("Permission level for the /fallingstar cleanup command")
                .defineInRange("fallingstar_cleanup", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_status = builder.comment("Permission level for the /fallingstar status command")
                .defineInRange("fallingstar_status", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_force = builder.comment("Permission level for the /fallingstar force command")
                .defineInRange("fallingstar_force", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_confirm_delete = builder.comment("Permission level for the /fallingstar confirm_delete command")
                .defineInRange("fallingstar_confirm_delete", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);

        builder.comment("Preset Command Permissions").push("preset");
        command_fallingstar_preset_enable = builder.comment("Permission level for the /fallingstar preset enable command")
                .defineInRange("fallingstar_preset_enable", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_disable = builder.comment("Permission level for the /fallingstar preset disable command")
                .defineInRange("fallingstar_preset_disable", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_list = builder.comment("Permission level for the /fallingstar preset list command")
                .defineInRange("fallingstar_preset_list", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_create = builder.comment("Permission level for the /fallingstar preset create command")
                .defineInRange("fallingstar_preset_create", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_delete = builder.comment("Permission level for the /fallingstar preset delete command")
                .defineInRange("fallingstar_preset_delete", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_info = builder.comment("Permission level for the /fallingstar preset info command")
                .defineInRange("fallingstar_preset_info", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_set = builder.comment("Permission level for the /fallingstar preset set command")
                .defineInRange("fallingstar_preset_set", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_add = builder.comment("Permission level for the /fallingstar preset add command")
                .defineInRange("fallingstar_preset_add", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        command_fallingstar_preset_remove = builder.comment("Permission level for the /fallingstar preset remove command")
                .defineInRange("fallingstar_preset_remove", PermissionLevel.ALL_COMMANDS.getLevel(), 0, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();
        builder.pop();
    }
}
