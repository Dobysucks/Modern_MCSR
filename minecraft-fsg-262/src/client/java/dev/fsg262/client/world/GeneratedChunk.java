package dev.fsg262.client.world;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Map;
import java.util.Optional;

/**
 * The real vanilla chunk produced by {@link RealChunkGenerationHarness}.
 *
 * <p>A {@link net.minecraft.world.level.chunk.ProtoChunk} is deliberately
 * retained. Converting it to a LevelChunk requires a live ServerLevel and
 * therefore would not be an honest in-memory result.</p>
 */
public record GeneratedChunk(
        ChunkAccess chunk,
        Optional<LevelChunk> levelChunk,
        Map<Structure, StructureStart> structureStarts,
        Map<GenerationStage, GenerationStageResult> stages
) {
    public GeneratedChunk {
        levelChunk = levelChunk == null ? Optional.empty() : levelChunk;
        structureStarts = Map.copyOf(structureStarts);
        stages = Map.copyOf(stages);
    }

    public GenerationStageResult stage(GenerationStage stage) {
        return stages.getOrDefault(stage,
                GenerationStageResult.unavailable("Stage was not attempted"));
    }
}
