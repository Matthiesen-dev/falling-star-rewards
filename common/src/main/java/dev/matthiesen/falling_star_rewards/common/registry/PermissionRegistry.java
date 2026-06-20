package dev.matthiesen.falling_star_rewards.common.registry;

import dev.matthiesen.common.matthiesen_lib_api.MatthiesenLibApi;
import dev.matthiesen.common.matthiesen_lib_api.permission.AbstractPermission;
import dev.matthiesen.common.matthiesen_lib_api.permission.Permission;
import dev.matthiesen.common.matthiesen_lib_api.permission.PermissionLevel;
import dev.matthiesen.falling_star_rewards.common.FallingStarRewards;
import net.minecraft.commands.CommandSourceStack;

public final class PermissionRegistry {
    public static Permission COMMAND_ROOT = register(
            "command.fallingstar.root",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.root
    );
    public static Permission COMMAND_FALLINGSTAR_HELP = register(
            "command.fallingstar.help",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.help
    );
    public static Permission COMMAND_FALLINGSTAR_RELOAD = register(
            "command.fallingstar.reload",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.reload
    );
    public static Permission COMMAND_FALLINGSTAR_CLEANUP = register(
            "command.fallingstar.cleanup",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.cleanup
    );
    public static Permission COMMAND_FALLINGSTAR_STATUS = register(
            "command.fallingstar.status",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.status
    );
    public static Permission COMMAND_FALLINGSTAR_FORCE = register(
            "command.fallingstar.force",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.force
    );
    public static Permission COMMAND_FALLINGSTAR_CONFIRM_DELETE = register(
            "command.fallingstar.confirm_delete",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.confirmDelete
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_ENABLE = register(
            "command.fallingstar.preset.enable",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.enable
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_DISABLE = register(
            "command.fallingstar.preset.disable",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.disable
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_LIST = register(
            "command.fallingstar.preset.list",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.list
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_CREATE = register(
            "command.fallingstar.preset.create",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.create
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_DELETE = register(
            "command.fallingstar.preset.delete",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.delete
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_INFO = register(
            "command.fallingstar.preset.info",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.info
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_SET = register(
            "command.fallingstar.preset.set",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.set
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_ADD = register(
            "command.fallingstar.preset.add",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.add
    );
    public static Permission COMMAND_FALLINGSTAR_PRESET_REMOVE = register(
            "command.fallingstar.preset.remove",
        FallingStarRewards.INSTANCE.getPermissionsConfig().command.fallingstar.preset.remove
    );

    public static class Permissions {
        public Permission COMMAND_ROOT = PermissionRegistry.COMMAND_ROOT;
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
        return MatthiesenLibApi.getPermissionValidator().hasPermission(source, permission);
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
        MatthiesenLibApi.registerPermission(newPermission);
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
