package dev.fsg262.cache;

import dev.fsg262.filter.SeedTypeChoice;

public record SeedCacheKey(
        long seed,
        SeedTypeChoice seedType,
        String profileName,
        long rngSeed
) {}