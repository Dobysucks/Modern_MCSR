package dev.fsg262.mixin;

import dev.fsg262.filter.DeterministicChestLoot;
import dev.fsg262.loot.ChestLootInjectionState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {
    @Unique private boolean fsg262$lootInitialized;

    @Inject(method = "startOpen", at = @At("HEAD"))
    private void fsg262$inject(ContainerUser user, CallbackInfo ci) {
        if (!(user instanceof Player) || fsg262$lootInitialized) return;
        ChestBlockEntity chest = (ChestBlockEntity) (Object) this;
        Level level = chest.getLevel();
        if (!(level instanceof ServerLevel server) || !ChestLootInjectionState.applies(server.getSeed())) return;

        DeterministicChestLoot.StructureCategory category = fsg262$category(chest);
        if (category == null) return;
        for (var entry : DeterministicChestLoot.forChest(server.getSeed(),
                chest.getBlockPos().asLong(), category)) {
            if (entry.count() <= 0) continue;
            ItemStack stack = fsg262$stack(entry.item(), entry.count());
            if (stack.isEmpty()) continue;
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                if (chest.getItem(slot).isEmpty()) {
                    chest.setItem(slot, stack);
                    break;
                }
            }
        }
        // Prevent vanilla lazy loot from materializing after the prototype
        // inventory has been inserted.
        chest.setLootTable(null);
        fsg262$lootInitialized = true;
        chest.setChanged();
    }

    @Unique
    private DeterministicChestLoot.StructureCategory fsg262$category(ChestBlockEntity chest) {
        ResourceKey<?> table = chest.getLootTable();
        if (table == null) return null;
        String path = table.identifier().getPath();
        if (path.contains("bastion")) return DeterministicChestLoot.StructureCategory.BASTION;
        if (path.contains("shipwreck")) return DeterministicChestLoot.StructureCategory.SHIPWRECK;
        if (path.contains("desert_pyramid")) return DeterministicChestLoot.StructureCategory.DESERT_TEMPLE;
        if (path.contains("ruined_portal")) return DeterministicChestLoot.StructureCategory.RUINED_PORTAL;
        if (path.contains("buried_treasure")) return DeterministicChestLoot.StructureCategory.BURIED_TREASURE;
        if (path.contains("village")) return DeterministicChestLoot.StructureCategory.VILLAGE;
        return null;
    }

    @Unique
    private ItemStack fsg262$stack(String item, int count) {
        return switch (item) {
            case "minecraft:iron_ingot" -> new ItemStack(Items.IRON_INGOT, count);
            case "minecraft:bread" -> new ItemStack(Items.BREAD, count);
            case "minecraft:obsidian" -> new ItemStack(Items.OBSIDIAN, count);
            case "minecraft:gold_ingot" -> new ItemStack(Items.GOLD_INGOT, count);
            default -> ItemStack.EMPTY;
        };
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
