package dev.matthiesen.falling_star_rewards.common.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class KeyValueTableComponentBuilder {
    private final String title;
    private final List<Entry> entries = new ArrayList<>();

    public KeyValueTableComponentBuilder(String title) {
        this.title = title == null ? "Status" : title;
    }

    public KeyValueTableComponentBuilder addRow(String key, String value) {
        entries.add(new Row(safeText(key), safeText(value)));
        return this;
    }

    public KeyValueTableComponentBuilder addSection(String sectionName) {
        entries.add(new Section(safeText(sectionName)));
        return this;
    }

    public Component build() {
        int maxKeyWidth = 0;
        for (Entry entry : entries) {
            if (entry instanceof Row row) {
                maxKeyWidth = Math.max(maxKeyWidth, row.key().length());
            }
        }

        MutableComponent message = Component.empty();
        message.append(Component.literal("[ " + title + " ]").withStyle(ChatFormatting.GOLD));

        boolean firstSection = true;
        for (Entry entry : entries) {
            if (entry instanceof Section(String name)) {
                message.append(Component.literal(firstSection ? "\n" : "\n\n"));
                message.append(Component.literal("[" + name + "]").withStyle(ChatFormatting.AQUA));
                firstSection = false;
                continue;
            }

            if (entry instanceof Row(String key, String value)) {
                message.append(Component.literal("\n- ").withStyle(ChatFormatting.DARK_GRAY));
                message.append(Component.literal(padRight(key, maxKeyWidth)).withStyle(ChatFormatting.YELLOW));
                message.append(Component.literal(" : ").withStyle(ChatFormatting.GRAY));
                message.append(Component.literal(value).withStyle(ChatFormatting.WHITE));
            }
        }

        return message;
    }

    private static String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "<unset>";
        }
        return value;
    }

    private static String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }

        return value + " ".repeat(width - value.length());
    }

    private sealed interface Entry permits Row, Section {
    }

    private record Row(String key, String value) implements Entry {
    }

    private record Section(String name) implements Entry {
    }
}

