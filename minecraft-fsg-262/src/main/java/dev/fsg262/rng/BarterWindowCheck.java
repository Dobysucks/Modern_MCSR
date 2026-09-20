package dev.fsg262.rng;

/** Auditable result for one complete standardized barter window. */
public record BarterWindowCheck(int startIndex, int pearlTrades, int obsidianTrades,
                                boolean passed) {}
