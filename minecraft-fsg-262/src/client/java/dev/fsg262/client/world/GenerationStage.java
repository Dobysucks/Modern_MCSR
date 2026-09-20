package dev.fsg262.client.world;

/**
 * Stages exposed by the bounded, in-memory world-generation harness.
 */
public enum GenerationStage {
    BIOMES,
    TERRAIN,
    STRUCTURE_STARTS,
    STRUCTURE_BLOCKS,
    LEVEL_CHUNK,
    BLOCK_ENTITIES
}
