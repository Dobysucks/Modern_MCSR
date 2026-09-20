package dev.fsg262.rng;

import java.util.List;

/**
 * Named facade for the deterministic MCSR barter contract. Keeping this
 * separate from the legacy stream makes the evaluator's evidence explicit
 * while retaining the existing replay-compatible implementation.
 */
public final class McsrBarterSimulation {
    private final LegacyPiglinBartering bartering = new LegacyPiglinBartering();

    public BarterWindowCheck window(long rngSeed, int windowNumber) {
        if (windowNumber < 0) throw new IllegalArgumentException("windowNumber cannot be negative");
        return bartering.checkWindow(rngSeed, windowNumber * LegacyPiglinBartering.WINDOW_SIZE);
    }

    public boolean passes(long rngSeed, int windows, int minimumObsidian,
                          int exactPearlTrades) {
        if (windows <= 0 || minimumObsidian < 0 || exactPearlTrades < 0) {
            throw new IllegalArgumentException("invalid barter requirements");
        }
        for (int i = 0; i < windows; i++) {
            var result = window(rngSeed, i);
            if (result.obsidianTrades() < minimumObsidian
                    || result.pearlTrades() != exactPearlTrades) return false;
        }
        return true;
    }

    public List<BarterResult> sequence(long rngSeed, int count) {
        return bartering.sequence(rngSeed, count);
    }
}
