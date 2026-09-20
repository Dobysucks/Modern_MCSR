package dev.fsg262.evaluation;

import dev.fsg262.completion.CompletionEvaluation;
import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.SeedEvaluation;
import dev.fsg262.nether.NetherEvaluation;
import java.util.List;

/** Evidence-rich, presentation-independent result of a seed analysis. */
public record EvaluationResult(
        long seed,
        VerificationStatus status,
        List<EvaluationStage> stages,
        CompletionEvaluation completion,
        List<SeedEvaluation> overworld,
        NetherEvaluation nether,
        TerrainEvidence terrain,
        long rngMarker
) {
    public EvaluationResult {
        stages = List.copyOf(stages);
        overworld = List.copyOf(overworld);
    }

    public String report() {
        var out = new StringBuilder("MCSR ANALYZE seed=").append(seed).append('\n');
        for (var stage : stages) {
            out.append(stage.status()).append(" ").append(stage.name())
                    .append(": ").append(stage.detail()).append('\n');
        }
        out.append("RESULT: ").append(status).append('\n');
        return out.toString().trim();
    }
}
