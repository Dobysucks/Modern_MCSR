package dev.fsg262.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelMixinTest {
    @Test
    void injectsAtTheMapped26_2ServerLevelInsertionMethod() throws NoSuchMethodException {
        var method = ServerLevel.class.getMethod("addFreshEntity", Entity.class);
        assertEquals(boolean.class, method.getReturnType());
        assertThrows(NoSuchMethodException.class,
                () -> Level.class.getDeclaredMethod("addFreshEntity", Entity.class));
    }

    @Test
    void blocksOnlyWhenMcsrWorldAndBruteDisablingAreEnabled() {
        assertTrue(McsrPiglinBrutePolicy.shouldBlock(true, true));
        assertFalse(McsrPiglinBrutePolicy.shouldBlock(true, false));
        assertFalse(McsrPiglinBrutePolicy.shouldBlock(false, true));
        assertFalse(McsrPiglinBrutePolicy.shouldBlock(false, false));
    }
}
