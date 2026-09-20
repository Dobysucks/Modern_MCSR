package dev.fsg262.filter;

import dev.fsg262.rng.ModernRng262;

import java.util.List;

/**
 * Shared, seed/position-stable chest prototype model.  This is deliberately
 * data-only so filtering and the runtime injector cannot silently diverge.
 */
public final class DeterministicChestLoot {
    public enum StructureCategory {
        VILLAGE, SHIPWRECK, DESERT_TEMPLE, RUINED_PORTAL, BURIED_TREASURE, BASTION
    }

    public record Entry(String item, int count) {}

    private DeterministicChestLoot() {}

    public static List<Entry> forChest(long worldSeed, long blockPos, StructureCategory category) {
        long mixed = ModernRng262.mix64(worldSeed ^ blockPos
                ^ ((long) category.ordinal() * 0x9E3779B97F4A7C15L));
        return switch (category) {
            case VILLAGE -> List.of(new Entry("minecraft:iron_ingot", 2 + (int) Math.floorMod(mixed, 4)),
                    new Entry("minecraft:obsidian", 2 + (int) Math.floorMod(mixed >>> 16, 4)),
                    new Entry("minecraft:bread", 2 + (int) Math.floorMod(mixed >>> 24, 3)));
            case SHIPWRECK -> List.of(new Entry("minecraft:iron_ingot", 2 + (int) Math.floorMod(mixed, 3)),
                    new Entry("minecraft:bread", 2 + (int) Math.floorMod(mixed >>> 8, 3)));
            case DESERT_TEMPLE -> List.of(new Entry("minecraft:iron_ingot", 2 + (int) Math.floorMod(mixed, 4)));
            case RUINED_PORTAL -> List.of(new Entry("minecraft:obsidian", 1 + (int) Math.floorMod(mixed, 2)));
            case BURIED_TREASURE -> List.of(new Entry("minecraft:iron_ingot", 3 + (int) Math.floorMod(mixed, 4)));
            case BASTION -> List.of(new Entry("minecraft:gold_ingot", 2 + (int) Math.floorMod(mixed, 5)),
                    new Entry("minecraft:obsidian", (int) Math.floorMod(mixed >>> 8, 2)));
        };
    }

    /** Canonical position used by the evaluator's prototype resource model. */
    public static List<Entry> forEvaluation(long worldSeed, StructureCategory category) {
        var entries = new java.util.ArrayList<Entry>();
        for (long chestIdentity = 0; chestIdentity < 3; chestIdentity++) {
            entries.addAll(forChest(worldSeed, chestIdentity, category));
        }
        return List.copyOf(entries);
    }
}
