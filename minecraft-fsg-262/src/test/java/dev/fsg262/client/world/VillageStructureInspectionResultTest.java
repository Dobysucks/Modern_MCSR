package dev.fsg262.client.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VillageStructureInspectionResultTest {
    @Test
    void preservesDeterministicStructureEvidence() {
        var bounds = new BoundingBox(0, 64, 0, 15, 80, 15);
        var result = new VillageStructureInspectionResult(
                1234L, new ChunkPos(2, -3), bounds, 2,
                List.of(bounds, new BoundingBox(16, 64, 0, 31, 82, 15)),
                17, true, "");

        assertEquals(1234L, result.seed());
        assertEquals(new ChunkPos(2, -3), result.startChunk());
        assertEquals(2, result.pieceCount());
        assertEquals(bounds, result.structureBounds());
        assertEquals(2, result.pieceBounds().size());
        assertTrue(result.verified());
        assertThrows(UnsupportedOperationException.class,
                () -> result.pieceBounds().add(bounds));
    }

    @Test
    void unavailableEvidenceCannotBeVerified() {
        var result = VillageStructureInspectionResult.unavailable(42L, "cancelled");

        assertFalse(result.verified());
        assertEquals(0, result.pieceCount());
        assertEquals("cancelled", result.failureReason());
        assertTrue(result.pieceBounds().isEmpty());
    }
}
