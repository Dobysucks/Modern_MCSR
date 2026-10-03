package dev.fsg262.seeddb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentSeedSelectorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void consumedSeedsAreSkippedAfterRestart() throws Exception {
        var database = database(1, 2, 3);
        var stateFile = temporaryDirectory.resolve("overworld.bitmap");
        var first = new PersistentSeedSelector(database, stateFile);
        var reservation = first.reserveNext().orElseThrow();
        var consumed = reservation.seed();
        reservation.commit();

        var restarted = new PersistentSeedSelector(database, stateFile);
        assertEquals(2, restarted.remainingCount());
        assertEquals(1, restarted.consumedCount());
        var nextReservation = restarted.reserveNext().orElseThrow();
        var next = nextReservation.seed();
        nextReservation.commit();
        assertFalse(consumed == next);
        var finalReservation = restarted.reserveNext().orElseThrow();
        var finalSeed = finalReservation.seed();
        finalReservation.commit();
        assertFalse(consumed == finalSeed);
        assertFalse(next == finalSeed);
        assertTrue(restarted.reserveNext().isEmpty());
    }

    @Test
    void dimensionAndProfileFilesKeepIndependentMembership() throws Exception {
        var database = database(-17, Long.MIN_VALUE, Long.MAX_VALUE);
        var overworld = new PersistentSeedSelector(database,
                temporaryDirectory.resolve("overworld/strict.bitmap"));
        var nether = new PersistentSeedSelector(database,
                temporaryDirectory.resolve("nether/strict.bitmap"));
        var alternateProfile = new PersistentSeedSelector(database,
                temporaryDirectory.resolve("overworld/balanced.bitmap"));

        var selectedOverworld = overworld.reserveNext().orElseThrow();
        assertEquals(3, nether.remainingCount());
        assertEquals(3, alternateProfile.remainingCount());
        assertEquals(3, new PersistentSeedSelector(database,
                temporaryDirectory.resolve("overworld/strict.bitmap")).remainingCount());
        assertEquals(3, database.size());
        assertTrue(Set.of(-17L, Long.MIN_VALUE, Long.MAX_VALUE).contains(selectedOverworld.seed()));
        selectedOverworld.release();
    }

    @Test
    void persistedStateRejectsChangedDatabase() throws Exception {
        var stateFile = temporaryDirectory.resolve("consumed.bitmap");
        var original = database(1, 2);
        new PersistentSeedSelector(original, stateFile).reserveNext().orElseThrow().commit();
        var changed = database(1, 3);
        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                java.io.IOException.class, () -> new PersistentSeedSelector(changed, stateFile));
        assertTrue(exception.getMessage().contains("does not match"));
    }

    @Test
    void categoryTagChangesPreservePersistedConsumptionForTheSameSeedDatabase() throws Exception {
        var stateFile = temporaryDirectory.resolve("tag-metadata.bitmap");
        var original = SeedDatabase.parse(new StringReader("""
                Generated count: 2
                SEED: 71 (Village)
                SEED: 72 (Shipwreck)
                """));
        new PersistentSeedSelector(original, stateFile).reserve(71).orElseThrow().commit();

        var sameSeedsWithDifferentTags = SeedDatabase.parse(new StringReader("""
                Generated count: 2
                SEED: 71 (Desert Temple)
                SEED: 72 (Ruined Portal)
                """));
        var afterUpgrade = new PersistentSeedSelector(sameSeedsWithDifferentTags, stateFile);

        assertEquals(1, afterUpgrade.consumedCount());
        assertTrue(afterUpgrade.reserve(71).isEmpty());
    }

    @Test
    void explicitReservationSkipsConsumedDuplicateRowAndUsesNextOccurrence() throws Exception {
        var database = database(81, 81);
        var selector = new PersistentSeedSelector(database,
                temporaryDirectory.resolve("duplicate-seed.bitmap"));

        selector.reserve(81).orElseThrow().commit();

        assertTrue(selector.reserve(81).isPresent());
    }

    @Test
    void selectingWithoutWorldCreationDoesNotConsumeSeed() throws Exception {
        var database = database(11, 22, 33);
        var stateFile = temporaryDirectory.resolve("selection-only.bitmap");
        var selector = new PersistentSeedSelector(database, stateFile);
        var reservation = selector.reserveNext().orElseThrow();

        assertEquals(0, selector.consumedCount());
        assertEquals(1, selector.reservedCount());
        var afterRestart = new PersistentSeedSelector(database, stateFile);
        assertEquals(3, afterRestart.remainingCount());
        assertEquals(0, afterRestart.consumedCount());

        reservation.release();
        assertEquals(0, selector.reservedCount());
        assertEquals(3, selector.remainingCount());
    }

    @Test
    void cancellingSelectionReleasesItWithoutConsuming() throws Exception {
        var database = database(4, 5);
        var stateFile = temporaryDirectory.resolve("cancelled.bitmap");
        var selector = new PersistentSeedSelector(database, stateFile);
        var reservation = selector.reserveNext().orElseThrow();

        reservation.release();

        assertEquals(0, selector.consumedCount());
        assertEquals(0, selector.reservedCount());
        assertEquals(2, new PersistentSeedSelector(database, stateFile).remainingCount());
    }

    @Test
    void creationFailureBeforeCommitLeavesSeedAvailableAfterRestart() throws Exception {
        var database = database(7, 8);
        var stateFile = temporaryDirectory.resolve("failed-creation.bitmap");
        var selector = new PersistentSeedSelector(database, stateFile);
        var reservation = selector.reserveNext().orElseThrow();

        reservation.release();

        var afterFailure = new PersistentSeedSelector(database, stateFile);
        assertEquals(2, afterFailure.remainingCount());
        assertEquals(0, afterFailure.consumedCount());
    }

    @Test
    void changingCandidateReleasesPreviousSeedWithoutConsumingEither() throws Exception {
        var database = database(31, 32, 33);
        var stateFile = temporaryDirectory.resolve("changed-selection.bitmap");
        var selector = new PersistentSeedSelector(database, stateFile);
        var previous = selector.reserveNext().orElseThrow();
        var replacement = selector.reserveNext().orElseThrow();

        previous.release();

        assertFalse(previous.seed() == replacement.seed());
        assertEquals(0, selector.consumedCount());
        assertEquals(1, selector.reservedCount());
        var canReReservePrevious = selector.reserve(previous.seed()).orElseThrow();
        assertEquals(previous.seed(), canReReservePrevious.seed());
        canReReservePrevious.release();
        replacement.release();
        assertEquals(3, new PersistentSeedSelector(database, stateFile).remainingCount());
    }

    @Test
    void successfulCreationCommitConsumesEachDimensionIndependently() throws Exception {
        var overworldDb = database(101, 102, 103);
        var netherDb = database(201, 202);
        var overworldFile = temporaryDirectory.resolve("commit/overworld.bitmap");
        var netherFile = temporaryDirectory.resolve("commit/nether.bitmap");
        var overworld = new PersistentSeedSelector(overworldDb, overworldFile);
        var nether = new PersistentSeedSelector(netherDb, netherFile);
        var overworldReservation = overworld.reserveNext().orElseThrow();
        var netherReservation = nether.reserveNext().orElseThrow();

        overworldReservation.commit();
        netherReservation.commit();

        assertEquals(1, new PersistentSeedSelector(overworldDb, overworldFile).consumedCount());
        assertEquals(1, new PersistentSeedSelector(netherDb, netherFile).consumedCount());
        assertEquals(3, new PersistentSeedSelector(overworldDb,
                temporaryDirectory.resolve("commit/other-overworld-profile.bitmap")).remainingCount());
        assertEquals(3, new PersistentSeedSelector(overworldDb,
                temporaryDirectory.resolve("commit/overworld-other-dimension.bitmap")).remainingCount());
    }

    @Test
    void categorySelectionOnlyReservesMatchingSeedsAndSkipsCommittedMatches() throws Exception {
        var database = SeedDatabase.parse(new StringReader("""
                Generated count: 4
                SEED: 11 (Village)
                SEED: 22 (Shipwreck, Village)
                SEED: 33 (Shipwreck)
                SEED: 44 (Ruined Portal)
                """));
        var stateFile = temporaryDirectory.resolve("category-selection.bitmap");
        var selector = new PersistentSeedSelector(database, stateFile);

        var village = selector.reserveNextMatching("Village").orElseThrow();
        assertTrue(village.seed() == 11 || village.seed() == 22);
        var consumedVillage = village.seed();
        village.commit();
        var shipwreck = selector.reserveNextMatching("Shipwreck").orElseThrow();
        assertTrue(shipwreck.seed() == 22 || shipwreck.seed() == 33);
        shipwreck.release();

        var restarted = new PersistentSeedSelector(database, stateFile);
        var nextVillage = restarted.reserveNextMatching("Village").orElseThrow();
        assertTrue(nextVillage.seed() == 11 || nextVillage.seed() == 22);
        assertTrue(nextVillage.seed() != consumedVillage);
        nextVillage.commit();
        assertTrue(restarted.reserveNextMatching("missing").isEmpty());
        assertTrue(restarted.reserveNextMatching("Village").isEmpty());
    }

    @Test
    void categoryReplacementCanReleaseOldReservationWithoutConsumingIt() throws Exception {
        var database = SeedDatabase.parse(new StringReader("""
                Generated count: 2
                SEED: 101 (Village)
                SEED: 202 (Shipwreck)
                """));
        var selector = new PersistentSeedSelector(database,
                temporaryDirectory.resolve("category-replacement.bitmap"));

        var previous = selector.reserveNextMatching("Village").orElseThrow();
        var replacement = selector.reserveNextMatching("Shipwreck").orElseThrow();
        previous.release();

        var returned = selector.reserveNextMatching("Village").orElseThrow();
        assertEquals(101, returned.seed());
        assertEquals(0, selector.consumedCount());
        returned.release();
        replacement.release();
    }

    private static SeedDatabase database(long... seeds) throws Exception {
        var content = new StringBuilder("Generated count: ")
                .append(seeds.length).append('\n');
        for (long seed : seeds) content.append("SEED: ").append(seed).append(" (Village)\n");
        return SeedDatabase.parse(new StringReader(content.toString()));
    }
}
