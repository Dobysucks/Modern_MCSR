package dev.fsg262.client.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Evidence produced by the vanilla structure-placement stage.
 *
 * <p>This is intentionally distinct from generated-piece evidence. A
 * placement hit cannot be promoted to a generated structure or loot result.
 */
public record StructurePlacementInspection(
        boolean placementVerified,
        String structureKey,
        ChunkPos chunk,
        BoundingBox placementBounds,
        boolean generatedPiecesVerified,
        String limitation
) {
    public static StructurePlacementInspection unverified() {
        return new StructurePlacementInspection(
                false, "", null, null, false, "No matching vanilla placement was found");
    }
}
