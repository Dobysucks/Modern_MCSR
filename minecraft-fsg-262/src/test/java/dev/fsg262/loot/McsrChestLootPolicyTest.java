package dev.fsg262.loot;

import dev.fsg262.filter.DeterministicChestLoot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McsrChestLootPolicyTest {
    private static final DeterministicChestLoot.StructureCategory CATEGORY =
            DeterministicChestLoot.StructureCategory.VILLAGE;

    @Test
    void onlyEmptyUninitializedSupportedMcsrChestsArePopulated() {
        assertTrue(McsrChestLootPolicy.shouldPopulate(true, false, true, true, CATEGORY));
        assertFalse(McsrChestLootPolicy.shouldPopulate(false, false, true, true, CATEGORY));
        assertFalse(McsrChestLootPolicy.shouldPopulate(true, false, true, true, null));
        assertFalse(McsrChestLootPolicy.shouldPopulate(true, false, false, true, CATEGORY));
        assertFalse(McsrChestLootPolicy.shouldPopulate(true, false, true, false, CATEGORY));
    }

    @Test
    void persistedInitializationMarkerPreventsRerollsAfterReload() {
        var firstOpenShouldPopulate =
                McsrChestLootPolicy.shouldPopulate(true, false, true, true, CATEGORY);

        assertTrue(firstOpenShouldPopulate);
        // ChestBlockEntityMixin writes this marker with saveAdditional and restores it
        // with loadAdditional before a re-opened block entity reaches this policy.
        var markerRestoredFromNbt = true;
        assertFalse(McsrChestLootPolicy.shouldPopulate(true, markerRestoredFromNbt,
                true, true, CATEGORY));
    }
}
