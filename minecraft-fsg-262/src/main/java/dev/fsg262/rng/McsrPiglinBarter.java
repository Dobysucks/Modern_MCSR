package dev.fsg262.rng;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class McsrPiglinBarter {
    private static final LegacyPiglinBartering BARTERING = new LegacyPiglinBartering();

    private McsrPiglinBarter() {}

    public static boolean shouldOverride(boolean mcsrWorld, boolean standardizedRng) {
        return mcsrWorld && standardizedRng;
    }

    public static long seedForDimension(boolean nether, long overworldSeed, long netherSeed) {
        return nether ? netherSeed : overworldSeed;
    }

    public static BarterResult result(long overworldSeed, long netherSeed,
                                      boolean nether, int barterIndex) {
        return BARTERING.result(seedForDimension(nether, overworldSeed, netherSeed), barterIndex);
    }

    public static long vanillaLootSeed(long dimensionSeed, int barterIndex) {
        return new ModernRng262(dimensionSeed).nextLong(barterIndex);
    }

    public static <T> List<T> mergeWithModernTrades(
            BarterResult scheduled,
            List<T> vanillaItems,
            Predicate<T> isPearl,
            Function<BarterResult, T> scheduledOutputFactory
    ) {
        if (scheduled.outputItem().equals("ender_pearl")
                || scheduled.outputItem().equals("obsidian")
                || vanillaItems.isEmpty()) {
            return List.of(scheduledOutputFactory.apply(scheduled));
        }

        if (vanillaItems.stream().noneMatch(isPearl)) {
            return List.copyOf(vanillaItems);
        }

        var merged = new ArrayList<T>(vanillaItems.size());
        boolean addedFallback = false;
        for (var item : vanillaItems) {
            if (isPearl.test(item)) {
                if (!addedFallback) {
                    merged.add(scheduledOutputFactory.apply(scheduled));
                    addedFallback = true;
                }
            } else {
                merged.add(item);
            }
        }
        return List.copyOf(merged);
    }

    public static ItemStack toItemStack(BarterResult result) {
        return new ItemStack(itemForOutput(result.outputItem()), result.outputQuantity());
    }

    public static Item itemForOutput(String name) {
        return switch (name) {
            case "ender_pearl" -> Items.ENDER_PEARL;
            case "obsidian" -> Items.OBSIDIAN;
            case "string" -> Items.STRING;
            case "gravel" -> Items.GRAVEL;
            case "leather" -> Items.LEATHER;
            case "nether_brick" -> Items.NETHER_BRICK;
            case "blackstone" -> Items.BLACKSTONE;
            case "crying_obsidian" -> Items.CRYING_OBSIDIAN;
            case "soul_sand" -> Items.SOUL_SAND;
            case "fire_charge" -> Items.FIRE_CHARGE;
            default -> throw new IllegalArgumentException("Unsupported barter output: " + name);
        };
    }
}
