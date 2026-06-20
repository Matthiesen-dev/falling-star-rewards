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
        public int root = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("fallingstar")
        public FallingstarSubCommands fallingstar = new FallingstarSubCommands();
    }

    public static class FallingstarSubCommands {
        @SerializedName("help")
        public int help = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("reload")
        public int reload = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("cleanup")
        public int cleanup = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("status")
        public int status = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("force")
        public int force = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("confirm-delete")
        public int confirmDelete = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("preset")
        public PresetSubCommands preset = new PresetSubCommands();
    }

    public static class PresetSubCommands {
        @SerializedName("enable")
        public int enable = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("disable")
        public int disable = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("list")
        public int list = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("create")
        public int create = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("delete")
        public int delete = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("info")
        public int info = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("set")
        public int set = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("add")
        public int add = PermissionLevel.ALL_COMMANDS.getNumericalValue();

        @SerializedName("remove")
        public int remove = PermissionLevel.ALL_COMMANDS.getNumericalValue();
    }

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
