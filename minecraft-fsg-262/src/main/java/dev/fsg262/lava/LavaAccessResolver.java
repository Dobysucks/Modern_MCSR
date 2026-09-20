package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;
import dev.fsg262.filter.StartType;
import java.util.List;

public final class LavaAccessResolver {
    private final LavaPoolAnalyzer analyzer = new LavaPoolAnalyzer();
    private final ArtificialLavaPoolGenerator generator = new ArtificialLavaPoolGenerator();

    public LavaAccessDecision resolve(long worldSeed, long rngSeed, StartType startType,
                                      BlockCoordinate origin,
                                      List<LavaPoolObservation> natural,
                                      List<ArtificialLavaPoolGenerator.CandidatePosition> candidates,
                                      LavaSearchConfig config,
                                      boolean prerequisitesPassed) {
        var usable = analyzer.qualifyingNaturalPools(natural, origin, config);
        if (!usable.isEmpty()) return LavaAccessDecision.natural(usable.size());
        if (!prerequisitesPassed) {
            return new LavaAccessDecision(LavaPoolKind.ARTIFICIAL, List.of(),
                    "natural lava absent and prerequisite stages failed");
        }
        return LavaAccessDecision.fallback(generator.createFallback(worldSeed, rngSeed,
                startType, origin, candidates, config));
    }
}
