package dev.matthiesen.falling_star_rewards.common.config.def.event;

import dev.matthiesen.falling_star_rewards.common.interfaces.AnnouncementScope;

import java.util.List;

public record Announcement(
        boolean enabled,
        AnnouncementScope scope,
        boolean useActionBar,
        List<String> messages
) {
}
