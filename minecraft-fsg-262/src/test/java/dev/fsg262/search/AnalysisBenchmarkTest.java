package dev.fsg262.search;

import dev.fsg262.completion.VerificationStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisBenchmarkTest {
    @Test
    void countsEachExplicitVerificationStatus() {
        var result = new AnalysisBenchmark().run(10, 3, seed -> switch ((int) seed) {
            case 10 -> VerificationStatus.PASS;
            case 11 -> VerificationStatus.FAIL;
            default -> VerificationStatus.NOT_VERIFIED;
        }, () -> false);

        assertEquals(3, result.candidatesTested());
        assertEquals(1, result.passCount());
        assertEquals(1, result.failCount());
        assertEquals(1, result.notVerifiedCount());
        assertFalse(result.cancelled());
    }

    @Test
    void cancellationStopsBeforeTheNextCandidate() {
        var cancelled = new AtomicBoolean();
        var result = new AnalysisBenchmark().run(1, 100, seed -> {
            cancelled.set(true);
            return VerificationStatus.NOT_VERIFIED;
        }, cancelled::get);

        assertEquals(1, result.candidatesTested());
        assertTrue(result.cancelled());
    }
}
