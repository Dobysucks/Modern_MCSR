package dev.fsg262.world;

import java.util.concurrent.atomic.AtomicReference;

public final class McsrWorldSeedState {
    private static final AtomicReference<WorldSeeds> PENDING = new AtomicReference<>();

    private McsrWorldSeedState() {}

    public static void prepareWorldCreation(long overworldSeed, long netherSeed,
                                            boolean disablePiglinBrutes,
                                            boolean standardizedRng) {
        PENDING.set(new WorldSeeds(overworldSeed, netherSeed, disablePiglinBrutes,
                standardizedRng));
    }

    public static WorldSeeds consumePending() {
        return PENDING.getAndSet(null);
    }

    public static void clearPending() {
        PENDING.set(null);
    }

    public record WorldSeeds(long mcsr_overworld_seed, long mcsr_nether_seed,
                             boolean disablePiglinBrutes, boolean standardizedRng) {}
}
