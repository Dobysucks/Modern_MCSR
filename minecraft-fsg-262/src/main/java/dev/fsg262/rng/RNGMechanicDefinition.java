package dev.fsg262.rng;

public record RNGMechanicDefinition(
        String mechanicName,
        String vanillaBehavior,
        String standardizedBehavior,
        String rngSource,
        String seedSource,
        String deterministicSequence,
        String testPlan
) {}