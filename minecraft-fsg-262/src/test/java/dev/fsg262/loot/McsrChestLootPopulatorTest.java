package dev.fsg262.loot;

import dev.fsg262.filter.DeterministicChestLoot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McsrChestLootPopulatorTest {
    @Test
    void generatedLootWritesToRawInventoryAndLeavesEmptySlots() {
        var layout = DeterministicChestLoot.forChestLayout(1234L, 9876L,
                DeterministicChestLoot.StructureCategory.VILLAGE,
                "minecraft:chests/village/village_plains_house", 27);
        var rawItems = emptyInventory(27);

        assertTrue(writeInventory(rawItems, layout));
        assertTrue(rawItems.stream().anyMatch(item -> !item.empty()));
        assertTrue(rawItems.stream().anyMatch(TestStack::empty));
    }

    @Test
    void emptyOrUnmappableLootCannotBeMarkedAsInitializedByCaller() {
        var rawItems = emptyInventory(27);
        var unmappable = List.of(new DeterministicChestLoot.PlacedEntry(
                0, "minecraft:not_a_real_item", 1));

        assertFalse(writeInventory(rawItems, List.of()));
        assertFalse(writeInventory(rawItems, unmappable));
        assertTrue(rawItems.stream().allMatch(TestStack::empty));
    }

    @Test
    void twoChestBlockEntitiesPopulateTheirOwnPersistentInventories() {
        var category = DeterministicChestLoot.StructureCategory.SHIPWRECK;
        var firstHalf = DeterministicChestLoot.forChestLayout(55L, 100L, category,
                "minecraft:chests/shipwreck_supply", 27);
        var secondHalf = DeterministicChestLoot.forChestLayout(55L, 101L, category,
                "minecraft:chests/shipwreck_supply", 27);
        var firstInventory = emptyInventory(27);
        var secondInventory = emptyInventory(27);

        assertTrue(writeInventory(firstInventory, firstHalf));
        assertTrue(writeInventory(secondInventory, secondHalf));
        assertTrue(firstInventory.stream().anyMatch(item -> !item.empty()));
        assertTrue(secondInventory.stream().anyMatch(item -> !item.empty()));
        assertTrue(firstInventory.stream().anyMatch(TestStack::empty));
        assertTrue(secondInventory.stream().anyMatch(TestStack::empty));
    }

    @Test
    void nonemptyInventoryIsNeverOverwrittenOrRerolled() {
        var rawItems = emptyInventory(27);
        rawItems.set(4, new TestStack("minecraft:diamond", 1));
        var layout = DeterministicChestLoot.forChestLayout(10L, 20L,
                DeterministicChestLoot.StructureCategory.BURIED_TREASURE,
                "minecraft:chests/buried_treasure", 27);

        assertFalse(writeInventory(rawItems, layout));
        assertEquals(new TestStack("minecraft:diamond", 1), rawItems.get(4));
        assertEquals(1, rawItems.stream().filter(item -> !item.empty()).count());
    }

    private static boolean writeInventory(
            List<TestStack> rawItems, List<DeterministicChestLoot.PlacedEntry> entries) {
        return McsrChestLootPopulator.writeInventory(rawItems, entries,
                entry -> entry.item().equals("minecraft:not_a_real_item")
                        ? TestStack.EMPTY : new TestStack(entry.item(), entry.count()),
                TestStack::empty);
    }

    private static List<TestStack> emptyInventory(int size) {
        return new ArrayList<>(Collections.nCopies(size, TestStack.EMPTY));
    }

    private record TestStack(String item, int count) {
        private static final TestStack EMPTY = new TestStack("", 0);

        private boolean empty() {
            return count == 0;
        }
    }
}
