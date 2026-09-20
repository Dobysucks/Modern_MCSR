package dev.fsg262.coords;

/** Unit-explicit coordinate helpers. All distance results are doubles. */
public final class CoordinateUtil {
    public static final int BLOCKS_PER_CHUNK = 16;
    public static final int CHUNKS_PER_REGION = 32;

    private CoordinateUtil() {}

    public static double blocksToChunks(double blocks) {
        return blocks / BLOCKS_PER_CHUNK;
    }

    public static double chunksToBlocks(double chunks) {
        return chunks * BLOCKS_PER_CHUNK;
    }

    public static double chunkDistance(ChunkCoordinate a, ChunkCoordinate b) {
        return Math.hypot((double) a.x() - b.x(), (double) a.z() - b.z());
    }

    public static double blockDistance(BlockCoordinate a, BlockCoordinate b) {
        return Math.hypot((double) a.x() - b.x(), (double) a.z() - b.z());
    }

    public static double regionDistance(ChunkCoordinate a, ChunkCoordinate b) {
        int ax = Math.floorDiv(a.x(), CHUNKS_PER_REGION);
        int az = Math.floorDiv(a.z(), CHUNKS_PER_REGION);
        int bx = Math.floorDiv(b.x(), CHUNKS_PER_REGION);
        int bz = Math.floorDiv(b.z(), CHUNKS_PER_REGION);
        return Math.hypot((double) ax - bx, (double) az - bz);
    }

    public static ChunkCoordinate blocksToChunk(BlockCoordinate block) {
        return new ChunkCoordinate(
                Math.toIntExact(Math.floorDiv(block.x(), BLOCKS_PER_CHUNK)),
                Math.toIntExact(Math.floorDiv(block.z(), BLOCKS_PER_CHUNK)));
    }
}