package dev.fsg262.rng;

public record BarterResult(
        long rngSeed,
        int barterIndex,
        int goldIngots,
        String outputItem,
        int outputQuantity
) {}