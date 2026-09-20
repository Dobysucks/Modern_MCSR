package dev.fsg262.client.world;

import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Adapter for a real, already bootstrapped temporary ServerLevel.
 *
 * <p>Server bootstrap is deliberately kept outside this class: 26.2 requires
 * the vanilla {@code WorldLoader} pack/data reload and a
 * {@code LevelStorageSource.LevelStorageAccess}. Supplying a live level from
 * that bootstrap lets this adapter use the normal {@code ServerChunkCache}
 * pipeline without manufacturing a ServerLevel or bypassing chunk status
 * stages.</p>
 */
public final class ServerLevelChunkHarness {
    private final ServerLevel level;

    public ServerLevelChunkHarness(ServerLevel level) {
        this.level = java.util.Objects.requireNonNull(level, "level");
    }

    public ServerLevel level() {
        return level;
    }

    public ServerChunkGenerationResult generate(ChunkPos position,
                                                BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean()) {
            throw new IllegalStateException("Cancelled before server chunk generation");
        }
        var stages = new EnumMap<GenerationStage, GenerationStageResult>(GenerationStage.class);
        try {
            ChunkResult<net.minecraft.world.level.chunk.ChunkAccess> result =
                    level.getChunkSource().getChunkFuture(
                            position.x(), position.z(), ChunkStatus.FULL, true).join();
            LevelChunk chunk = result.map(value -> value instanceof LevelChunk full
                    ? full : null).orElseThrow(() -> new IllegalStateException(
                    "FULL chunk result was not a LevelChunk: " + result.getError()));
            stages.put(GenerationStage.BIOMES,
                    GenerationStageResult.available("ServerChunkCache FULL pipeline"));
            stages.put(GenerationStage.TERRAIN,
                    GenerationStageResult.available("ServerChunkCache FULL pipeline"));
            stages.put(GenerationStage.STRUCTURE_STARTS,
                    GenerationStageResult.available("ChunkAccess structure starts"));
            stages.put(GenerationStage.STRUCTURE_BLOCKS,
                    GenerationStageResult.available("LevelChunk post-generation state"));
            stages.put(GenerationStage.LEVEL_CHUNK,
                    GenerationStageResult.available("ServerChunkCache FULL pipeline"));
            stages.put(GenerationStage.BLOCK_ENTITIES,
                    GenerationStageResult.available(
                            "LevelChunk block entities: " + chunk.getBlockEntities().size()));
            return new ServerChunkGenerationResult(level, chunk, stages);
        } catch (RuntimeException failure) {
            throw new IllegalStateException("Server chunk generation failed: "
                    + failure.getClass().getSimpleName(), failure);
        }
    }

    public Map<ChunkPos, ServerChunkGenerationResult> generate(
            Iterable<ChunkPos> positions, int maxChunks, BooleanSupplier cancelled) {
        if (maxChunks < 1) throw new IllegalArgumentException("maxChunks must be positive");
        var result = new LinkedHashMap<ChunkPos, ServerChunkGenerationResult>();
        for (var position : positions) {
            if (result.size() >= maxChunks || cancelled.getAsBoolean()) break;
            result.put(position, generate(position, cancelled));
        }
        return java.util.Collections.unmodifiableMap(result);
    }
}
