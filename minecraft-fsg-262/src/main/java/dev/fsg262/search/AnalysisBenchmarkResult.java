package dev.fsg262.search;

public record AnalysisBenchmarkResult(
        long candidatesTested,
        long passCount,
        long failCount,
        long notVerifiedCount,
        long elapsedMillis,
        boolean cancelled
) {
    public double candidatesPerSecond() {
        return elapsedMillis <= 0 ? 0.0 : candidatesTested * 1000.0 / elapsedMillis;
    }
}
