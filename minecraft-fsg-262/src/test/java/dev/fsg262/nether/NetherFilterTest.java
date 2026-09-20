package dev.fsg262.nether;

import dev.fsg262.filter.FilterProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetherFilterTest {
    private final FilterProfile profile = FilterProfile.strictRankedStyle();
    private final NetherFilter filter = new NetherFilter();

    @Test
    void bastionAndFortressThresholdsAreInclusive() {
        var input = new NetherEvaluationInput(1, true, true,
                14, 10, true, 3, 5, BastionType.BRIDGE,
                0, 0, 16, true);
        assertTrue(filter.evaluate(input, profile).passed());
    }

    @Test
    void failingBastionTerrainOrLootFails() {
        var input = new NetherEvaluationInput(1, true, true,
                14, 10, false, 2, 4, BastionType.BRIDGE,
                0, 0, 16, true);
        assertFalse(filter.evaluate(input, profile).passed());
    }

    @Test
    void stablesRequireTwoGapsOrGapAndTripleChestRampart() {
        var twoGaps = new NetherEvaluationInput(1, true, true,
                14, 10, true, 3, 5, BastionType.HOGLIN_STABLES,
                2, 0, 16, true);
        var oneGapAndChest = new NetherEvaluationInput(1, true, true,
                14, 10, true, 3, 5, BastionType.HOGLIN_STABLES,
                1, 1, 16, true);
        var oneGap = new NetherEvaluationInput(1, true, true,
                14, 10, true, 3, 5, BastionType.HOGLIN_STABLES,
                1, 0, 16, true);
        assertTrue(filter.evaluate(twoGaps, profile).passed());
        assertTrue(filter.evaluate(oneGapAndChest, profile).passed());
        assertFalse(filter.evaluate(oneGap, profile).passed());
    }

    @Test
    void gapAnalyzerUsesExplicitGeometry() {
        var analyzer = new BastionGapAnalyzer();
        var good = new BastionGapAnalyzer.GapObservation(3, 3, 3, 0, false);
        var blocked = new BastionGapAnalyzer.GapObservation(3, 3, 3, 1, false);
        assertTrue(analyzer.isGoodGap(good));
        assertFalse(analyzer.isGoodGap(blocked));
        assertTrue(analyzer.countGoodGaps(List.of(good, blocked)) == 1);
    }
}