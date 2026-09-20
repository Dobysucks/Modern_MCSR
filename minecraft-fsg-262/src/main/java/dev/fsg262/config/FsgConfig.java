package dev.fsg262.config;

import dev.fsg262.filter.FilterProfile;
import dev.fsg262.rng.StandardizedSpeedrunRng;

/**
 * Runtime configuration model. Serialization is intentionally separate from
 * the filter so strict values cannot be mutated accidentally by a UI.
 */
public record FsgConfig(
        String profileName,
        boolean standardizedRng,
        boolean legacyPiglinBehavior,
        boolean blazeRng,
        boolean pearlRng,
        boolean mobDropRng,
        Long separateRngSeed,
        boolean filterLogging,
        boolean rngLogging,
        boolean structureLogging,
        boolean seedReports
) {
    public static FsgConfig defaults() {
        return new FsgConfig(
                FilterProfile.strictRankedStyle().name(),
                true, true, false, false, false, null,
                false, false, false, true
        );
    }

    public long rngSeed(long overworldSeed) {
        return separateRngSeed == null ? overworldSeed : separateRngSeed;
    }

    public StandardizedSpeedrunRng.Mode rngMode() {
        return legacyPiglinBehavior
                ? StandardizedSpeedrunRng.Mode.LEGACY_1161
                : StandardizedSpeedrunRng.Mode.MODERN_262;
    }
}