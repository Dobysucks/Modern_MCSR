package dev.fsg262.mixin;

import dev.fsg262.client.world.WorldCreationController;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Shadow
    private net.minecraft.client.gui.screens.worldselection.WorldCreationUiState uiState;

    @Inject(method = "createNewWorld", at = @At("HEAD"), cancellable = true)
    private void fsg262$requireAcceptedSeed(
            LayeredRegistryAccess<RegistryLayer> registries,
            LevelDataAndDimensions.WorldDataAndGenSettings settings,
            java.util.Optional<GameRules> gameRules,
            CallbackInfoReturnable<Boolean> cir
    ) {
        var controller = WorldCreationController.instance();
        if (controller.settings().enabled()
                && !controller.acceptedSeedMatches(uiState.getSeed())) {
            cir.setReturnValue(false);
        }
    }
}
