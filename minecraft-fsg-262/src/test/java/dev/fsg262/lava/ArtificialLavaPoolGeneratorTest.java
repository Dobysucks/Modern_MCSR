package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;
import dev.fsg262.filter.StartType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtificialLavaPoolGeneratorTest {
    @Test
    void fallbackSelectsExactlyThreeDeterministicNonOverlappingPools() {
        var origin = new BlockCoordinate(0, 0);
        var candidates = List.of(
                candidate(4, 0), candidate(12, 0), candidate(20, 0),
                candidate(28, 0), candidate(0, 28));
        var generator = new ArtificialLavaPoolGenerator();

        var first = generator.createFallback(12L, 12L, StartType.VILLAGE,
                origin, candidates, LavaSearchConfig.defaults());
        var second = generator.createFallback(12L, 12L, StartType.VILLAGE,
                origin, candidates, LavaSearchConfig.defaults());

        assertTrue(first.verified());
        assertEquals(3, first.pools().size());
        assertEquals(first.pools(), second.pools());
        assertTrue(first.pools().stream().allMatch(pool ->
                generator.verifyPool(pool, origin, LavaSearchConfig.defaults())));
    }

    private ArtificialLavaPoolGenerator.CandidatePosition candidate(long x, long z) {
        return new ArtificialLavaPoolGenerator.CandidatePosition(
                new BlockCoordinate(x, z), 2, 2, 1,
                true, true, true, false, true);
    }
}
