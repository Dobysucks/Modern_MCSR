package dev.fsg262.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McsrBarterCounterTest {
    @Test
    void restoredCounterContinuesTheSavedSequence() {
        var counter = new McsrBarterCounter();
        assertEquals(0, counter.next());
        assertEquals(1, counter.next());

        var restored = new McsrBarterCounter();
        restored.restore(counter.current());

        assertEquals(2, restored.next());
        assertEquals(3, restored.current());
    }

    @Test
    void invalidPersistedIndexIsClampedToZero() {
        var counter = new McsrBarterCounter();
        counter.restore(-9);

        assertEquals(0, counter.next());
    }
}
