package dev.fsg262.rng;

/**
 * Compatibility stream for mechanics that need a named legacy sequence.
 * Vanilla's full 1.16.1 random state is not globally replaced.
 */
public final class LegacyRng1161 implements SpeedrunRng {
    private static final long MULTIPLIER = 25214903917L;
    private static final long ADDEND = 11L;
    private static final long MASK = (1L << 48) - 1;
    private final long seed;

    public LegacyRng1161(long seed) {
        this.seed = (seed ^ MULTIPLIER) & MASK;
    }

    @Override
    public long nextLong(long streamIndex) {
        long state = seed;
        long steps = Math.max(0, streamIndex) * 2 + 1;
        for (long i = 0; i < steps; i++) {
            state = (state * MULTIPLIER + ADDEND) & MASK;
        }
        return (state << 16) ^ ((state * MULTIPLIER + ADDEND) & MASK);
    }
}