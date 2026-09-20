package dev.fsg262.lava;

import java.util.List;

/**
 * Resolves lava access without treating the absence of natural lava as a
 * failed seed. The fallback is only eligible after the other requirements
 * have passed and always contains exactly three pools.
 */
public record LavaAccessDecision(
        LavaPoolKind kind,
        List<ArtificialLavaPoolGenerator.GeneratedPool> artificialPools,
        String reason) {
    public LavaAccessDecision {
        artificialPools = List.copyOf(artificialPools);
    }

    public boolean passed() {
        return kind == LavaPoolKind.NATURAL
                || (kind == LavaPoolKind.ARTIFICIAL && artificialPools.size() == 3);
    }

    public static LavaAccessDecision natural(int count) {
        if (count <= 0) throw new IllegalArgumentException("natural count must be positive");
        return new LavaAccessDecision(LavaPoolKind.NATURAL, List.of(),
                "qualifying natural lava available");
    }

    public static LavaAccessDecision fallback(ArtificialLavaPoolGenerator.PoolGenerationResult result) {
        if (!result.verified() || result.pools().size() != 3) {
            return new LavaAccessDecision(LavaPoolKind.ARTIFICIAL, List.of(),
                    "NOT VERIFIED: artificial fallback could not produce exactly 3 pools");
        }
        return new LavaAccessDecision(LavaPoolKind.ARTIFICIAL, result.pools(),
                "natural lava absent; deterministic 3-pool fallback selected");
    }
}
