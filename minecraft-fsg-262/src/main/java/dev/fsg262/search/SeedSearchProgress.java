package dev.fsg262.search;

public record SeedSearchProgress(
        long tested,
        long rejected,
        long filterPassed,
        long completable,
        long currentSeed,
        String currentStage
) {}