package dev.fsg262.lava;

public record LavaSearchConfig(
        int maximumDistanceBlocks,
        int maximumExcavationBlocks,
        int artificialPoolCount
) {
    public LavaSearchConfig {
        if (maximumDistanceBlocks < 0 || maximumExcavationBlocks < 0 || artificialPoolCount < 0) {
            throw new IllegalArgumentException("Lava configuration values cannot be negative");
        }
    }

    public static LavaSearchConfig defaults() {
        // The brief specifies 32 blocks and exactly 3 fallback pools. The
        // excavation limit is configurable because it is not numerically
        // specified by the reference behavior.
        return new LavaSearchConfig(32, 8, 3);
    }
}