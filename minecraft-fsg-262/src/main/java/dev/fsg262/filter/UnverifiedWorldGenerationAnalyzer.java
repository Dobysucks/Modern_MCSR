package dev.fsg262.filter;

/**
 * Safe placeholder until the locally resolved 26.2 world-generation names are
 * wired. It fails explicitly instead of making up structure facts.
 */
public final class UnverifiedWorldGenerationAnalyzer implements WorldGenerationAnalyzer {
    @Override
    public StartEvaluationInput analyzeOverworld(long seed, StartType startType, FilterProfile profile) {
        return new StartEvaluationInput(
                seed, startType, Double.POSITIVE_INFINITY,
                0, 0, false, Double.POSITIVE_INFINITY, 0,
                false, 0, false, false, 0, 0,
                false, false, false, true, false
        );
    }
}