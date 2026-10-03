package dev.fsg262.loot;

import dev.fsg262.filter.DeterministicChestLoot;
import dev.fsg262.access.McsrChestLootAccess;
import dev.fsg262.world.McsrWorldDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class McsrChestLootPopulator {
    private McsrChestLootPopulator() {}

    public static void populate(RandomizableContainer container) {
        if (!(container instanceof ChestBlockEntity chest)) return;
        Level level = chest.getLevel();
        if (!(level instanceof ServerLevel server)) return;
        if (!(server.getServer().getWorldData() instanceof McsrWorldDataAccess worldData)
                || !worldData.fsg262$isMcsrWorld()) return;

        var lootTable = chest.getLootTable();
        var category = lootTable == null ? null
                : DeterministicChestLoot.categoryForLootTable(
                        lootTable.identifier().getNamespace(), lootTable.identifier().getPath());
        var access = (McsrChestLootAccess) chest;
        if (!McsrChestLootPolicy.shouldPopulate(worldData.fsg262$isMcsrWorld(),
                access.fsg262$lootInitialized(), access.fsg262$rawInventoryIsEmpty(),
                lootTable != null, category)) return;

        var dimensionSeed = server.dimension().equals(Level.NETHER)
                ? worldData.fsg262$mcsrNetherSeed()
                : worldData.fsg262$mcsrOverworldSeed();
        var rngSeed = worldData.fsg262$standardizedRng()
                ? dimensionSeed : server.getRandom().nextLong();
        var entries = DeterministicChestLoot.forChestLayout(rngSeed,
                chest.getBlockPos().asLong(), category,
                lootTable.identifier().toString(), chest.getContainerSize());
        var rawItems = access.fsg262$rawItems();
        if (!writeInventory(rawItems, entries,
                entry -> stack(entry.item(), entry.count()), ItemStack::isEmpty)) return;

        chest.setLootTable(null);
        access.fsg262$setLootInitialized(true);
        chest.setChanged();
    }

    static <T> boolean writeInventory(List<T> rawItems,
                                      List<DeterministicChestLoot.PlacedEntry> entries,
                                      Function<DeterministicChestLoot.PlacedEntry, T> stackFactory,
                                      Predicate<T> isEmpty) {
        if (rawItems.isEmpty() || entries.isEmpty()
                || rawItems.stream().anyMatch(item -> !isEmpty.test(item))) return false;
        var writes = new ArrayList<InventoryWrite<T>>(entries.size());
        var occupied = new java.util.BitSet(rawItems.size());
        for (var entry : entries) {
            if (entry.slot() >= rawItems.size() || occupied.get(entry.slot())) return false;
            var stack = stackFactory.apply(entry);
            if (stack == null || isEmpty.test(stack)) return false;
            occupied.set(entry.slot());
            writes.add(new InventoryWrite<>(entry.slot(), stack));
        }
        if (writes.isEmpty()) return false;
        for (var write : writes) rawItems.set(write.slot(), write.stack());
        return rawItems.stream().anyMatch(item -> !isEmpty.test(item));
    }

    private record InventoryWrite<T>(int slot, T stack) {}

    private static ItemStack stack(String item, int count) {
        return switch (item) {
            case "minecraft:iron_ingot" -> new ItemStack(Items.IRON_INGOT, count);
            case "minecraft:iron_nugget" -> new ItemStack(Items.IRON_NUGGET, count);
            case "minecraft:bread" -> new ItemStack(Items.BREAD, count);
            case "minecraft:obsidian" -> new ItemStack(Items.OBSIDIAN, count);
            case "minecraft:emerald" -> new ItemStack(Items.EMERALD, count);
            case "minecraft:flint_and_steel" -> new ItemStack(Items.FLINT_AND_STEEL, count);
            case "minecraft:cooked_cod" -> new ItemStack(Items.COOKED_COD, count);
            case "minecraft:tnt" -> new ItemStack(Items.TNT, count);
            case "minecraft:compass" -> new ItemStack(Items.COMPASS, count);
            case "minecraft:paper" -> new ItemStack(Items.PAPER, count);
            case "minecraft:gold_ingot" -> new ItemStack(Items.GOLD_INGOT, count);
            case "minecraft:bone" -> new ItemStack(Items.BONE, count);
            case "minecraft:string" -> new ItemStack(Items.STRING, count);
            case "minecraft:diamond" -> new ItemStack(Items.DIAMOND, count);
            case "minecraft:golden_apple" -> new ItemStack(Items.GOLDEN_APPLE, count);
            case "minecraft:fire_charge" -> new ItemStack(Items.FIRE_CHARGE, count);
            case "minecraft:flint" -> new ItemStack(Items.FLINT, count);
            case "minecraft:gold_nugget" -> new ItemStack(Items.GOLD_NUGGET, count);
            case "minecraft:crying_obsidian" -> new ItemStack(Items.CRYING_OBSIDIAN, count);
            case "minecraft:arrow" -> new ItemStack(Items.ARROW, count);
            default -> ItemStack.EMPTY;
        };
    }
}
