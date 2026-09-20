package dev.fsg262.search;

import dev.fsg262.rng.ModernRng262;

public final class CandidateSeedGenerator {
    private final long baseSeed;

    public CandidateSeedGenerator(long baseSeed) {
        this.baseSeed = baseSeed;
    }

    public long seedAt(long index) {
        if (index < 0) throw new IllegalArgumentException("index cannot be negative");
        return new ModernRng262(baseSeed).nextLong(index);
    }
}