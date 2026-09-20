package dev.fsg262.client.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;

/**
 * A fully generated chunk obtained from an actual ServerLevel chunk pipeline.
 */
public record ServerChunkGenerationResult(
        ServerLevel level,
        LevelChunk chunk,
        Map<GenerationStage, GenerationStageResult> stages
) {
    public ServerChunkGenerationResult {
        stages = Map.copyOf(stages);
    }

    public GenerationStageResult stage(GenerationStage stage) {
        return stages.getOrDefault(stage,
                GenerationStageResult.unavailable("Stage was not attempted"));
    }
}
