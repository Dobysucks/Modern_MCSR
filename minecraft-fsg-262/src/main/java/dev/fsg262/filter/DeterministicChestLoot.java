package dev.fsg262.filter;

import dev.fsg262.rng.ModernRng262;
import dev.fsg262.rng.StandardizedSpeedrunRng;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared speedrun-oriented chest loot model used by evaluation and real chests. */
public final class DeterministicChestLoot {
    public enum StructureCategory {
        VILLAGE, SHIPWRECK, DESERT_TEMPLE, RUINED_PORTAL, BURIED_TREASURE, BASTION
    }

    public record Entry(String item, int count) {
        public Entry {
            if (item == null || !item.startsWith("minecraft:") || count <= 0 || count > 64) {
                throw new IllegalArgumentException("Invalid chest loot entry");
            }
        }
    }

    public record PlacedEntry(int slot, String item, int count) {
        public PlacedEntry {
            if (slot < 0) throw new IllegalArgumentException("Slot cannot be negative");
            new Entry(item, count);
        }
    }

    private record WeightedItem(String item, int weight, int minimum, int maximum) {}

    private static final Map<StructureCategory, List<WeightedItem>> TABLES = Map.of(
            StructureCategory.VILLAGE, List.of(
                    item("iron_ingot", 8, 2, 7), item("bread", 10, 3, 8),
                    item("iron_nugget", 5, 5, 16), item("obsidian", 6, 2, 7),
                    item("emerald", 3, 1, 4), item("flint_and_steel", 2, 1, 1)),
            StructureCategory.SHIPWRECK, List.of(
                    item("bread", 5, 3, 8), item("cooked_cod", 3, 3, 8),
                    item("iron_ingot", 6, 2, 7), item("iron_nugget", 4, 5, 16),
                    item("tnt", 2, 1, 4), item("emerald", 2, 1, 4),
                    item("compass", 1, 1, 1), item("paper", 2, 3, 8)),
            StructureCategory.DESERT_TEMPLE, List.of(
                    item("iron_ingot", 5, 2, 7), item("gold_ingot", 7, 2, 8),
                    item("emerald", 3, 1, 5), item("bone", 2, 3, 9),
                    item("string", 2, 3, 8), item("diamond", 2, 1, 3),
                    item("golden_apple", 1, 1, 1)),
            StructureCategory.RUINED_PORTAL, List.of(
                    item("obsidian", 8, 2, 8), item("gold_ingot", 6, 2, 8),
                    item("flint_and_steel", 3, 1, 1), item("fire_charge", 3, 1, 3),
                    item("flint", 3, 3, 9), item("iron_ingot", 4, 2, 7),
                    item("golden_apple", 1, 1, 1)),
            StructureCategory.BURIED_TREASURE, List.of(
                    item("iron_ingot", 5, 3, 9), item("gold_ingot", 7, 3, 10),
                    item("emerald", 4, 2, 6), item("cooked_cod", 2, 3, 9),
                    item("tnt", 3, 1, 5), item("diamond", 2, 1, 3)),
            StructureCategory.BASTION, List.of(
                    item("gold_ingot", 8, 4, 12), item("gold_nugget", 5, 6, 18),
                    item("obsidian", 5, 2, 8), item("crying_obsidian", 4, 2, 7),
                    item("iron_ingot", 3, 2, 7), item("string", 2, 3, 9),
                    item("arrow", 2, 6, 18), item("fire_charge", 2, 2, 5)));

    private DeterministicChestLoot() {}

    private static WeightedItem item(String item, int weight, int minimum, int maximum) {
        return new WeightedItem("minecraft:" + item, weight, minimum, maximum);
    }

    public static List<Entry> forChest(long worldSeed, long blockPos,
                                       StructureCategory category) {
        return forChest(worldSeed, blockPos, category, 0L);
    }

    public static List<Entry> forChest(long worldSeed, long blockPos,
                                       StructureCategory category, String lootTableId) {
        return forChest(worldSeed, blockPos, category, stableHash(lootTableId));
    }

    public static List<Entry> forChest(long worldSeed, long blockPos,
                                       StructureCategory category, long lootTableSalt) {
        return generateEntries(worldSeed, blockPos, category, lootTableSalt);
    }

