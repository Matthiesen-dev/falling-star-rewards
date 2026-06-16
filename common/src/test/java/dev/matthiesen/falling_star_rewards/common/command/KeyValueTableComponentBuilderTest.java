package dev.matthiesen.falling_star_rewards.common.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyValueTableComponentBuilderTest {

    @Test
    void buildsMultilineTableText() {
        String rendered = new KeyValueTableComponentBuilder("Demo")
                .addRow("Enabled", "true")
                .addRow("Particle", "ash")
                .build()
                .getString();

        assertTrue(rendered.contains("[ Demo ]"));
        assertTrue(rendered.contains("Enabled"));
        assertTrue(rendered.contains("Particle"));
        assertTrue(rendered.contains("\n- "));
    }

    @Test
    void nullAndBlankValuesAreReplaced() {
        String rendered = new KeyValueTableComponentBuilder(null)
                .addRow("Key", "")
                .addRow(null, null)
                .build()
                .getString();

        assertTrue(rendered.contains("[ Status ]"));
        assertTrue(rendered.contains("<unset>"));
    }
}

