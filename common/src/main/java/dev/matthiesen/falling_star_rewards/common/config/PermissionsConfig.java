package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.annotations.SerializedName;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;

public final class PermissionsConfig {
    @SerializedName("command")
    public CommandPermissions command = new CommandPermissions();

    public static class CommandPermissions {
        @SerializedName("root")
        public int root = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("fallingstar")
        public FallingstarSubCommands fallingstar = new FallingstarSubCommands();
    }

    public static class FallingstarSubCommands {
        @SerializedName("help")
        public int help = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("reload")
        public int reload = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("cleanup")
        public int cleanup = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("status")
        public int status = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("force")
        public int force = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("confirm-delete")
        public int confirmDelete = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("preset")
        public PresetSubCommands preset = new PresetSubCommands();
    }

    public static class PresetSubCommands {
        @SerializedName("enable")
        public int enable = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("disable")
        public int disable = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("list")
        public int list = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("create")
        public int create = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("delete")
        public int delete = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("info")
        public int info = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("set")
        public int set = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("add")
        public int add = PermissionLevel.ALL_COMMANDS.getLevel();

        @SerializedName("remove")
        public int remove = PermissionLevel.ALL_COMMANDS.getLevel();
    }
}
