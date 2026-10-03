package dev.fsg262.seeddb;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedDatabaseTest {
    @Test
    void bundledDatabasesMatchTheirDeclaredCounts() {
        var overworld = SeedDatabaseResources.overworld();
        var nether = SeedDatabaseResources.nether();

        assertEquals(500_236, overworld.size());
        assertEquals(overworld.size(), overworld.declaredCount());
        assertEquals(0, overworld.malformedLineCount());
        assertEquals(627_337, nether.size());
        assertEquals(nether.size(), nether.declaredCount());
        assertEquals(0, nether.malformedLineCount());
    }

    @Test
    void everyCreateWorldCategoryHasBundledCandidates() {
        var overworld = SeedDatabaseResources.overworld();
        var nether = SeedDatabaseResources.nether();

        for (var category : dev.fsg262.client.world.WorldCreationSettings.OVERWORLD_CATEGORIES) {
            assertTrue(overworld.indexesWithTag(category).length > 0, category);
        }
        for (var category : dev.fsg262.client.world.WorldCreationSettings.NETHER_CATEGORIES) {
            assertTrue(nether.indexesWithTag(category).length > 0, category);
        }
    }

    @Test
    void parserPreservesSignedLongsAndSkipsMalformedEntries() throws Exception {
        var database = SeedDatabase.parse(new StringReader("""
                Minecraft Java: 26.2
                Filter: MCSR-26.2-ADAPTED-v4
                Generated count: 4

                malformed first row
                SEED: -9223372036854775808 (Village)
                SEED: invalid (Ruined Portal)
                SEED: 9223372036854775807 (Buried Treasure)
                not a seed row
                SEED: 0 (Shipwreck)
                SEED: -42 (Village, Ruined Portal)
                """));

        assertEquals(4, database.size());
        assertEquals(3, database.malformedLineCount());
        assertEquals(Long.MIN_VALUE, database.seedAt(0));
        assertEquals(Long.MAX_VALUE, database.seedAt(1));
        assertEquals(0L, database.seedAt(2));
        assertEquals(-42L, database.seedAt(3));
        assertEquals(List.of(5, 7, 9), database.malformedLineNumbers());
        assertEquals(2, database.indexesWithTag("Village").length);
        assertEquals(1, database.indexesWithTag("Ruined Portal").length);
        assertEquals(1, database.indexesWithTag("Shipwreck").length);
    }

    @Test
    void resourceLoaderKeepsValidOverworldAndNetherRowsAroundMalformedLines() {
        var malformedOverworld = SeedDatabaseResources.load(new StringReader("""
                Minecraft Java: 26.2
                Filter: MCSR-26.2-ADAPTED-v4
                Generated count: 2
                SEED: malformed (Village)
                SEED: -7000000000000000000 (Village)
                """), "overworld_seeds.txt");
        var malformedNether = SeedDatabaseResources.load(new StringReader("""
                Minecraft Java: 26.2
                Filter: MCSR-26.2-ADAPTED-v4
                Generated count: 2
                SEED: 9223372036854775808 (Fortress)
                SEED: 7000000000000000000 (Fortress)
                """), "nether_seeds.txt");

        assertEquals(1, malformedOverworld.size());
        assertEquals(-7_000_000_000_000_000_000L, malformedOverworld.seedAt(0));
        assertEquals(1, malformedOverworld.malformedLineCount());
        assertEquals(List.of(4), malformedOverworld.malformedLineNumbers());
        assertFalse(malformedOverworld.declaredCountMatchesParsedCount());
        assertEquals(1, malformedNether.size());
        assertEquals(7_000_000_000_000_000_000L, malformedNether.seedAt(0));
        assertEquals(1, malformedNether.malformedLineCount());
        assertEquals(List.of(4), malformedNether.malformedLineNumbers());
        assertFalse(malformedNether.declaredCountMatchesParsedCount());
    }
}
