package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import dev.matthiesen.common.matthiesen_lib_api.permission.PermissionLevel;

public final class PermissionsConfig {
    @SerializedName("command")
    public CommandPermissions command = new CommandPermissions();

    public static class CommandPermissions {
        @SerializedName("root")
        public int root = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("fallingstar")
        public FallingstarSubCommands fallingstar = new FallingstarSubCommands();
    }

    public static class FallingstarSubCommands {
        @SerializedName("help")
        public int help = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("reload")
        public int reload = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("cleanup")
        public int cleanup = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("status")
        public int status = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("force")
        public int force = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("confirm-delete")
        public int confirmDelete = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("preset")
        public PresetSubCommands preset = new PresetSubCommands();
    }

    public static class PresetSubCommands {
        @SerializedName("enable")
        public int enable = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("disable")
        public int disable = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("list")
        public int list = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("create")
        public int create = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("delete")
        public int delete = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("info")
        public int info = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("set")
        public int set = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("add")
        public int add = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();

        @SerializedName("remove")
        public int remove = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getNumericalValue();
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
