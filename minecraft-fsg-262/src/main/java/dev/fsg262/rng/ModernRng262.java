package dev.fsg262.rng;

/**
 * Stable independent stream used by standardized mode. It does not replace
 * Minecraft's global random source.
 */
public final class ModernRng262 implements SpeedrunRng {
    private final long seed;

    public ModernRng262(long seed) {
        this.seed = seed;
    }

    @Override
    public long nextLong(long streamIndex) {
        return mix64(seed + 0x9E3779B97F4A7C15L * (streamIndex + 1));
    }

    public static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}