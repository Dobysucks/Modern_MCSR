package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LavaPoolAnalyzerTest {
    private static final BlockCoordinate ORIGIN = new BlockCoordinate(0, 0);

    @Test
    void onlyUsableNaturalPoolsQualify() {
        var usable = new LavaPoolObservation(
                new BlockCoordinate(8, 0), 4, true, true,
                false, 0, false, true, LavaPoolKind.NATURAL);
        var buried = new LavaPoolObservation(
                new BlockCoordinate(4, 0), 12, false, false,
                false, 0, false, true, LavaPoolKind.NATURAL);
        var artificial = new LavaPoolObservation(
                new BlockCoordinate(2, 0), 4, true, true,
                false, 0, false, true, LavaPoolKind.ARTIFICIAL);

        var result = new LavaPoolAnalyzer().qualifyingNaturalPools(
                List.of(usable, buried, artificial), ORIGIN, LavaSearchConfig.defaults());

        assertEquals(List.of(usable), result);
        assertTrue(new LavaPoolAnalyzer().hasNaturalAccess(
                List.of(usable), ORIGIN, LavaSearchConfig.defaults()));
    }
}
