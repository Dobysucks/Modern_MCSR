package dev.fsg262.access;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface McsrChestLootAccess {
    boolean fsg262$lootInitialized();

    void fsg262$setLootInitialized(boolean initialized);

    boolean fsg262$rawInventoryIsEmpty();

    NonNullList<ItemStack> fsg262$rawItems();
}
