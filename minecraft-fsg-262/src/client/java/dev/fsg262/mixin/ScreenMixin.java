package dev.fsg262.mixin;

import dev.fsg262.client.world.WorldCreationController;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    private void fsg262$releaseSeedsWhenCreateWorldIsClosed(CallbackInfo ci) {
        if ((Object) this instanceof CreateWorldScreen) {
            WorldCreationController.instance().creationScreenRemoved();
        }
    }
}
