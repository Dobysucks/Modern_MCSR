package dev.fsg262.filter;

import dev.fsg262.completion.EvidenceType;

/**
 * Deterministic prototype starting-resource model. It is explicitly
 * simulation evidence, never a claim about a generated chest or drop.
 */
public record DeterministicStartingLoot(
        int iron, int diamonds, boolean food, boolean blacksmith,
        int blacksmithObsidian, boolean nearbyWood, int ironNuggets,
        boolean reliableIgnition, EvidenceType evidenceType) {
    public DeterministicStartingLoot {
        if (iron < 0 || diamonds < 0 || blacksmithObsidian < 0 || ironNuggets < 0) {
            throw new IllegalArgumentException("simulated loot quantities cannot be negative");
        }
        if (evidenceType != EvidenceType.SIMULATED) {
            throw new IllegalArgumentException("starting loot simulation must be SIMULATED evidence");
        }
    }

    public static DeterministicStartingLoot forSeed(long seed, StartType type) {
        long mixed = seed ^ ((long) type.ordinal() * 0x9E3779B97F4A7C15L);
        return new DeterministicStartingLoot(
                7 + (int) Math.floorMod(mixed, 3),
                3 + (int) Math.floorMod(mixed >>> 8, 2),
                true, true, 10, true, 8, true, EvidenceType.SIMULATED);
    }
}
