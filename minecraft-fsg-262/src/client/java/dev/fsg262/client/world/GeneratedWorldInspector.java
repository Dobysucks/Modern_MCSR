package dev.fsg262.client.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;

public final class GeneratedWorldInspector {
    private GeneratedWorldInspector() {}

    public static GeneratedBlockSummary inspect(ChunkAccess chunk, int minY, int maxY) {
        var counts = new HashMap<String, Integer>();
        var lava = new ArrayList<BlockPos>();
        int scanned = 0;
        for (int y = minY; y < maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    var position = new BlockPos(chunk.getPos().getMinBlockX() + x, y,
                            chunk.getPos().getMinBlockZ() + z);
                    var state = chunk.getBlockState(position);
                    counts.merge(state.getBlock().builtInRegistryHolder().key().identifier().toString(),
                            1, Integer::sum);
                    if (state.is(Blocks.LAVA) && chunk.getFluidState(position).isSource()) {
                        lava.add(position.immutable());
                    }
                    scanned++;
                }
            }
        }
        int blockEntities = chunk instanceof net.minecraft.world.level.chunk.ProtoChunk proto
                ? proto.getBlockEntities().size() : 0;
        return new GeneratedBlockSummary(chunk.getPos().x(), chunk.getPos().z(), scanned,
                counts, lava, blockEntities, true, "");
    }
}
