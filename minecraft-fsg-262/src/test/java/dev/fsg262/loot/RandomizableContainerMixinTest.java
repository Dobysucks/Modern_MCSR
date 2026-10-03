package dev.fsg262.loot;

import dev.fsg262.mixin.RandomizableContainerMixin;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomizableContainerMixinTest {
    @Test
    void singleAndDoubleChestsShareThePreUnpackInterfaceHook() throws NoSuchMethodException {
        var unpack = RandomizableContainer.class.getMethod("unpackLootTable", Player.class);
        assertTrue(RandomizableContainer.class.isInterface());
        assertTrue(unpack.isDefault());
        assertEquals(RandomizableContainer.class,
                ChestBlockEntity.class.getMethod("unpackLootTable", Player.class)
                        .getDeclaringClass());
        assertTrue(RandomizableContainerMixin.class.isInterface());
    }
}
