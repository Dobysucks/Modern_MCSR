package dev.fsg262.filter;

/**
 * Boundary between Minecraft 26.2 APIs and the pure filter engine.
 *
 * <p>An implementation must use biome/structure placement and structure
 * generation APIs directly. It must not create and walk a complete world for
 * every candidate seed.
 */
public interface WorldGenerationAnalyzer {
    StartEvaluationInput analyzeOverworld(long seed, StartType startType, FilterProfile profile);
}