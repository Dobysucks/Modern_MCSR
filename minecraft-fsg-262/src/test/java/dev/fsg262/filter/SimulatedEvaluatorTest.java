package dev.fsg262.filter;

import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.search.LocalSeedFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulatedEvaluatorTest {
    @Test
    void simulatedStartingLootIsStableAndCompletablePipelineCanPass() {
        var analyzer = new SimulatedWorldGenerationAnalyzer();
        var first = analyzer.analyzeOverworld(123L, StartType.VILLAGE,
                FilterProfile.completable());
        var second = analyzer.analyzeOverworld(123L, StartType.VILLAGE,
                FilterProfile.completable());
        assertEquals(first, second);
        assertEquals(dev.fsg262.completion.EvidenceType.SIMULATED, first.evidenceType());
        var result = new LocalSeedFilter(analyzer).evaluate(123L, 123L,
                SeedTypeChoice.VILLAGE, FilterProfile.completable(), true);
        assertEquals(VerificationStatus.PASS, result.status());
    }
}
