package dev.fsg262.nether;

public record NetherEvaluationInput(
        long seed,
        boolean intendedBastionSelected,
        boolean intendedFortressSelected,
        double bastionDistanceFromOriginChunks,
        double bastionSeparationFromCompetitorChunks,
        boolean openTerrainToBastion,
        int bastionIron,
        int bastionObsidian,
        BastionType bastionType,
        int goodGaps,
        int tripleChestRamparts,
        double fortressDistanceFromBastionChunks,
        boolean openTerrainToFortress
) {}