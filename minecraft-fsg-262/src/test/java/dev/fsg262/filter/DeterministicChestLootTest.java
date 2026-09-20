package dev.fsg262.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicChestLootTest {
    @Test
    void sameSeedPositionAndCategoryAreStable() {
        var first = DeterministicChestLoot.forChest(1234L, 99L,
                DeterministicChestLoot.StructureCategory.BASTION);
        assertEquals(first, DeterministicChestLoot.forChest(1234L, 99L,
                DeterministicChestLoot.StructureCategory.BASTION));
        assertTrue(first.stream().allMatch(entry -> entry.count() >= 0));
    }

    @Test
    void everySupportedCategoryProducesModelOutput() {
        for (var category : DeterministicChestLoot.StructureCategory.values()) {
            assertTrue(!DeterministicChestLoot.forChest(1L, 2L, category).isEmpty(), category.name());
        }
    }
}
