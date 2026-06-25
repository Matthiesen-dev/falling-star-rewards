package dev.matthiesen.falling_star_rewards.common;

import dev.matthiesen.common.matthiesen_lib_api.abstracts.AbstractCommonMod;
import dev.matthiesen.common.matthiesen_lib_api.core.interfaces.MatthiesenLibServerEventHandler;
import dev.matthiesen.common.matthiesen_lib_api.permission.Permission;
import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.config.FallingStarsConfigManager;
import dev.matthiesen.falling_star_rewards.common.config.AnnouncementsConfig;
import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import dev.matthiesen.falling_star_rewards.common.config.PermissionsConfig;
import dev.matthiesen.falling_star_rewards.common.registry.PermissionRegistry;
import dev.matthiesen.falling_star_rewards.common.runtime.RuntimeManager;
import dev.matthiesen.libs.faststats.Token;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class FallingStarRewards extends AbstractCommonMod {
    public static final String MOD_ID = "falling_star_rewards";
    private static final String MOD_NAME = "Falling Star Rewards";
    private static @Token final String METRICS_TOKEN = "3b8d656e1efa1d6eaa2ec90c7ad832bd";
    public static final FallingStarRewards INSTANCE;
    public static final FallingStarsConfigManager CONFIG_MANAGER;

    public static PermissionRegistry.Permissions getPermissions() {
        return PermissionRegistry.getPermissions();
    }

    public static Predicate<CommandSourceStack> getPermissionPredicate(Permission permission) {
        return source -> PermissionRegistry.checkPermission(source, permission);
    }

    static {
        INSTANCE = new FallingStarRewards();
        CONFIG_MANAGER = new FallingStarsConfigManager(INSTANCE);
    }

    public FallingStarRewards() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public void initialize() {
        super.initialize();
        CONFIG_MANAGER.init();

        reload().run();
        PermissionRegistry.init();
        registerServerEventHandler(getServerEventHandler());
        registerCommand(FallingStarCommand.CMD);
        createInfoLog("Initializing Falling Star Rewards");
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    @Override
    public Runnable reload() {
        return () -> {
            loadConfigs();
            MainConfig config = getMainConfig();
            CONFIG_MANAGER.validateRewardsConfigs();
            createInfoLog("Reloaded Config (enabled=" + config.enabled + ")");
        };
    }

    public void loadConfigs() {
        CONFIG_MANAGER.getMainConfigManager().loadConfig();
        CONFIG_MANAGER.getAnnouncementsConfigManager().loadConfig();
        CONFIG_MANAGER.getEventsConfigManager().loadConfigs();
        CONFIG_MANAGER.getRewardsConfigManager().loadConfigs();
        CONFIG_MANAGER.getVisualsConfigManager().loadConfigs();
        CONFIG_MANAGER.getSchedulesConfigManager().loadConfigs();
    }

    public MainConfig getMainConfig() {
        return CONFIG_MANAGER.getMainConfigManager().getConfig();
    }

    public AnnouncementsConfig getAnnouncementsConfig() {
        return CONFIG_MANAGER.getAnnouncementsConfigManager().getConfig();
    }

    public PermissionsConfig getPermissionsConfig() {
        return CONFIG_MANAGER.getPermissionsConfigManager().getConfig();
    }

    public FallingStarsConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }

    public long getNextCycleTick() {
        return RuntimeManager.getNextCycleTick();
    }

    public int getActiveDropCount() {
        return RuntimeManager.getActiveDropCount();
    }

    public int cleanupActiveDrops(MinecraftServer server) {
        return RuntimeManager.cleanupActiveDrops(server);
    }

    public int forceCycle(MinecraftServer server, String presetId, boolean bypassActivationChecks) {
        MainConfig config = getMainConfig();
        if (!config.enabled) {
            createInfoLog("Cannot force cycle - mod is disabled");
            return 0;
        }
        var preset = presetId != null ? CONFIG_MANAGER.loadPresetConfig(presetId) : CONFIG_MANAGER.loadRandomEventPreset();
        if (preset == null) {
            createWarnLog("No event presets available to start a cycle");
            return 0;
        }
        return RuntimeManager.runCycle(server, config, preset, getAnnouncementsConfig(), bypassActivationChecks);
    }

    public MatthiesenLibServerEventHandler getServerEventHandler() {
        return new MatthiesenLibServerEventHandler() {
            @Override
            public void onServerStart(MinecraftServer server) {
                createInfoLog("Server Started");
            }

            @Override
            public void onServerTick(MinecraftServer server) {
                MainConfig config = getMainConfig();
                if (!config.enabled) return;
                RuntimeManager.tick(server, config, getAnnouncementsConfig());
            }

            @Override
            public void onServerStop(MinecraftServer server) {
                createInfoLog("Server Stopped");
            }
        };
    }

    public void createWarnLog(String message) {
        getLogger().warn(message);
    }
}
