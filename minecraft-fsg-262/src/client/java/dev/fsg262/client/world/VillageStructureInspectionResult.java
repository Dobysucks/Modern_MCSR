package dev.fsg262.client.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

/**
 * Real structure-generation evidence for the Village phase.
 */
public record VillageStructureInspectionResult(
        long seed,
        ChunkPos startChunk,
        BoundingBox structureBounds,
        int pieceCount,
        List<BoundingBox> pieceBounds,
        long generationTimeMillis,
        boolean verified,
        String failureReason
) {
    public VillageStructureInspectionResult {
        pieceBounds = List.copyOf(pieceBounds);
    }

    public static VillageStructureInspectionResult unavailable(long seed, String reason) {
        return new VillageStructureInspectionResult(
                seed, null, null, 0, List.of(), 0, false, reason);
    }
}
