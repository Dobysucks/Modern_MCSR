package dev.fsg262.seeddb;

import java.io.IOException;

public final class SeedCreationLifecycle {
    private PersistentSeedSelector.Reservation overworld;
    private PersistentSeedSelector.Reservation nether;
    private boolean creating;

    public synchronized void selectOverworld(
            PersistentSeedSelector.Reservation reservation) {
        releaseOverworld();
        overworld = reservation;
    }

    public synchronized void selectNether(
            PersistentSeedSelector.Reservation reservation) {
        releaseNether();
        nether = reservation;
    }

    public synchronized void beginCreation() {
        creating = true;
    }

    public synchronized void screenRemoved() {
        if (!creating) releaseAll();
    }

    public synchronized void creationFailed() {
        creating = false;
        releaseAll();
    }

    public synchronized void creationSucceeded() throws IOException {
        if (!creating) return;
        try {
            if (overworld != null) {
                overworld.commit();
                overworld = null;
            }
            if (nether != null) {
                nether.commit();
                nether = null;
            }
            creating = false;
        } catch (IOException | RuntimeException exception) {
            creating = false;
            releaseAll();
            throw exception;
        }
    }

    public synchronized boolean isCreating() {
        return creating;
    }

    public synchronized Long overworldSeed() {
        return overworld == null ? null : overworld.seed();
    }

    public synchronized Long netherSeed() {
        return nether == null ? null : nether.seed();
    }

    private void releaseAll() {
        releaseOverworld();
        releaseNether();
    }

    private void releaseOverworld() {
        if (overworld == null) return;
        overworld.release();
        overworld = null;
    }

    private void releaseNether() {
        if (nether == null) return;
        nether.release();
        nether = null;
    }
}
