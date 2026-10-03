package dev.fsg262.mixin;

import dev.fsg262.world.McsrWorldDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class LevelMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void fsg262$disablePiglinBrutes(Entity entity,
                                             CallbackInfoReturnable<Boolean> cir) {
        var level = (ServerLevel) (Object) this;
        var worldData = (McsrWorldDataAccess) level.getServer().getWorldData();
        if (entity instanceof PiglinBrute
                && McsrPiglinBrutePolicy.shouldBlock(worldData.fsg262$isMcsrWorld(),
                worldData.fsg262$disablePiglinBrutes())) {
            cir.setReturnValue(false);
        }
    }
}
