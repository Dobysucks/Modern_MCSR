package dev.fsg262.coords;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateUtilTest {
    @Test
    void convertsBetweenBlocksAndChunks() {
        assertEquals(2.0, CoordinateUtil.blocksToChunks(32));
        assertEquals(32.0, CoordinateUtil.chunksToBlocks(2));
    }

    @Test
    void calculatesChunkAndBlockDistance() {
        assertEquals(5.0, CoordinateUtil.chunkDistance(
                new ChunkCoordinate(0, 0), new ChunkCoordinate(3, 4)));
        assertEquals(50.0, CoordinateUtil.blockDistance(
                new BlockCoordinate(0, 0), new BlockCoordinate(30, 40)));
    }

    @Test
    void calculatesRegionsUsingFloorDivisionForNegativeCoordinates() {
        assertEquals(Math.sqrt(8), CoordinateUtil.regionDistance(
                new ChunkCoordinate(-1, -1), new ChunkCoordinate(32, 32)));
    }

    @Test
    void convertsNegativeBlocksToTheContainingChunk() {
        assertEquals(new ChunkCoordinate(-1, -1),
                CoordinateUtil.blocksToChunk(new BlockCoordinate(-1, -1)));
    }
}