package dev.fsg262.cache;

import dev.fsg262.filter.SeedTypeChoice;

public record SeedCacheKey(
        long seed,
        SeedTypeChoice seedType,
        String profileName,
        long rngSeed,
        String filterVersion,
        String minecraftVersion
) {
    public SeedCacheKey(long seed, SeedTypeChoice seedType, String profileName, long rngSeed) {
        this(seed, seedType, profileName, rngSeed,
                SeedCache.FILTER_VERSION, SeedCache.MINECRAFT_VERSION);
    }

    public boolean isCurrent() {
        return SeedCache.FILTER_VERSION.equals(filterVersion)
                && SeedCache.MINECRAFT_VERSION.equals(minecraftVersion);
    }
}