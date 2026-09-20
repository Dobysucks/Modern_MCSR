package dev.fsg262.client.world;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public record GeneratedBlockSummary(
        int chunkX,
        int chunkZ,
        int scannedBlocks,
        Map<String, Integer> blockCounts,
        List<BlockPos> lavaSources,
        int blockEntityCount,
        boolean verified,
        String failureReason
) {
    public GeneratedBlockSummary {
        blockCounts = Map.copyOf(blockCounts);
        lavaSources = List.copyOf(lavaSources);
    }

    public static GeneratedBlockSummary unavailable(String reason) {
        return new GeneratedBlockSummary(0, 0, 0, Map.of(), List.of(), 0, false, reason);
    }
}