    public static List<PlacedEntry> forChestLayout(long worldSeed, long blockPos,
                                                   StructureCategory category,
                                                   String lootTableId, int inventorySize) {
        if (inventorySize <= 0) throw new IllegalArgumentException("Inventory size must be positive");
        var entries = generateEntries(worldSeed, blockPos, category, stableHash(lootTableId));
        if (entries.size() > inventorySize) {
            throw new IllegalArgumentException("Inventory cannot hold generated chest loot");
        }
        var streamSeed = streamSeed(worldSeed, blockPos, category, stableHash(lootTableId));
        var rng = new StandardizedSpeedrunRng(streamSeed,
                StandardizedSpeedrunRng.Mode.MODERN_262);
        var slots = new int[inventorySize];
        for (var slot = 0; slot < inventorySize; slot++) slots[slot] = slot;
        var placed = new ArrayList<PlacedEntry>(entries.size());
        for (var index = 0; index < entries.size(); index++) {
            var remaining = inventorySize - index;
            var selected = index + (int) Long.remainderUnsigned(
                    rng.nextLong(10_000L + index), remaining);
            var slot = slots[selected];
            slots[selected] = slots[index];
            slots[index] = slot;
            var entry = entries.get(index);
            placed.add(new PlacedEntry(slot, entry.item(), entry.count()));
        }
        return List.copyOf(placed);
    }

    private static List<Entry> generateEntries(long worldSeed, long blockPos,
                                               StructureCategory category, long lootTableSalt) {
        if (category == null) throw new IllegalArgumentException("Structure category is required");
        var streamSeed = streamSeed(worldSeed, blockPos, category, lootTableSalt);
        var rng = new StandardizedSpeedrunRng(streamSeed,
                StandardizedSpeedrunRng.Mode.MODERN_262);
        var table = TABLES.get(category);
        var totalWeight = table.stream().mapToInt(WeightedItem::weight).sum();
        var rollCount = 5 + (int) Long.remainderUnsigned(rng.nextLong(0), 3);
        var contents = new LinkedHashMap<String, Integer>();
        for (var roll = 0; roll < rollCount; roll++) {
            var pick = (int) Long.remainderUnsigned(rng.nextLong(roll * 2L + 1), totalWeight);
            var selected = table.get(weightedIndex(table, pick));
            var count = selected.minimum() + (int) Long.remainderUnsigned(
                    rng.nextLong(roll * 2L + 2), selected.maximum() - selected.minimum() + 1);
            contents.merge(selected.item(), count, Integer::sum);
        }
        var entries = new ArrayList<Entry>(contents.size());
        contents.forEach((item, count) -> entries.add(new Entry(item, Math.min(64, count))));
        return List.copyOf(entries);
    }

    private static long streamSeed(long worldSeed, long blockPos,
                                   StructureCategory category, long lootTableSalt) {
        return ModernRng262.mix64(worldSeed ^ blockPos
                ^ ((long) category.ordinal() * 0x9E3779B97F4A7C15L)
                ^ lootTableSalt);
    }

    private static int weightedIndex(List<WeightedItem> table, int pick) {
        for (var i = 0; i < table.size(); i++) {
            pick -= table.get(i).weight();
            if (pick < 0) return i;
        }
        throw new IllegalStateException("Weighted chest loot selection exceeded table weight");
    }

    private static long stableHash(String text) {
        if (text == null) return 0L;
        var hash = 0xcbf29ce484222325L;
        for (var i = 0; i < text.length(); i++) {
            hash ^= text.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    public static StructureCategory categoryForLootTable(String namespace, String path) {
        if (!"minecraft".equals(namespace) || path == null) return null;
        if (path.startsWith("chests/village/")) return StructureCategory.VILLAGE;
        if (path.equals("chests/shipwreck_supply") || path.equals("chests/shipwreck_map")
                || path.equals("chests/shipwreck_treasure")) return StructureCategory.SHIPWRECK;
        if (path.equals("chests/desert_pyramid")) return StructureCategory.DESERT_TEMPLE;
        if (path.equals("chests/ruined_portal")) return StructureCategory.RUINED_PORTAL;
        if (path.equals("chests/buried_treasure")) return StructureCategory.BURIED_TREASURE;
        if (path.equals("chests/bastion_treasure") || path.equals("chests/bastion_hoglin_stable")
                || path.equals("chests/bastion_bridge") || path.equals("chests/bastion_other")) {
            return StructureCategory.BASTION;
        }
        return null;
    }

    public static List<Entry> forEvaluation(long worldSeed, StructureCategory category) {
        var entries = new ArrayList<Entry>();
        for (long chestIdentity = 0; chestIdentity < 3; chestIdentity++) {
            entries.addAll(forChest(worldSeed, chestIdentity, category));
        }
        return List.copyOf(entries);
    }
}
