package dev.fsg262.search;

import dev.fsg262.completion.VerificationStatus;

import java.util.function.BooleanSupplier;
import java.util.function.LongFunction;

/**
 * Bounded diagnostic runner for world-generation analysis. It deliberately
 * reports NOT_VERIFIED separately and never changes evaluator decisions.
 */
public final class AnalysisBenchmark {
    public AnalysisBenchmarkResult run(
            long firstSeed,
            int candidateCount,
            LongFunction<VerificationStatus> evaluator,
            BooleanSupplier cancelled
    ) {
        if (candidateCount < 0) throw new IllegalArgumentException("candidateCount must not be negative");
        long started = System.nanoTime();
        long tested = 0;
        long pass = 0;
        long fail = 0;
        long notVerified = 0;
        boolean wasCancelled = false;
        for (int index = 0; index < candidateCount; index++) {
            if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
                wasCancelled = true;
                break;
            }
            var status = evaluator.apply(firstSeed + index);
            tested++;
            switch (status) {
                case PASS -> pass++;
                case FAIL -> fail++;
                case NOT_VERIFIED -> notVerified++;
            }
        }
        return new AnalysisBenchmarkResult(
                tested, pass, fail, notVerified,
                (System.nanoTime() - started) / 1_000_000L, wasCancelled);
    }
}
