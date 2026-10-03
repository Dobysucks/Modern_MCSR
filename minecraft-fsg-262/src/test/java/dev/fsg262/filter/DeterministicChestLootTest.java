package dev.fsg262.filter;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicChestLootTest {
    @Test
    void sameSeedPositionAndCategoryAreStable() {
        var first = DeterministicChestLoot.forChest(1234L, 99L,
                DeterministicChestLoot.StructureCategory.BASTION);
        assertEquals(first, DeterministicChestLoot.forChest(1234L, 99L,
                DeterministicChestLoot.StructureCategory.BASTION));
        assertTrue(first.stream().allMatch(entry -> entry.count() > 0 && entry.count() <= 64));
    }

    @Test
    void everySupportedCategoryProducesModelOutput() {
        for (var category : DeterministicChestLoot.StructureCategory.values()) {
            assertTrue(!DeterministicChestLoot.forChest(1L, 2L, category).isEmpty(), category.name());
        }
    }

    @Test
    void differentRngSeedsProduceVariedValidLoot() {
        Set<java.util.List<DeterministicChestLoot.Entry>> outcomes = new HashSet<>();
        for (long seed = 0; seed < 50; seed++) {
            var entries = DeterministicChestLoot.forChest(seed, 123456L,
                    DeterministicChestLoot.StructureCategory.SHIPWRECK);
            assertTrue(entries.stream().allMatch(entry ->
                    entry.item().startsWith("minecraft:")
                            && entry.count() > 0 && entry.count() <= 64));
            outcomes.add(entries);
        }
        assertTrue(outcomes.size() > 1);
    }

    @Test
    void lootTableIdentityChangesPerStructureChestLoot() {
        var supply = DeterministicChestLoot.forChest(42L, 99L,
                DeterministicChestLoot.StructureCategory.SHIPWRECK,
                "minecraft:chests/shipwreck_supply");
        var map = DeterministicChestLoot.forChest(42L, 99L,
                DeterministicChestLoot.StructureCategory.SHIPWRECK,
                "minecraft:chests/shipwreck_map");

        assertNotEquals(supply, map);
    }

    @Test
    void onlyExactVanillaSupportedLootTablesAreRecognized() {
        assertEquals(DeterministicChestLoot.StructureCategory.VILLAGE,
                DeterministicChestLoot.categoryForLootTable("minecraft",
                        "chests/village/village_plains_house"));
        assertEquals(DeterministicChestLoot.StructureCategory.SHIPWRECK,
                DeterministicChestLoot.categoryForLootTable("minecraft",
                        "chests/shipwreck_treasure"));
        assertEquals(DeterministicChestLoot.StructureCategory.DESERT_TEMPLE,
                DeterministicChestLoot.categoryForLootTable("minecraft",
                        "chests/desert_pyramid"));
        assertEquals(DeterministicChestLoot.StructureCategory.RUINED_PORTAL,
                DeterministicChestLoot.categoryForLootTable("minecraft",
                        "chests/ruined_portal"));
        assertEquals(DeterministicChestLoot.StructureCategory.BURIED_TREASURE,
                DeterministicChestLoot.categoryForLootTable("minecraft",
                        "chests/buried_treasure"));
        assertEquals(DeterministicChestLoot.StructureCategory.BASTION,
                DeterministicChestLoot.categoryForLootTable("minecraft",
                        "chests/bastion_hoglin_stable"));
        assertNull(DeterministicChestLoot.categoryForLootTable("minecraft",
                "chests/stronghold_corridor"));
        assertNull(DeterministicChestLoot.categoryForLootTable("othermod",
                "chests/village/custom_house"));
    }

    @Test
    void standardizedRngControlsSlotLayoutAndLeavesNaturalEmptySlots() {
        var first = DeterministicChestLoot.forChestLayout(918273L, 456L,
                DeterministicChestLoot.StructureCategory.BURIED_TREASURE,
                "minecraft:chests/buried_treasure", 27);
        var repeated = DeterministicChestLoot.forChestLayout(918273L, 456L,
                DeterministicChestLoot.StructureCategory.BURIED_TREASURE,
                "minecraft:chests/buried_treasure", 27);

        assertEquals(first, repeated);
        assertTrue(first.stream().allMatch(entry -> entry.slot() >= 0 && entry.slot() < 27));
        assertEquals(first.size(), first.stream().map(
                DeterministicChestLoot.PlacedEntry::slot).distinct().count());
        assertTrue(first.size() < 27);
        var changedSeed = DeterministicChestLoot.forChestLayout(918274L, 456L,
                DeterministicChestLoot.StructureCategory.BURIED_TREASURE,
                "minecraft:chests/buried_treasure", 27);
        assertNotEquals(first.stream().map(DeterministicChestLoot.PlacedEntry::slot).toList(),
                changedSeed.stream().map(DeterministicChestLoot.PlacedEntry::slot).toList());
    }

    @Test
    void usefulResourcesHaveHighProbabilityWithoutProducingOneFixedJackpot() {
        var usefulItems = Set.of("minecraft:iron_ingot", "minecraft:iron_nugget",
                "minecraft:bread", "minecraft:obsidian", "minecraft:gold_ingot",
                "minecraft:emerald", "minecraft:diamond", "minecraft:flint_and_steel");
        var outcomes = new HashSet<java.util.List<DeterministicChestLoot.Entry>>();
        var usefulOutcomes = 0;
        for (long seed = 0; seed < 250; seed++) {
            var entries = DeterministicChestLoot.forChest(seed, seed * 31,
                    DeterministicChestLoot.StructureCategory.VILLAGE);
            outcomes.add(entries);
            if (entries.stream().anyMatch(entry -> usefulItems.contains(entry.item()))) {
                usefulOutcomes++;
            }
        }

        assertTrue(usefulOutcomes > 220);
        assertTrue(outcomes.size() > 100);
    }

    @Test
    void allCategoriesKeepSlotCountAndStackQuantitiesWithinChestLimits() {
        for (var category : DeterministicChestLoot.StructureCategory.values()) {
            var layout = DeterministicChestLoot.forChestLayout(99L, 123L, category,
                    "minecraft:chests/" + category.name().toLowerCase(), 27);
            assertTrue(!layout.isEmpty(), category.name());
            assertTrue(layout.stream().allMatch(entry -> entry.slot() < 27
                    && entry.count() > 0 && entry.count() <= 64), category.name());
        }
    }
}
