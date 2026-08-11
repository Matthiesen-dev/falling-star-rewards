package dev.matthiesen.falling_star_rewards.common.registry;

import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import dev.matthiesen.falling_star_rewards.common.config.FSConfig;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import dev.matthiesen.matthiesen_core.common.utility.AbstractPermission;
import net.minecraft.commands.CommandSourceStack;

public final class PermissionRegistry {
    public static Permission COMMAND_FALLINGSTAR = register(
            "command.fallingstar",
            FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_HELP = register(
            "command.fallingstar.help",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_help.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_RELOAD = register(
            "command.fallingstar.reload",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_reload.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_CLEANUP = register(
            "command.fallingstar.cleanup",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_cleanup.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_STATUS = register(
            "command.fallingstar.status",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_status.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_FORCE = register(
            "command.fallingstar.force",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_force.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_CONFIRM_DELETE = register(
            "command.fallingstar.confirm_delete",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_confirm_delete.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_ENABLE = register(
            "command.fallingstar.preset.enable",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_enable.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_DISABLE = register(
            "command.fallingstar.preset.disable",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_disable.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_LIST = register(
            "command.fallingstar.preset.list",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_list.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_CREATE = register(
            "command.fallingstar.preset.create",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_create.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_DELETE = register(
            "command.fallingstar.preset.delete",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_delete.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_INFO = register(
            "command.fallingstar.preset.info",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_info.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_SET = register(
            "command.fallingstar.preset.set",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_set.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_ADD = register(
            "command.fallingstar.preset.add",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_add.get().getLevel()
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_REMOVE = register(
            "command.fallingstar.preset.remove",
        FSConfig.PERMISSIONS_START_CONFIG.command_fallingstar_preset_remove.get().getLevel()
    );

    public static class Permissions {
        public Permission COMMAND_FALLINGSTAR = PermissionRegistry.COMMAND_FALLINGSTAR;
        public Permission COMMAND_FALLINGSTAR_HELP = PermissionRegistry.COMMAND_FALLINGSTAR_HELP;
        public Permission COMMAND_FALLINGSTAR_RELOAD = PermissionRegistry.COMMAND_FALLINGSTAR_RELOAD;
        public Permission COMMAND_FALLINGSTAR_CLEANUP = PermissionRegistry.COMMAND_FALLINGSTAR_CLEANUP;
        public Permission COMMAND_FALLINGSTAR_STATUS = PermissionRegistry.COMMAND_FALLINGSTAR_STATUS;
        public Permission COMMAND_FALLINGSTAR_FORCE = PermissionRegistry.COMMAND_FALLINGSTAR_FORCE;
        public Permission COMMAND_FALLINGSTAR_CONFIRM_DELETE = PermissionRegistry.COMMAND_FALLINGSTAR_CONFIRM_DELETE;
        public Permission COMMAND_FALLINGSTAR_PRESET_ENABLE = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_ENABLE;
        public Permission COMMAND_FALLINGSTAR_PRESET_DISABLE = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_DISABLE;
        public Permission COMMAND_FALLINGSTAR_PRESET_LIST = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_LIST;
        public Permission COMMAND_FALLINGSTAR_PRESET_CREATE = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_CREATE;
        public Permission COMMAND_FALLINGSTAR_PRESET_DELETE = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_DELETE;
        public Permission COMMAND_FALLINGSTAR_PRESET_INFO = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_INFO;
        public Permission COMMAND_FALLINGSTAR_PRESET_SET = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_SET;
        public Permission COMMAND_FALLINGSTAR_PRESET_ADD = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_ADD;
        public Permission COMMAND_FALLINGSTAR_PRESET_REMOVE = PermissionRegistry.COMMAND_FALLINGSTAR_PRESET_REMOVE;
    }

    public static Permissions getPermissions() {
        return new Permissions();
    }

    public static void init() {}

    public static boolean checkPermission(CommandSourceStack source, Permission permission) {
        return FallingStarRewards.INSTANCE.getPermissionsManager().getPermissionValidator().hasPermission(source, permission);
    }

    public static PermissionLevel toPermLevel(int permLevel) {
        for (PermissionLevel value : PermissionLevel.values()) {
            if (value.ordinal() == permLevel) {
                return value;
            }
        }
        return PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS;
    }

    private static Permission register(String node, int level) {
        var newPermission = modPermission(node, toPermLevel(level));
        FallingStarRewards.INSTANCE.getPermissionsManager().registerPermission(newPermission);
        return newPermission;
    }

    private static Permission modPermission(String node, PermissionLevel level) {
        return new AbstractPermission(node, level) {
            @Override
            protected String getModId() {
                return FallingStarRewards.MOD_ID;
            }

            @Override
            protected String getPermissionNamespace() {
                return "FallingStarRewards";
            }
        };
    }
}
