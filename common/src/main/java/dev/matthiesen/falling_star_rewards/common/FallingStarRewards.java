package dev.matthiesen.falling_star_rewards.common;

import dev.matthiesen.falling_star_rewards.common.command.FallingStarCommand;
import dev.matthiesen.falling_star_rewards.common.config.FSConfig;
import dev.matthiesen.falling_star_rewards.common.config.FallingStarsConfigManager;
import dev.matthiesen.falling_star_rewards.common.registry.PermissionRegistry;
import dev.matthiesen.falling_star_rewards.common.runtime.RuntimeManager;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.events.server.ServerEvent;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.api.platform.loader.ModConfigType;
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

        registerModConfig(MOD_ID, ModConfigType.SERVER, FSConfig.SERVER_CONFIG_SPEC, "falling_star_rewards/server.toml");
        registerModConfig(MOD_ID, ModConfigType.STARTUP, FSConfig.PERMISSIONS_START_SPEC, "falling_star_rewards/permissions.toml");

        PermissionRegistry.init();

        getCommandsRegistryManager().registerCommand(FallingStarCommand.CMD);

        PlatformEvents.SERVER_STARTED.subscribe(this::onServerStarted);
        PlatformEvents.SERVER_RELOAD.subscribe(this::onServerReload);
        PlatformEvents.SERVER_END_TICK.subscribe(this::onServerEndTick);

        createInfoLog("Initializing Falling Star Rewards");
    }

    private boolean isServerRunning = false;

    public void onServerStarted(ServerEvent.Started event) {
        CONFIG_MANAGER.init();
        reload().run();
        isServerRunning = true;
    }

    public void onServerReload(ServerEvent.Reload event) {
        reload().run();
    }

    public void onServerEndTick(ServerEvent.EndTick event) {
        boolean enabled = FSConfig.SERVER_CONFIG.enabled.getAsBoolean();
        if (!enabled) return;
        if (!isServerRunning) return;
        RuntimeManager.tick(event.server());
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    public Runnable reload() {
        return () -> {
            loadConfigs();
            CONFIG_MANAGER.validateRewardsConfigs();
            createInfoLog("Reloaded Config (enabled=" + FSConfig.SERVER_CONFIG.enabled.getAsBoolean() + ")");
        };
    }

    public void loadConfigs() {
        CONFIG_MANAGER.getEventsConfigManager().loadConfigs();
        CONFIG_MANAGER.getRewardsConfigManager().loadConfigs();
        CONFIG_MANAGER.getVisualsConfigManager().loadConfigs();
        CONFIG_MANAGER.getSchedulesConfigManager().loadConfigs();
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
        if (!FSConfig.SERVER_CONFIG.enabled.getAsBoolean()) {
            createInfoLog("Cannot force cycle - mod is disabled");
            return 0;
        }
        var preset = presetId != null ? CONFIG_MANAGER.loadPresetConfig(presetId) : CONFIG_MANAGER.loadRandomEventPreset();
        if (preset == null) {
            createWarnLog("No event presets available to start a cycle");
            return 0;
        }
        return RuntimeManager.runCycle(server, preset, bypassActivationChecks);
    }
}
