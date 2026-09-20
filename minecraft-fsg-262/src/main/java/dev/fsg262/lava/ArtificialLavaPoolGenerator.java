package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;
import dev.fsg262.filter.StartType;
import dev.fsg262.rng.ModernRng262;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic fallback planner. The Minecraft adapter supplies candidates
 * from real 26.2 terrain; this class never invents terrain facts.
 */
public final class ArtificialLavaPoolGenerator {
    public record CandidatePosition(
            BlockCoordinate center,
            int width,
            int length,
            int depth,
            boolean surface,
            boolean safeTerrain,
            boolean reachable,
            boolean insideImportantStructure,
            boolean portalBuildable
    ) {}

    public record GeneratedPool(
            BlockCoordinate center,
            int width,
            int length,
            int depth,
            List<BlockCoordinate> sourceBlocks
    ) {}

    public record PoolGenerationResult(
            boolean verified,
            List<GeneratedPool> pools,
            String reason
    ) {
        public PoolGenerationResult {
            pools = List.copyOf(pools);
        }
    }

    public List<CandidatePosition> findCandidatePositions(
            List<CandidatePosition> terrainCandidates,
            BlockCoordinate origin,
            LavaSearchConfig config
    ) {
        return terrainCandidates.stream()
                .filter(candidate -> distance(origin, candidate.center()) <= config.maximumDistanceBlocks())
                .filter(this::validateTerrain)
                .toList();
    }

    public boolean validateTerrain(CandidatePosition candidate) {
        return candidate.surface()
                && candidate.safeTerrain()
                && candidate.reachable()
                && !candidate.insideImportantStructure()
                && candidate.portalBuildable()
                && candidate.width() >= 2
                && candidate.length() >= 2
                && candidate.depth() >= 1;
    }

    public List<CandidatePosition> selectThreePositions(
            long worldSeed,
            long rngSeed,
            StartType startType,
            List<CandidatePosition> candidates,
            int requestedCount
    ) {
        if (requestedCount != 3) {
            throw new IllegalArgumentException("The fallback must generate exactly 3 pools");
        }
        var ordered = new ArrayList<>(candidates);
        long salt = worldSeed ^ rngSeed ^ ((long) startType.ordinal() << 48);
        ordered.sort(Comparator.comparingLong(candidate -> score(salt, candidate.center())));
        var selected = new ArrayList<CandidatePosition>(3);
        for (var candidate : ordered) {
            if (selected.stream().noneMatch(existing ->
                    distance(existing.center(), candidate.center()) < 4)) {
                selected.add(candidate);
            }
            if (selected.size() == requestedCount) break;
        }
        return List.copyOf(selected);
    }

    public GeneratedPool generatePool(CandidatePosition candidate) {
        var sourceBlocks = new ArrayList<BlockCoordinate>();
        long minX = candidate.center().x() - candidate.width() / 2L;
        long minZ = candidate.center().z() - candidate.length() / 2L;
        for (int x = 0; x < candidate.width(); x++) {
            for (int z = 0; z < candidate.length(); z++) {
                sourceBlocks.add(new BlockCoordinate(minX + x, minZ + z));
            }
        }
        return new GeneratedPool(candidate.center(), candidate.width(), candidate.length(),
                candidate.depth(), sourceBlocks);
    }

    public boolean verifyPool(GeneratedPool pool, BlockCoordinate origin, LavaSearchConfig config) {
        return pool.sourceBlocks().size() == pool.width() * pool.length()
                && distance(pool.center(), origin) <= config.maximumDistanceBlocks()
                && pool.width() >= 2
                && pool.length() >= 2
                && pool.depth() >= 1;
    }

    public PoolGenerationResult createFallback(
            long worldSeed,
            long rngSeed,
            StartType startType,
            BlockCoordinate origin,
            List<CandidatePosition> terrainCandidates,
            LavaSearchConfig config
    ) {
        var candidates = findCandidatePositions(terrainCandidates, origin, config);
        var selected = selectThreePositions(worldSeed, rngSeed, startType, candidates,
                config.artificialPoolCount());
        if (selected.size() != config.artificialPoolCount()) {
            return new PoolGenerationResult(false, List.of(),
                    "NOT VERIFIED: fewer than exactly 3 valid terrain candidates");
        }
        var pools = selected.stream().map(this::generatePool).toList();
        boolean verified = pools.size() == 3
                && pools.stream().allMatch(pool -> verifyPool(pool, origin, config));
        return new PoolGenerationResult(verified, verified ? pools : List.of(),
                verified ? "Exactly 3 deterministic fallback pools planned"
                        : "NOT VERIFIED: generated pool validation failed");
    }

    private long score(long seed, BlockCoordinate coordinate) {
        return ModernRng262.mix64(seed ^ coordinate.x() * 0x9E3779B97F4A7C15L
                ^ coordinate.z() * 0xBF58476D1CE4E5B9L);
    }

    private double distance(BlockCoordinate a, BlockCoordinate b) {
        return Math.hypot((double) a.x() - b.x(), (double) a.z() - b.z());
    }
}