package dev.matthiesen.falling_star_rewards.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class FSConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_CONFIG_SPEC;

    public static final PermissionsStartupConfig PERMISSIONS_START_CONFIG;
    public static final ModConfigSpec PERMISSIONS_START_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = specPair.getLeft();
        SERVER_CONFIG_SPEC = specPair.getRight();

        Pair<PermissionsStartupConfig, ModConfigSpec> permissionsSpecPair = new ModConfigSpec.Builder().configure(PermissionsStartupConfig::new);
        PERMISSIONS_START_CONFIG = permissionsSpecPair.getLeft();
        PERMISSIONS_START_SPEC = permissionsSpecPair.getRight();
    }
}
