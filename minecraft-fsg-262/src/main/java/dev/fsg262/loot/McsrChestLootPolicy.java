package dev.fsg262.loot;

import dev.fsg262.filter.DeterministicChestLoot;

public final class McsrChestLootPolicy {
    private McsrChestLootPolicy() {}

    public static boolean shouldPopulate(boolean mcsrWorld, boolean alreadyInitialized,
                                         boolean inventoryEmpty, boolean hasLootTable,
                                         DeterministicChestLoot.StructureCategory category) {
        return mcsrWorld && !alreadyInitialized && inventoryEmpty && hasLootTable
                && category != null;
    }
}
