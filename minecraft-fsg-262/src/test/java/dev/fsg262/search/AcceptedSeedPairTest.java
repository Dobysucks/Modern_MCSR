package dev.fsg262.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcceptedSeedPairTest {
    @Test
    void keepsDimensionSeedsDistinct() {
        var accepted = new AcceptedSeedPair(Long.MIN_VALUE, Long.MAX_VALUE);

        assertEquals(Long.MIN_VALUE, accepted.mcsr_overworld_seed());
        assertEquals(Long.MAX_VALUE, accepted.mcsr_nether_seed());
    }
}
