package dev.fsg262.filter;

import dev.fsg262.completion.EvidenceType;

/**
 * Deterministic prototype adapter. It supplies simulation evidence so the
 * evaluator can exercise the complete pipeline without pretending to have
 * inspected a 26.2 server world.
 */
public final class SimulatedWorldGenerationAnalyzer implements WorldGenerationAnalyzer {
    @Override
    public StartEvaluationInput analyzeOverworld(long seed, StartType type, FilterProfile profile) {
        var loot = DeterministicStartingLoot.forSeed(seed, type);
        int distance = switch (type) {
            case VILLAGE -> 7;
            case SHIPWRECK -> 4;
            case DESERT_TEMPLE -> 5;
            case RUINED_PORTAL -> 3;
            case BURIED_TREASURE -> 5;
        };
        return new StartEvaluationInput(seed, type, distance, loot.iron(), loot.diamonds(),
                loot.food(), 0, 3, loot.blacksmith(), loot.blacksmithObsidian(),
                false, loot.nearbyWood(), 2, loot.ironNuggets(),
                loot.reliableIgnition(), true, true, false, true, EvidenceType.SIMULATED);
    }

    @Override
    public boolean isVerified() {
        return true;
    }
}
