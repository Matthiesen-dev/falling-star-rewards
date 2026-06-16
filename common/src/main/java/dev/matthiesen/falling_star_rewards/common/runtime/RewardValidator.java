package dev.matthiesen.falling_star_rewards.common.runtime;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.matthiesen.falling_star_rewards.common.config.RewardsConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates reward configuration entries and tracks validity status.
 */
public final class RewardValidator {
    private int validEntries = 0;
    private int invalidEntries = 0;
    private final List<String> validationMessages = new ArrayList<>();

    public void validateRewards(RewardsConfig config) {
        validEntries = 0;
        invalidEntries = 0;
        validationMessages.clear();

        if (config.entries == null || config.entries.length == 0) {
            validationMessages.add("No reward entries configured.");
            return;
        }

        for (int i = 0; i < config.entries.length; i++) {
            RewardsConfig.RewardEntry entry = config.entries[i];
            if (validateEntry(entry, i)) {
                validEntries++;
            } else {
                invalidEntries++;
            }
        }

    }

    private boolean validateEntry(RewardsConfig.RewardEntry entry, int index) {
        if (entry == null) {
                    validationMessages.add("Reward entry " + index + " is null");
            return false;
        }

        if (entry.id == null || entry.id.isBlank()) {
                    validationMessages.add("Reward entry " + index + " has no id");
            return false;
        }

        ResourceLocation itemId = ResourceLocation.tryParse(entry.id);
        if (itemId == null) {
                    validationMessages.add("Reward entry " + index + " has invalid item id: " + entry.id);
            return false;
        }

        if (BuiltInRegistries.ITEM.getOptional(itemId).isEmpty()) {
                    validationMessages.add("Reward entry " + index + " references unknown item: " + entry.id);
            return false;
        }

        if (entry.weight <= 0) {
                    validationMessages.add("Reward entry " + index + " (" + entry.id + ") has non-positive weight: " + entry.weight);
            return false;
        }

        int min = Math.max(1, entry.minCount);
        int max = Math.max(min, entry.maxCount);
        if (min != entry.minCount || max != entry.maxCount) {
                    validationMessages.add("Reward entry " + index + " (" + entry.id + ") has out-of-range counts, normalized to " + min + ".." + max);
            return false;
        }

        if (entry.customData != null && !entry.customData.isBlank()) {
            try {
                TagParser.parseTag(entry.customData);
            } catch (CommandSyntaxException e) {
                        validationMessages.add("Reward entry " + index + " (" + entry.id + ") has invalid customData SNBT: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    public int getValidEntries() {
        return validEntries;
    }

    public int getInvalidEntries() {
        return invalidEntries;
    }

    public List<String> getValidationMessages() {
        return new ArrayList<>(validationMessages);
    }
}

