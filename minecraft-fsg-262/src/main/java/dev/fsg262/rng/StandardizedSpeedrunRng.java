package dev.fsg262.rng;

public final class StandardizedSpeedrunRng implements SpeedrunRng {
    public enum Mode { LEGACY_1161, MODERN_262 }

    private final long seed;
    private final Mode mode;

    public StandardizedSpeedrunRng(long seed, Mode mode) {
        this.seed = seed;
        this.mode = mode;
    }

    @Override
    public long nextLong(long streamIndex) {
        return switch (mode) {
            case LEGACY_1161 -> new LegacyRng1161(seed).nextLong(streamIndex);
            case MODERN_262 -> new ModernRng262(seed).nextLong(streamIndex);
        };
    }

    public long seed() {
        return seed;
    }

    public Mode mode() {
        return mode;
    }
}