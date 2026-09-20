package dev.fsg262.search;

public record SeedSearchProgress(
        long tested,
        long rejected,
        long notVerified,
        long filterPassed,
        long completable,
        long currentSeed,
        String currentStage,
        long elapsedMillis
) {
    public SeedSearchProgress(long tested, long rejected, long filterPassed,
                              long completable, long currentSeed, String currentStage) {
        this(tested, rejected, 0, filterPassed, completable, currentSeed, currentStage, 0);
    }

    public double candidatesPerSecond() {
        return elapsedMillis <= 0 ? 0.0 : tested * 1000.0 / elapsedMillis;
    }
}