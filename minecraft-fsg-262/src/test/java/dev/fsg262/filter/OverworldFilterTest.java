package dev.fsg262.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverworldFilterTest {
    private final FilterProfile profile = FilterProfile.strictRankedStyle();
    private final OverworldFilter filter = new OverworldFilter();

    @Test
    void villageAtSevenChunksPasses() {
        assertTrue(filter.evaluate(village(7, 7, 0, 6, 3, 8), profile).passed());
    }

    @Test
    void villageBeyondSevenChunksFails() {
        assertFalse(filter.evaluate(village(7.001, 7, 0, 6, 3, 8), profile).passed());
    }

    @Test
    void villageSixIronFailsAndFourIronThreeDiamondsPasses() {
        assertFalse(filter.evaluate(village(0, 6, 0, 6, 3, 8), profile).passed());
        assertTrue(filter.evaluate(village(0, 4, 3, 6, 3, 8), profile).passed());
    }

    @Test
    void villageRiverAndLavaThresholdsAreInclusive() {
        assertTrue(filter.evaluate(village(0, 7, 0, 6, 3, 0), profile).passed());
        assertFalse(filter.evaluate(village(0, 7, 0, 6.001, 3, 0), profile).passed());
        assertFalse(filter.evaluate(village(0, 7, 0, 6, 2, 0), profile).passed());
        assertTrue(filter.evaluate(village(0, 7, 0, 6, 0, 8), profile).passed());
    }

    @Test
    void shipwreckUsesFourChunkAndTwoMagmaRavineThresholds() {
        var good = new StartEvaluationInput(2, StartType.SHIPWRECK, 4, 7, 0,
                true, 99, 0, false, 0, false, false, 2, 0,
                false, false, false, false, true);
        assertTrue(filter.evaluate(good, profile).passed());
        assertFalse(filter.evaluate(new StartEvaluationInput(2, StartType.SHIPWRECK, 4.001, 7, 0,
                true, 99, 0, false, 0, false, false, 2, 0,
                false, false, false, false, true), profile).passed());
    }

    @Test
    void desertTempleRequiresRiverWoodAndThreeLavaPools() {
        var good = new StartEvaluationInput(3, StartType.DESERT_TEMPLE, 5, 4, 3,
                true, 6, 3, false, 0, false, true, 0, 0,
                false, false, false, false, true);
        assertTrue(filter.evaluate(good, profile).passed());
        assertFalse(filter.evaluate(new StartEvaluationInput(3, StartType.DESERT_TEMPLE, 5, 4, 3,
                true, 6, 2, false, 0, false, true, 0, 0,
                false, false, false, false, true), profile).passed());
    }

    @Test
    void ruinedPortalRequiresEighteenNuggetsAndAnEnterRoute() {
        var good = new StartEvaluationInput(4, StartType.RUINED_PORTAL, 3, 0, 0,
                true, 99, 0, false, 0, false, false, 0, 18,
                true, true, false, false, true);
        assertTrue(filter.evaluate(good, profile).passed());
        assertFalse(filter.evaluate(new StartEvaluationInput(4, StartType.RUINED_PORTAL, 3, 0, 0,
                true, 99, 0, false, 0, false, false, 0, 17,
                true, true, false, false, true), profile).passed());
    }

    @Test
    void buriedTreasureUsesFiveChunkAndTwoMagmaRavineThresholds() {
        var good = new StartEvaluationInput(5, StartType.BURIED_TREASURE, 5, 7, 0,
                true, 99, 0, false, 0, false, false, 2, 0,
                false, false, false, false, true);
        assertTrue(filter.evaluate(good, profile).passed());
        assertFalse(filter.evaluate(new StartEvaluationInput(5, StartType.BURIED_TREASURE, 5, 7, 0,
                true, 99, 0, false, 0, false, false, 1, 0,
                false, false, false, false, true), profile).passed());
    }

    private StartEvaluationInput village(double distance, int iron, int diamonds,
                                         double river, int lava, int obsidian) {
        return new StartEvaluationInput(1, StartType.VILLAGE, distance, iron, diamonds,
                true, river, lava, true, obsidian, false, false,
                0, 0, false, false, false, false, true);
    }
}