package dev.fsg262.filter;

/**
 * Facts supplied by the 26.2 world-generation adapter. This record contains
 * only observed facts; it does not select a nearby structure on its own.
 */
public record StartEvaluationInput(
        long seed,
        StartType startType,
        double intendedStructureDistanceChunks,
        int iron,
        int diamonds,
        boolean foodAvailable,
        double riverDistanceChunks,
        int usableLavaPools,
        boolean blacksmith,
        int blacksmithObsidian,
        boolean taigaVariant,
        boolean nearbyWood,
        int eligibleMagmaRavines,
        int ironNuggets,
        boolean reliableIgnition,
        boolean canEnterWithObsidian,
        boolean canEnterWithBucket,
        boolean hasBlockingStructures,
        boolean intendedStructureWasSelected
) {}