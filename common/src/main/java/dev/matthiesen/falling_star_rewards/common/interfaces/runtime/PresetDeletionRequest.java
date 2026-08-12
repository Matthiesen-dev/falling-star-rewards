package dev.matthiesen.falling_star_rewards.common.interfaces.runtime;

import dev.matthiesen.falling_star_rewards.common.interfaces.PresetTypes;

public record PresetDeletionRequest(PresetTypes presetType, String presetName, long createdAtMs) {
}
