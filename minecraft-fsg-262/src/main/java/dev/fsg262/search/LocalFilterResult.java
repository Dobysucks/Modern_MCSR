package dev.fsg262.search;

import dev.fsg262.completion.CompletionEvaluation;
import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.SeedEvaluation;

import java.util.List;

public record LocalFilterResult(
        VerificationStatus status,
        String reason,
        List<SeedEvaluation> overworld,
        VerificationStatus rng,
        CompletionEvaluation completion
) {
    public LocalFilterResult {
        overworld = List.copyOf(overworld);
    }

    public boolean accepted() {
        return status == VerificationStatus.PASS;
    }

    public String report() {
        return "LOCAL FILTER: " + status + "\n"
                + "Reason: " + reason + "\n"
                + "RNG: " + rng + "\n"
                + "Completion: " + completion.status();
    }
}
