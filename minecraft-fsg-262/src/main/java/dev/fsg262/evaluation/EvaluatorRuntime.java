package dev.fsg262.evaluation;

import dev.fsg262.filter.UnverifiedWorldGenerationAnalyzer;
import dev.fsg262.filter.WorldGenerationAnalyzer;

/** Client-installed world-generation bridge used by diagnostic commands. */
public final class EvaluatorRuntime {
    private static volatile WorldGenerationAnalyzer analyzer =
            new UnverifiedWorldGenerationAnalyzer();
    private static volatile EvaluatorEvidenceProvider evidence =
            new EvaluatorEvidenceProvider() {};

    private EvaluatorRuntime() {}

    public static void install(WorldGenerationAnalyzer worldgen,
                               EvaluatorEvidenceProvider provider) {
        analyzer = worldgen;
        evidence = provider;
    }

    public static EvaluatorOrchestrator orchestrator() {
        return new EvaluatorOrchestrator(analyzer, evidence);
    }
}
