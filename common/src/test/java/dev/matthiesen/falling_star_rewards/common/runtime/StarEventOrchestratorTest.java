package dev.matthiesen.falling_star_rewards.common.runtime;

import dev.matthiesen.falling_star_rewards.common.config.MainConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarEventOrchestratorTest {

    @Test
    void disabledConfigNeverStartsCycle() {
        MainConfig config = new MainConfig();
        config.enabled = false;

        StarEventOrchestrator orchestrator = new StarEventOrchestrator();

        assertFalse(orchestrator.shouldStartCycle(0, config));
        assertFalse(orchestrator.shouldStartCycle(200, config));
    }

    @Test
    void startsAtFirstTickThenRespectsCooldown() {
        MainConfig config = new MainConfig();
        config.scheduler.baseIntervalTicks = 100;
        config.scheduler.intervalJitterTicks = 0;

        StarEventOrchestrator orchestrator = new StarEventOrchestrator();

        assertTrue(orchestrator.shouldStartCycle(0, config));
        assertFalse(orchestrator.shouldStartCycle(50, config));
        assertTrue(orchestrator.shouldStartCycle(100, config));
    }
}

