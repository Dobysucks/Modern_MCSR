package dev.fsg262.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McsrWorldSeedStateTest {
    @Test
    void keepsDimensionSeedsAndBruteOptionInPendingWorldState() {
        McsrWorldSeedState.prepareWorldCreation(Long.MIN_VALUE, Long.MAX_VALUE, true, true);

        var state = McsrWorldSeedState.consumePending();

        assertEquals(Long.MIN_VALUE, state.mcsr_overworld_seed());
        assertEquals(Long.MAX_VALUE, state.mcsr_nether_seed());
        assertTrue(state.disablePiglinBrutes());
        assertTrue(state.standardizedRng());
        assertNull(McsrWorldSeedState.consumePending());
    }

    @Test
    void pendingStateCanDisableBruteSuppression() {
        McsrWorldSeedState.prepareWorldCreation(1L, 2L, false, false);

        assertFalse(McsrWorldSeedState.consumePending().disablePiglinBrutes());
    }

    @Test
    void disabledWorldCreationClearsUnconsumedState() {
        McsrWorldSeedState.prepareWorldCreation(1L, 2L, true, true);

        McsrWorldSeedState.clearPending();

        assertNull(McsrWorldSeedState.consumePending());
    }
}
