package dev.fsg262.rng;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic indexed bartering sequence.
 *
 * <p>Every 72-barter window assigns exactly three pearl slots and six
 * guaranteed obsidian slots through a deterministic permutation. Remaining
 * slots use a stable independent output stream. The window is the
 * eight-gold-block sequence (8 * 9 = 72 gold ingots).
 */
public final class LegacyPiglinBartering {
    public static final int WINDOW_SIZE = 72;
    public static final int PEARL_TRADES_PER_WINDOW = 3;
    public static final int GUARANTEED_OBSIDIAN_PER_WINDOW = 6;

    private static final String[] OTHER_OUTPUTS = {
            "string", "gravel", "leather", "nether_brick", "blackstone",
            "crying_obsidian", "soul_sand", "fire_charge"
    };

    public BarterResult result(long rngSeed, int barterIndex) {
        if (barterIndex < 0) {
            throw new IllegalArgumentException("barterIndex cannot be negative");
        }
        int window = Math.floorDiv(barterIndex, WINDOW_SIZE);
        int local = Math.floorMod(barterIndex, WINDOW_SIZE);
        int[] permutation = permutation(rngSeed, window);
        String item;
        int quantity;
        if (local == permutation[0] || local == permutation[1] || local == permutation[2]) {
            item = "ender_pearl";
            quantity = 2 + bounded(rngSeed, window, local, 2);
        } else if (local == permutation[3] || local == permutation[4]
                || local == permutation[5] || local == permutation[6]
                || local == permutation[7] || local == permutation[8]) {
            item = "obsidian";
            quantity = 1 + bounded(rngSeed, window, local, 2);
        } else {
            int choice = bounded(rngSeed, window, local, OTHER_OUTPUTS.length);
            item = OTHER_OUTPUTS[choice];
            quantity = 1 + bounded(rngSeed ^ 0xC6BC279692B5CC83L, window, local, 4);
        }
        return new BarterResult(rngSeed, barterIndex, 1, item, quantity);
    }

    public List<BarterResult> sequence(long rngSeed, int count) {
        var results = new ArrayList<BarterResult>(count);
        for (int i = 0; i < count; i++) results.add(result(rngSeed, i));
        return List.copyOf(results);
    }

    public String replay(long rngSeed, int count) {
        var out = new StringBuilder("RNG SEED: ").append(rngSeed).append('\n');
        for (var result : sequence(rngSeed, count)) {
            out.append("Barter #").append(result.barterIndex())
                    .append(" -> ").append(result.outputQuantity()).append(' ')
                    .append(result.outputItem()).append('\n');
        }
        return out.toString();
    }

    public boolean hasWindowGuarantees(long rngSeed, int startIndex) {
        return checkWindow(rngSeed, startIndex).passed();
    }

    public BarterWindowCheck checkWindow(long rngSeed, int startIndex) {
        if (startIndex < 0 || startIndex % WINDOW_SIZE != 0) {
            throw new IllegalArgumentException("window start must be a non-negative multiple of 72");
        }
        int pearls = 0;
        int obsidian = 0;
        for (int i = startIndex; i < startIndex + WINDOW_SIZE; i++) {
            var result = result(rngSeed, i);
            if (result.outputItem().equals("ender_pearl")) pearls++;
            if (result.outputItem().equals("obsidian")) obsidian++;
        }
        return new BarterWindowCheck(startIndex, pearls, obsidian,
                pearls == PEARL_TRADES_PER_WINDOW && obsidian >= GUARANTEED_OBSIDIAN_PER_WINDOW);
    }

    private int[] permutation(long seed, int window) {
        int[] values = new int[WINDOW_SIZE];
        for (int i = 0; i < values.length; i++) values[i] = i;
        for (int i = values.length - 1; i > 0; i--) {
            int j = bounded(seed, window, i, i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return values;
    }

    private int bounded(long seed, int window, int index, int bound) {
        long mixed = ModernRng262.mix64(seed ^ ((long) window << 32) ^ index);
        return (int) Long.remainderUnsigned(mixed, bound);
    }
}