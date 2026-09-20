package dev.fsg262.rng;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyPiglinBarteringTest {
    private final LegacyPiglinBartering bartering = new LegacyPiglinBartering();

    @Test
    void everySeventyTwoBarterWindowHasTheDocumentedGuarantees() {
        for (int start : new int[]{0, 72, 144, 216, 936}) {
            assertTrue(bartering.hasWindowGuarantees(12345L, start), "window " + start);
        }
    }

    @Test
    void longSequencesRemainDeterministic() {
        for (int count : new int[]{72, 144, 216, 1008}) {
            assertEquals(bartering.sequence(12345L, count),
                    bartering.sequence(12345L, count));
        }
    }

    @Test
    void sameSeedAndIndexProduceTheSameResult() {
        assertEquals(bartering.result(44L, 19), bartering.result(44L, 19));
    }

    @Test
    void differentSeedsProduceDifferentSequences() {
        assertNotEquals(bartering.sequence(44L, 72), bartering.sequence(45L, 72));
    }

    @Test
    void replayOutputIncludesIndexAndOutput() {
        String replay = bartering.replay(7L, 2);
        assertTrue(replay.contains("RNG SEED: 7"));
        assertTrue(replay.contains("Barter #0 ->"));
        assertTrue(replay.contains("Barter #1 ->"));
    }
}