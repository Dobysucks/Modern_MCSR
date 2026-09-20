package dev.fsg262.search;

import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.SeedTypeChoice;

public record SeedSearchRequest(
        long generatorSeed,
        SeedTypeChoice seedType,
        FilterProfile profile,
        long maximumCandidates
) {
    public SeedSearchRequest {
        if (maximumCandidates <= 0) {
            throw new IllegalArgumentException("maximumCandidates must be positive");
        }
    }
}