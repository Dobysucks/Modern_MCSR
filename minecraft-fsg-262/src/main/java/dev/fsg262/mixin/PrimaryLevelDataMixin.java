package dev.fsg262.mixin;

import com.mojang.serialization.Dynamic;
import dev.fsg262.world.McsrWorldDataAccess;
import dev.fsg262.world.McsrBarterCounter;
import dev.fsg262.world.McsrWorldSeedState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.PrimaryLevelData.SpecialWorldProperty;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements McsrWorldDataAccess {
    @Unique private long fsg262$mcsrOverworldSeed;
    @Unique private long fsg262$mcsrNetherSeed;
    @Unique private boolean fsg262$disablePiglinBrutes;
    @Unique private boolean fsg262$standardizedRng;
    @Unique private boolean fsg262$isMcsrWorld;
    @Unique private McsrBarterCounter fsg262$mcsrBarterCounter = new McsrBarterCounter();

    @Inject(method = "<init>(Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/storage/PrimaryLevelData$SpecialWorldProperty;Lcom/mojang/serialization/Lifecycle;)V",
            at = @At("TAIL"))
    private void fsg262$takePendingMcsrSeeds(LevelSettings settings,
                                             SpecialWorldProperty worldProperty,
                                             Lifecycle lifecycle, CallbackInfo ci) {
        var seeds = McsrWorldSeedState.consumePending();
        if (seeds != null) {
            fsg262$setMcsrWorldSeeds(seeds.mcsr_overworld_seed(),
                    seeds.mcsr_nether_seed(), seeds.disablePiglinBrutes(),
                    seeds.standardizedRng());
        }
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static <T> void fsg262$readMcsrSeeds(
            Dynamic<T> dynamic,
            LevelSettings settings,
            SpecialWorldProperty worldProperty,
            Lifecycle lifecycle,
            CallbackInfoReturnable<PrimaryLevelData> cir
    ) {
        ((McsrWorldDataAccess) cir.getReturnValue()).fsg262$setMcsrWorldSeeds(
                dynamic.get("mcsr_overworld_seed").asLong(0L),
                dynamic.get("mcsr_nether_seed").asLong(0L),
                dynamic.get("mcsr_disable_piglin_brutes").asBoolean(false),
                dynamic.get("mcsr_standardized_rng").asBoolean(false));
        ((McsrWorldDataAccess) cir.getReturnValue()).fsg262$setMcsrWorldCreated(
                dynamic.get("mcsr_seed_database_world").asBoolean(false));
        ((McsrWorldDataAccess) cir.getReturnValue()).fsg262$setMcsrBarterIndex(
                dynamic.get("mcsr_barter_index").asInt(0));
    }

    @Inject(method = "createTag", at = @At("RETURN"))
    private void fsg262$writeMcsrSeeds(UUID playerId,
                                       CallbackInfoReturnable<CompoundTag> cir) {
        var tag = cir.getReturnValue();
        tag.putLong("mcsr_overworld_seed", fsg262$mcsrOverworldSeed);
        tag.putLong("mcsr_nether_seed", fsg262$mcsrNetherSeed);
        tag.putBoolean("mcsr_disable_piglin_brutes", fsg262$disablePiglinBrutes);
        tag.putBoolean("mcsr_standardized_rng", fsg262$standardizedRng);
        tag.putBoolean("mcsr_seed_database_world", fsg262$isMcsrWorld);
        tag.putInt("mcsr_barter_index", fsg262$mcsrBarterCounter.current());
    }

    @Override
    public boolean fsg262$isMcsrWorld() {
        return fsg262$isMcsrWorld;
    }

    @Override
    public void fsg262$setMcsrWorldCreated(boolean isMcsrWorld) {
        fsg262$isMcsrWorld = isMcsrWorld;
    }

    @Override
    public long fsg262$mcsrOverworldSeed() {
        return fsg262$mcsrOverworldSeed;
    }

    @Override
    public long fsg262$mcsrNetherSeed() {
        return fsg262$mcsrNetherSeed;
    }

    @Override
    public boolean fsg262$disablePiglinBrutes() {
        return fsg262$disablePiglinBrutes;
    }

    @Override
    public boolean fsg262$standardizedRng() {
        return fsg262$standardizedRng;
    }

    @Override
    public void fsg262$setMcsrBarterIndex(int index) {
        fsg262$mcsrBarterCounter.restore(index);
    }

    @Override
    public synchronized int fsg262$nextMcsrBarterIndex() {
        return fsg262$mcsrBarterCounter.next();
    }

    @Override
    public void fsg262$setMcsrWorldSeeds(long overworldSeed, long netherSeed,
                                         boolean disablePiglinBrutes, boolean standardizedRng) {
        fsg262$mcsrOverworldSeed = overworldSeed;
        fsg262$mcsrNetherSeed = netherSeed;
        fsg262$disablePiglinBrutes = disablePiglinBrutes;
        fsg262$standardizedRng = standardizedRng;
        fsg262$isMcsrWorld = true;
    }
}
