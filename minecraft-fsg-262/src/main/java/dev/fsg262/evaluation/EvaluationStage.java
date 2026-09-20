package dev.fsg262.evaluation;

import dev.fsg262.completion.VerificationStatus;

public record EvaluationStage(String name, VerificationStatus status, String detail) {
    public EvaluationStage {
        detail = detail == null || detail.isBlank() ? "no details" : detail;
    }
}
