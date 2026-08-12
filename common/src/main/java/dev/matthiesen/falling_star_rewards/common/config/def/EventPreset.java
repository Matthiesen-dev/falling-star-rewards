package dev.matthiesen.falling_star_rewards.common.config.def;

import com.electronwill.nightconfig.core.Config;
import dev.matthiesen.falling_star_rewards.common.config.def.event.Announcement;
import dev.matthiesen.falling_star_rewards.common.config.def.event.Spawn;
import dev.matthiesen.falling_star_rewards.common.interfaces.AnnouncementScope;
import dev.matthiesen.falling_star_rewards.common.interfaces.SpawnTargetScope;

import java.util.ArrayList;
import java.util.List;

public record EventPreset(
        String eventId,
        boolean enabled,
        String rewardsPresetId,
        String visualsPresetId,
        List<String> commands,
        Spawn spawn,
        Announcement announcement
) {

    public static List<Config> getDefaultConfig() {
        List<EventPreset> presets = new ArrayList<>();

        presets.add(new EventPreset(
                "base",
                true,
                "base",
                "base",
                List.of(),
                new Spawn(SpawnTargetScope.PER_PLAYER, 16, 48, 12, false),
                new Announcement(true, AnnouncementScope.NEARBY, false, List.of(
                        "A falling star has appeared nearby!",
                        "A falling star has appeared in the sky!",
                        "A falling star has appeared in the world!"
                ))
        ));

        return presets.stream()
                .map(EventPreset::serialize)
                .toList();
    }

    public static boolean isValid(Object object) {
        if (!(object instanceof Config config)) {
            return false;
        }

        String eventId = config.get("eventId");
        String rewardsPresetId = config.get("rewardsPresetId");
        String visualsPresetId = config.get("visualsPresetId");
        List<String> commands = config.get("commands");
        Config spawnConfig = config.get("spawn");
        Config announcementConfig = config.get("announcement");

        return eventId != null && !eventId.isEmpty()
                && rewardsPresetId != null && !rewardsPresetId.isEmpty()
                && visualsPresetId != null && !visualsPresetId.isEmpty()
                && commands != null
                && spawnConfig != null
                && announcementConfig != null;
    }

    public static EventPreset deserialize(Config raw) {
        String eventId = raw.get("eventId");
        boolean enabled = raw.get("enabled");
        String rewardsPresetId = raw.get("rewardsPresetId");
        String visualsPresetId = raw.get("visualsPresetId");
        List<String> commands = raw.get("commands");
        Config spawnConfig = raw.get("spawn");
        Spawn spawn = new Spawn(
                SpawnTargetScope.valueOf(spawnConfig.get("targetScope").toString().toUpperCase()),
                spawnConfig.get("minRadius"),
                spawnConfig.get("maxRadius"),
                spawnConfig.get("maxLocationAttempts"),
                spawnConfig.get("allowWaterSpawns")
        );
        Config announcementConfig = raw.get("announcement");
        Announcement announcement = new Announcement(
                announcementConfig.get("enabled"),
                AnnouncementScope.valueOf(announcementConfig.get("scope").toString().toUpperCase()),
                announcementConfig.get("useActionBar"),
                announcementConfig.get("messages")
        );
        return new EventPreset(
                eventId,
                enabled,
                rewardsPresetId,
                visualsPresetId,
                commands,
                spawn,
                announcement
        );
    }

    public Config serialize() {
        Config spawnConfig = Config.inMemory();
        spawnConfig.set("targetScope", spawn.targetScope().name().toLowerCase());
        spawnConfig.set("minRadius", spawn.minRadius());
        spawnConfig.set("maxRadius", spawn.maxRadius());
        spawnConfig.set("maxLocationAttempts", spawn.maxLocationAttempts());
        spawnConfig.set("allowWaterSpawns", spawn.allowWaterSpawns());

        Config announcementConfig = Config.inMemory();
        announcementConfig.set("enabled", announcement.enabled());
        announcementConfig.set("scope", announcement.scope().name().toLowerCase());
        announcementConfig.set("useActionBar", announcement.useActionBar());
        announcementConfig.set("messages", announcement.messages());

        Config config = Config.inMemory();
        config.set("eventId", eventId);
        config.set("enabled", enabled);
        config.set("rewardsPresetId", rewardsPresetId);
        config.set("visualsPresetId", visualsPresetId);
        config.set("commands", commands);
        config.set("spawn", spawnConfig);
        config.set("announcement", announcementConfig);
        return config;
    }
}
