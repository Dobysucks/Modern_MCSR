package dev.fsg262.mixin;

import dev.fsg262.loot.McsrChestLootPopulator;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {
    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    default void fsg262$populateMcsrChestBeforeVanillaLoot(
            Player player, CallbackInfo ci
    ) {
        McsrChestLootPopulator.populate((RandomizableContainer) this);
    }
}
