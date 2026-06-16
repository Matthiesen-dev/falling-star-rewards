package dev.matthiesen.falling_star_rewards.common.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class KeyValueTableComponentBuilder {
    private final String title;
    private final List<Row> rows = new ArrayList<>();

    public KeyValueTableComponentBuilder(String title) {
        this.title = title == null ? "Status" : title;
    }

    public KeyValueTableComponentBuilder addRow(String key, String value) {
        rows.add(new Row(safeText(key), safeText(value)));
        return this;
    }

    public Component build() {
        int maxKeyWidth = 0;
        for (Row row : rows) {
            maxKeyWidth = Math.max(maxKeyWidth, row.key().length());
        }

        MutableComponent message = Component.empty();
        message.append(Component.literal("[ " + title + " ]").withStyle(ChatFormatting.GOLD));

        for (Row row : rows) {
            message.append(Component.literal("\n- ").withStyle(ChatFormatting.DARK_GRAY));
            message.append(Component.literal(padRight(row.key(), maxKeyWidth)).withStyle(ChatFormatting.YELLOW));
            message.append(Component.literal(" : ").withStyle(ChatFormatting.GRAY));
            message.append(Component.literal(row.value()).withStyle(ChatFormatting.WHITE));
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

    private record Row(String key, String value) {
    }
}

