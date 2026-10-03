package dev.fsg262.seeddb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedCreationLifecycleTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void backingOutAfterSelectingBothDimensionsReleasesBothReservations() throws Exception {
        var overworld = selector(101, "back/overworld.bitmap");
        var nether = selector(201, "back/nether.bitmap");
        var lifecycle = new SeedCreationLifecycle();
        lifecycle.selectOverworld(overworld.reserveNext().orElseThrow());
        lifecycle.selectNether(nether.reserveNext().orElseThrow());

        lifecycle.screenRemoved();

        assertFalse(lifecycle.isCreating());
        assertEquals(0, overworld.consumedCount());
        assertEquals(0, nether.consumedCount());
        assertEquals(1, new PersistentSeedSelector(database(101),
                temporaryDirectory.resolve("back/overworld.bitmap")).remainingCount());
        assertEquals(1, new PersistentSeedSelector(database(201),
                temporaryDirectory.resolve("back/nether.bitmap")).remainingCount());
    }

    @Test
    void leavingScreenAfterCreationBeginsPreservesReservationsUntilServerSuccess()
            throws Exception {
        var overworld = selector(111, "transition/overworld.bitmap");
        var nether = selector(211, "transition/nether.bitmap");
        var lifecycle = new SeedCreationLifecycle();
        lifecycle.selectOverworld(overworld.reserveNext().orElseThrow());
        lifecycle.selectNether(nether.reserveNext().orElseThrow());
        lifecycle.beginCreation();

        lifecycle.screenRemoved();

        assertTrue(lifecycle.isCreating());
        assertEquals(1, overworld.reservedCount());
        assertEquals(1, nether.reservedCount());
        assertEquals(0, overworld.consumedCount());
        assertEquals(0, nether.consumedCount());

        lifecycle.creationSucceeded();

        assertEquals(1, new PersistentSeedSelector(database(111),
                temporaryDirectory.resolve("transition/overworld.bitmap")).consumedCount());
        assertEquals(1, new PersistentSeedSelector(database(211),
                temporaryDirectory.resolve("transition/nether.bitmap")).consumedCount());
    }

    @Test
    void integratedServerStartupFailureAfterScreenExitReleasesBothSeeds() throws Exception {
        var overworld = selector(115, "startup-failure/overworld.bitmap");
        var nether = selector(215, "startup-failure/nether.bitmap");
        var lifecycle = new SeedCreationLifecycle();
        lifecycle.selectOverworld(overworld.reserveNext().orElseThrow());
        lifecycle.selectNether(nether.reserveNext().orElseThrow());
        lifecycle.beginCreation();
        lifecycle.screenRemoved();

        lifecycle.creationFailed();

        assertFalse(lifecycle.isCreating());
        assertEquals(0, overworld.consumedCount());
        assertEquals(0, nether.consumedCount());
        assertEquals(1, new PersistentSeedSelector(database(115),
                temporaryDirectory.resolve("startup-failure/overworld.bitmap")).remainingCount());
        assertEquals(1, new PersistentSeedSelector(database(215),
                temporaryDirectory.resolve("startup-failure/nether.bitmap")).remainingCount());
    }

    @Test
    void serverStoppingAfterSuccessfulCreationDoesNotUndoConsumption() throws Exception {
        var overworld = selector(117, "shutdown/overworld.bitmap");
        var nether = selector(217, "shutdown/nether.bitmap");
        var lifecycle = new SeedCreationLifecycle();
        lifecycle.selectOverworld(overworld.reserveNext().orElseThrow());
        lifecycle.selectNether(nether.reserveNext().orElseThrow());
        lifecycle.beginCreation();
        lifecycle.creationSucceeded();

        lifecycle.creationFailed();

        assertEquals(1, new PersistentSeedSelector(database(117),
                temporaryDirectory.resolve("shutdown/overworld.bitmap")).consumedCount());
        assertEquals(1, new PersistentSeedSelector(database(217),
                temporaryDirectory.resolve("shutdown/nether.bitmap")).consumedCount());
    }

    @Test
    void failedCreationReleasesBothDimensionReservations() throws Exception {
        var overworld = selector(121, "failed/overworld.bitmap");
        var nether = selector(221, "failed/nether.bitmap");
        var lifecycle = new SeedCreationLifecycle();
        lifecycle.selectOverworld(overworld.reserveNext().orElseThrow());
        lifecycle.selectNether(nether.reserveNext().orElseThrow());
        lifecycle.beginCreation();

        lifecycle.creationFailed();

        assertFalse(lifecycle.isCreating());
        assertEquals(0, overworld.consumedCount());
        assertEquals(0, nether.consumedCount());
        assertEquals(1, new PersistentSeedSelector(database(121),
                temporaryDirectory.resolve("failed/overworld.bitmap")).remainingCount());
        assertEquals(1, new PersistentSeedSelector(database(221),
                temporaryDirectory.resolve("failed/nether.bitmap")).remainingCount());
    }

    @Test
    void replacingSelectedOverworldReturnsPreviousSeedToAvailablePool() throws Exception {
        var database = database(131, 132);
        var selector = new PersistentSeedSelector(database,
                temporaryDirectory.resolve("replace/overworld.bitmap"));
        var lifecycle = new SeedCreationLifecycle();
        var first = selector.reserveNext().orElseThrow();
        var firstSeed = first.seed();
        lifecycle.selectOverworld(first);
        lifecycle.selectOverworld(selector.reserveNext().orElseThrow());

        var previousAvailable = selector.reserve(firstSeed).orElseThrow();
        assertEquals(firstSeed, previousAvailable.seed());
        previousAvailable.release();
        lifecycle.screenRemoved();
        assertEquals(2, new PersistentSeedSelector(database,
                temporaryDirectory.resolve("replace/overworld.bitmap")).remainingCount());
    }

    @Test
    void successfulCreationConsumesExactSelectedSeedsInTheirOwnDimensions() throws Exception {
        var overworld = selector(141, "success/overworld.bitmap");
        var nether = selector(241, "success/nether.bitmap");
        var lifecycle = new SeedCreationLifecycle();
        lifecycle.selectOverworld(overworld.reserveNext().orElseThrow());
        lifecycle.selectNether(nether.reserveNext().orElseThrow());
        assertEquals(141L, lifecycle.overworldSeed());
        assertEquals(241L, lifecycle.netherSeed());
        lifecycle.beginCreation();
        lifecycle.creationSucceeded();

        assertEquals(1, new PersistentSeedSelector(database(141),
                temporaryDirectory.resolve("success/overworld.bitmap")).consumedCount());
        assertEquals(1, new PersistentSeedSelector(database(241),
                temporaryDirectory.resolve("success/nether.bitmap")).consumedCount());
    }

    private PersistentSeedSelector selector(long seed, String relativePath) throws Exception {
        return new PersistentSeedSelector(database(seed), temporaryDirectory.resolve(relativePath));
    }

    private static SeedDatabase database(long... seeds) throws Exception {
        var content = new StringBuilder("Generated count: ")
                .append(seeds.length).append('\n');
        for (long seed : seeds) content.append("SEED: ").append(seed).append(" (Village)\n");
        return SeedDatabase.parse(new StringReader(content.toString()));
    }
}
