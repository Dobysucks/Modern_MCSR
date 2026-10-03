package dev.fsg262.mixin;

import dev.fsg262.rng.McsrPiglinBarter;
import dev.fsg262.world.McsrWorldDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraft.world.entity.monster.piglin.PiglinAi")
public abstract class PiglinAiMixin {
    @Inject(
            method = "getBarterResponseItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void fsg262$useStandardizedMcsrBarter(
            Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        if (!(piglin.level() instanceof ServerLevel server)) return;
        if (!(server.getServer().getWorldData() instanceof McsrWorldDataAccess worldData)
                || !McsrPiglinBarter.shouldOverride(worldData.fsg262$isMcsrWorld(),
                worldData.fsg262$standardizedRng())) return;

        boolean nether = server.dimension().equals(Level.NETHER);
        int index = worldData.fsg262$nextMcsrBarterIndex();
        long dimensionSeed = McsrPiglinBarter.seedForDimension(nether,
                worldData.fsg262$mcsrOverworldSeed(), worldData.fsg262$mcsrNetherSeed());
        var result = McsrPiglinBarter.result(worldData.fsg262$mcsrOverworldSeed(),
                worldData.fsg262$mcsrNetherSeed(), nether, index);
        var lootParams = new LootParams.Builder(server)
                .withParameter(LootContextParams.THIS_ENTITY, piglin)
                .create(LootContextParamSets.PIGLIN_BARTER);
        var vanillaItems = server.getServer().reloadableRegistries()
                .getLootTable(BuiltInLootTables.PIGLIN_BARTERING)
                .getRandomItems(lootParams, McsrPiglinBarter.vanillaLootSeed(dimensionSeed, index));
        cir.setReturnValue(McsrPiglinBarter.mergeWithModernTrades(result, vanillaItems,
                stack -> stack.is(net.minecraft.world.item.Items.ENDER_PEARL),
                McsrPiglinBarter::toItemStack));
    }
}
