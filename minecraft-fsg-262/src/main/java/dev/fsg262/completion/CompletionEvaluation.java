package dev.fsg262.completion;

import java.util.List;

public record CompletionEvaluation(
        VerificationStatus status,
        boolean filterPassed,
        List<String> failures,
        List<String> notVerified
) {
    public CompletionEvaluation {
        failures = List.copyOf(failures);
        notVerified = List.copyOf(notVerified);
    }

    public String report() {
        var out = new StringBuilder("COMPLETION: ").append(status).append('\n');
        failures.forEach(failure -> out.append("[FAIL] ").append(failure).append('\n'));
        notVerified.forEach(item -> out.append("[NOT VERIFIED] ").append(item).append('\n'));
        return out.toString();
    }
}