package dev.fsg262.loot;

/**
 * Explicit opt-in state for the gameplay prototype.  It is deliberately
 * empty by default, so ordinary worlds and worlds loaded after a restart are
 * never modified.
 */
public final class ChestLootInjectionState {
    private static volatile boolean enabled;
    private static volatile long acceptedSeed;
    private static volatile String configurationKey = "";

    private ChestLootInjectionState() {}

    public static void enable(long seed) {
        enable(seed, "");
    }

    public static void enable(long seed, String configuration) {
        acceptedSeed = seed;
        configurationKey = configuration == null ? "" : configuration;
        enabled = true;
    }

    public static void disable() {
        enabled = false;
        configurationKey = "";
    }

    public static boolean applies(long worldSeed) {
        return enabled && acceptedSeed == worldSeed;
    }

    public static boolean applies(long worldSeed, String configuration) {
        return applies(worldSeed) && configurationKey.equals(configuration == null ? "" : configuration);
    }
}
