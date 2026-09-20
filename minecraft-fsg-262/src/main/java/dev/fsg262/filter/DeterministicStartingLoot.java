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
        var category = DeterministicChestLoot.StructureCategory.valueOf(type.name());
        var entries = DeterministicChestLoot.forEvaluation(seed, category);
        int iron = entries.stream().filter(entry -> entry.item().equals("minecraft:iron_ingot"))
                .mapToInt(DeterministicChestLoot.Entry::count).sum();
        int obsidian = entries.stream().filter(entry -> entry.item().equals("minecraft:obsidian"))
                .mapToInt(DeterministicChestLoot.Entry::count).sum();
        boolean food = entries.stream().anyMatch(entry -> entry.item().equals("minecraft:bread"));
        long mixed = seed ^ ((long) type.ordinal() * 0x9E3779B97F4A7C15L);
        return new DeterministicStartingLoot(
                iron, 3 + (int) Math.floorMod(mixed >>> 8, 2),
                food, true, obsidian, true, 8, true, EvidenceType.SIMULATED);
    }
}
