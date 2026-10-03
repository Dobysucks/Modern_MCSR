package dev.fsg262.rng;

import dev.fsg262.mixin.PiglinAiMixin;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McsrPiglinBarterTest {
    @Test
    void overridesOnlyStandardizedMcsrWorlds() {
        assertTrue(McsrPiglinBarter.shouldOverride(true, true));
        assertFalse(McsrPiglinBarter.shouldOverride(true, false));
        assertFalse(McsrPiglinBarter.shouldOverride(false, true));
        assertFalse(McsrPiglinBarter.shouldOverride(false, false));
    }

    @Test
    void usesTheSeedForThePiglinsDimension() {
        assertEquals(11L, McsrPiglinBarter.seedForDimension(false, 11L, 22L));
        assertEquals(22L, McsrPiglinBarter.seedForDimension(true, 11L, 22L));
        assertNotEquals(McsrPiglinBarter.result(11L, 22L, false, 0),
                McsrPiglinBarter.result(11L, 22L, true, 0));
    }

    @Test
    void actualTradeModelRetainsTheThreePearlAndSixObsidianWindow() {
        var trades = new LegacyPiglinBartering().sequence(928341L, 72);

        assertEquals(3, trades.stream()
                .filter(trade -> trade.outputItem().equals("ender_pearl")).count());
        assertTrue(trades.stream()
                .filter(trade -> trade.outputItem().equals("obsidian")).count() >= 6);
        assertEquals(trades, new LegacyPiglinBartering().sequence(928341L, 72));
    }

    @Test
    void outputItemsAndQuantitiesMapToVanillaTrades() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var pearl = new BarterResult(4L, 0, 1, "ender_pearl", 5);
        var obsidian = new BarterResult(4L, 1, 1, "obsidian", 2);

        assertEquals(Items.ENDER_PEARL, McsrPiglinBarter.itemForOutput(pearl.outputItem()));
        assertEquals(5, pearl.outputQuantity());
        assertEquals(Items.OBSIDIAN, McsrPiglinBarter.itemForOutput(obsidian.outputItem()));
        assertEquals(2, obsidian.outputQuantity());
    }

    @Test
    void modernNonPearlTradeIsPreservedForUnscheduledBarter() {
        var modernTrade = new String("quartz");
        var scheduled = new BarterResult(4L, 0, 1, "nether_brick", 2);

        var result = McsrPiglinBarter.mergeWithModernTrades(scheduled, List.of(modernTrade),
                "ender_pearl"::equals, BarterResult::outputItem);

        assertEquals(1, result.size());
        assertSame(modernTrade, result.get(0));
    }

    @Test
    void scheduledPearlAndObsidianReplaceModernTradeOnlyAtGuaranteedSlots() {
        var modernTrade = "quartz";

        var pearl = McsrPiglinBarter.mergeWithModernTrades(
                new BarterResult(4L, 0, 1, "ender_pearl", 4), List.of(modernTrade),
                "ender_pearl"::equals, BarterResult::outputItem);
        var obsidian = McsrPiglinBarter.mergeWithModernTrades(
                new BarterResult(4L, 1, 1, "obsidian", 2), List.of(modernTrade),
                "ender_pearl"::equals, BarterResult::outputItem);

        assertEquals("ender_pearl", pearl.get(0));
        assertEquals("obsidian", obsidian.get(0));
    }

    @Test
    void unscheduledVanillaPearlIsReplacedToPreserveTheThreePearlWindow() {
        var scheduled = new BarterResult(4L, 2, 1, "nether_brick", 2);

        var result = McsrPiglinBarter.mergeWithModernTrades(
                scheduled, List.of("ender_pearl"), "ender_pearl"::equals, BarterResult::outputItem);

        assertEquals("nether_brick", result.get(0));
    }

    @Test
    void vanillaLootSeedIsStableAndDimensionSpecific() {
        long first = McsrPiglinBarter.vanillaLootSeed(11L, 7);

        assertEquals(first, McsrPiglinBarter.vanillaLootSeed(11L, 7));
        assertNotEquals(first, McsrPiglinBarter.vanillaLootSeed(22L, 7));
        assertNotEquals(first, McsrPiglinBarter.vanillaLootSeed(11L, 8));
    }

    @Test
    void mixinTargetsTheMapped26_2BarterResponseMethod() throws Exception {
        var target = PiglinAi.class.getDeclaredMethod("getBarterResponseItems", Piglin.class);
        assertEquals(List.class, target.getReturnType());

        var injection = Arrays.stream(PiglinAiMixin.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(Inject.class))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
        assertEquals("getBarterResponseItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/List;",
                injection.method()[0]);
    }
}
