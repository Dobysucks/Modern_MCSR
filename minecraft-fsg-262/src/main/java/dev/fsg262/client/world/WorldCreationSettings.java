package dev.fsg262.client.world;

import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.SeedTypeChoice;

/**
 * Settings owned by the Create New World workflow. The record is immutable so
 * a search cannot observe half-updated UI state.
 */
public record WorldCreationSettings(
        boolean enabled,
        SeedTypeChoice seedType,
        String profileName,
        boolean completable,
        boolean standardizedRng,
        Long customRngSeed,
        boolean backgroundFiltering
) {
    public WorldCreationSettings {
        if (seedType == null || profileName == null || profileName.isBlank()) {
            throw new IllegalArgumentException("Seed type and profile are required");
        }
    }

    public static WorldCreationSettings defaults() {
        return new WorldCreationSettings(true, SeedTypeChoice.VILLAGE,
                FilterProfile.strictRankedStyle().name(), true, true, null, true);
    }

    public long rngSeed(long worldSeed) {
        return customRngSeed == null ? worldSeed : customRngSeed;
    }

    public String customRngSeedText() {
        return customRngSeed == null ? "" : Long.toString(customRngSeed);
    }
}
