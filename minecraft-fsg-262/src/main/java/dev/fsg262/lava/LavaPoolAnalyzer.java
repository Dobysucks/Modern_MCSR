package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;
import java.util.List;

public final class LavaPoolAnalyzer {
    public List<LavaPoolObservation> qualifyingNaturalPools(
            List<LavaPoolObservation> observations,
            BlockCoordinate origin,
            LavaSearchConfig config
    ) {
        return observations.stream()
                .filter(pool -> pool.qualifiesAsNatural(config, origin))
                .toList();
    }

    public boolean hasNaturalAccess(
            List<LavaPoolObservation> observations,
            BlockCoordinate origin,
            LavaSearchConfig config
    ) {
        return !qualifyingNaturalPools(observations, origin, config).isEmpty();
    }
}