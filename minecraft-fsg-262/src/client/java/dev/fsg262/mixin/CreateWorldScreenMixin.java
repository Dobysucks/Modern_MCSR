package dev.fsg262.mixin;

import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.search.AcceptedSeedPair;
import dev.fsg262.world.McsrWorldDataAccess;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelDataAndDimensions.WorldDataAndGenSettings;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Inject(method = "createNewWorld", at = @At("HEAD"), cancellable = true)
    private void fsg262$requireAcceptedSeed(
            LayeredRegistryAccess<RegistryLayer> registries,
            LevelDataAndDimensions.WorldDataAndGenSettings settings,
            java.util.Optional<GameRules> gameRules,
            CallbackInfoReturnable<Boolean> cir
    ) {
        var controller = WorldCreationController.instance();
        if (!controller.prepareWorldCreation((CreateWorldScreen) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "createNewWorld", at = @At("HEAD"),
            argsOnly = true, ordinal = 0)
    private WorldDataAndGenSettings fsg262$applyMcsrOverworldSeed(
            WorldDataAndGenSettings original
    ) {
        var controller = WorldCreationController.instance();
        if (!controller.settings().enabled()
                || !controller.prepareWorldCreation((CreateWorldScreen) (Object) this)) {
            return original;
        }
        AcceptedSeedPair accepted = controller.acceptedSeeds();
        ((McsrWorldDataAccess) original.data()).fsg262$setMcsrWorldSeeds(
                accepted.mcsr_overworld_seed(), accepted.mcsr_nether_seed(),
                controller.settings().disablePiglinBrutes(),
                controller.settings().standardizedRng());
        var oldSettings = original.genSettings();
        var options = oldSettings.options().withSeed(
                OptionalLong.of(accepted.mcsr_overworld_seed()));
        return new WorldDataAndGenSettings(original.data(),
                new WorldGenSettings(options, oldSettings.dimensions()));
    }

    @Inject(method = "createNewWorld", at = @At("RETURN"))
    private void fsg262$releaseMcsrReservationOnFailure(
            LayeredRegistryAccess<RegistryLayer> registries,
            LevelDataAndDimensions.WorldDataAndGenSettings settings,
            java.util.Optional<GameRules> gameRules,
            CallbackInfoReturnable<Boolean> cir
    ) {
        var controller = WorldCreationController.instance();
        if (!cir.getReturnValue() && controller.settings().enabled()) {
            controller.worldCreationFailed();
        }
    }
}
