package dev.fsg262.search;

import dev.fsg262.filter.SeedTypeChoice;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.completion.CompletionEvaluation;
import dev.fsg262.lava.ArtificialLavaPoolGenerator;
import dev.fsg262.lava.LavaPoolObservation;

import java.util.List;

public record AcceptedSeed(
        long seed,
        SeedTypeChoice seedType,
        FilterProfile profile,
        CompletionEvaluation completion,
        List<LavaPoolObservation> naturalLava,
        ArtificialLavaPoolGenerator.PoolGenerationResult artificialLava
) {
    public AcceptedSeed {
        naturalLava = List.copyOf(naturalLava);
    }
}