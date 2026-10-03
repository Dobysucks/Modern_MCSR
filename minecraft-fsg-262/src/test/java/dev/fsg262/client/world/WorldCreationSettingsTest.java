package dev.fsg262.client.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldCreationSettingsTest {
    @Test
    void defaultsUseIndependentValidDimensionCategories() {
        var settings = WorldCreationSettings.defaults();

        assertEquals("Village", settings.overworldCategory());
        assertEquals("Fortress", settings.netherCategory());
        assertNull(settings.overworldSeed());
        assertNull(settings.netherSeed());
    }

    @Test
    void changingOneCategoryClearsOnlyThatDimensionsCandidate() {
        var settings = new WorldCreationSettings(true, "BALANCED", true, true, true,
                "Village", "Stables", 123L, 456L);

        var changedOverworld = settings.withOverworldCategory("Shipwreck");
        assertEquals("Shipwreck", changedOverworld.overworldCategory());
        assertNull(changedOverworld.overworldSeed());
        assertEquals(456L, changedOverworld.netherSeed());

        var changedNether = settings.withNetherCategory("Bridge");
        assertEquals("Bridge", changedNether.netherCategory());
        assertEquals(123L, changedNether.overworldSeed());
        assertNull(changedNether.netherSeed());
    }

    @Test
    void invalidCategoryCannotBeConfigured() {
        assertThrows(IllegalArgumentException.class, () -> new WorldCreationSettings(
                true, "BALANCED", true, true, true, "Stronghold", "Fortress",
                null, null));
    }
}
