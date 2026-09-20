package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;
import dev.fsg262.filter.StartType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LavaAccessResolverTest {
    @Test
    void absentNaturalLavaUsesExactlyThreePoolsOnlyAfterPrerequisitesPass() {
        var origin = new BlockCoordinate(0, 0);
        var candidates = List.of(candidate(4, 0), candidate(12, 0),
                candidate(20, 0), candidate(28, 0));
        var resolver = new LavaAccessResolver();
        var decision = resolver.resolve(1L, 2L, StartType.VILLAGE, origin,
                List.of(), candidates, LavaSearchConfig.defaults(), true);
        assertEquals(LavaPoolKind.ARTIFICIAL, decision.kind());
        assertEquals(3, decision.artificialPools().size());
        assertTrue(decision.passed());
        var rejected = resolver.resolve(1L, 2L, StartType.VILLAGE, origin,
                List.of(), candidates, LavaSearchConfig.defaults(), false);
        assertTrue(!rejected.passed());
    }

    private ArtificialLavaPoolGenerator.CandidatePosition candidate(long x, long z) {
        return new ArtificialLavaPoolGenerator.CandidatePosition(
                new BlockCoordinate(x, z), 2, 2, 1,
                true, true, true, false, true);
    }
}
