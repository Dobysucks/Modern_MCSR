package dev.fsg262.mixin;

import dev.fsg262.access.McsrChestLootAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements McsrChestLootAccess {
    @Unique private boolean fsg262$lootInitialized;

    @Shadow
    protected abstract NonNullList<ItemStack> getItems();

    @Override
    public boolean fsg262$lootInitialized() {
        return fsg262$lootInitialized;
    }

    @Override
    public void fsg262$setLootInitialized(boolean initialized) {
        fsg262$lootInitialized = initialized;
    }

    @Override
    public boolean fsg262$rawInventoryIsEmpty() {
        return getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public NonNullList<ItemStack> fsg262$rawItems() {
        return getItems();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void fsg262$load(ValueInput input, CallbackInfo ci) {
        fsg262$lootInitialized = input.getBooleanOr("Fsg262LootInitialized", false);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void fsg262$save(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("Fsg262LootInitialized", fsg262$lootInitialized);
    }
}
