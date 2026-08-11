package dev.matthiesen.falling_star_rewards.common.config;

import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PermissionsStartupConfig {

    // Command Permissions
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_help;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_reload;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_cleanup;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_status;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_force;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_confirm_delete;

    // Preset Command Permissions
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_enable;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_disable;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_list;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_create;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_delete;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_info;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_set;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_add;
    public ModConfigSpec.EnumValue<PermissionLevel> command_fallingstar_preset_remove;

    public PermissionsStartupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Permission Configuration").push("permissions");
        builder.comment("Command Permissions").push("command");

        command_fallingstar = builder.comment("Permission level for the /fallingstar command")
                .defineEnum("fallingstar", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_help = builder.comment("Permission level for the /fallingstar help command")
                .defineEnum("fallingstar_help", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_reload = builder.comment("Permission level for the /fallingstar reload command")
                .defineEnum("fallingstar_reload", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_cleanup = builder.comment("Permission level for the /fallingstar cleanup command")
                .defineEnum("fallingstar_cleanup", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_status = builder.comment("Permission level for the /fallingstar status command")
                .defineEnum("fallingstar_status", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_force = builder.comment("Permission level for the /fallingstar force command")
                .defineEnum("fallingstar_force", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_confirm_delete = builder.comment("Permission level for the /fallingstar confirm_delete command")
                .defineEnum("fallingstar_confirm_delete", PermissionLevel.ALL_COMMANDS);

        builder.comment("Preset Command Permissions").push("preset");
        command_fallingstar_preset_enable = builder.comment("Permission level for the /fallingstar preset enable command")
                .defineEnum("fallingstar_preset_enable", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_disable = builder.comment("Permission level for the /fallingstar preset disable command")
                .defineEnum("fallingstar_preset_disable", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_list = builder.comment("Permission level for the /fallingstar preset list command")
                .defineEnum("fallingstar_preset_list", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_create = builder.comment("Permission level for the /fallingstar preset create command")
                .defineEnum("fallingstar_preset_create", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_delete = builder.comment("Permission level for the /fallingstar preset delete command")
                .defineEnum("fallingstar_preset_delete", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_info = builder.comment("Permission level for the /fallingstar preset info command")
                .defineEnum("fallingstar_preset_info", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_set = builder.comment("Permission level for the /fallingstar preset set command")
                .defineEnum("fallingstar_preset_set", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_add = builder.comment("Permission level for the /fallingstar preset add command")
                .defineEnum("fallingstar_preset_add", PermissionLevel.ALL_COMMANDS);
        command_fallingstar_preset_remove = builder.comment("Permission level for the /fallingstar preset remove command")
                .defineEnum("fallingstar_preset_remove", PermissionLevel.ALL_COMMANDS);
        builder.pop();

        builder.pop();
        builder.pop();
    }
}
