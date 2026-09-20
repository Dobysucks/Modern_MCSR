package dev.fsg262.evaluation;

import dev.fsg262.completion.CompletionEvidence;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.nether.NetherEvaluation;

/**
 * Optional bridge for a client-side world-generation adapter.  The common
 * evaluator remains usable on a dedicated server, while a client can supply
 * real terrain, Nether geometry and completion observations.
 */
public interface EvaluatorEvidenceProvider {
    default TerrainEvidence terrain(long seed) {
        return TerrainEvidence.unavailable("terrain adapter not installed");
    }

    default NetherEvaluation nether(long seed, FilterProfile profile) {
        return null;
    }

    default CompletionEvidence completion(long seed) {
        return null;
    }
}
