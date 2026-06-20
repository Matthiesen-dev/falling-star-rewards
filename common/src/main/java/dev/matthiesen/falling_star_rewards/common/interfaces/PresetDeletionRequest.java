package dev.matthiesen.falling_star_rewards.common.interfaces;

public record PresetDeletionRequest(PresetTypes presetType, String presetName, long createdAtMs) {
}
