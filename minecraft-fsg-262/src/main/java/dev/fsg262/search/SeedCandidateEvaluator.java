package dev.fsg262.search;

@FunctionalInterface
public interface SeedCandidateEvaluator {
    CandidateEvaluation evaluate(long candidateSeed, SeedSearchRequest request);
}