package dev.fsg262.client.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

/**
 * Evidence returned by direct vanilla Structure.generate invocation.
 * It contains no inferred blocks, entities, or loot.
 */
public record StructureGenerationInspectionResult(
        long seed,
        String structureKey,
        ChunkPos startChunk,
        BoundingBox structureBounds,
        int pieceCount,
        List<BoundingBox> pieceBounds,
        long generationTimeMillis,
        boolean verified,
        String failureReason
) {
    public StructureGenerationInspectionResult {
        pieceBounds = List.copyOf(pieceBounds);
    }

    public static StructureGenerationInspectionResult unavailable(long seed, String reason) {
        return new StructureGenerationInspectionResult(
                seed, "", null, null, 0, List.of(), 0, false, reason);
    }
}
