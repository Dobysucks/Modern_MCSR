package dev.fsg262.client.world;

import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.StartEvaluationInput;
import dev.fsg262.filter.StartType;
import dev.fsg262.filter.WorldGenerationAnalyzer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Locale;

/**
 * Read-only seed analysis over the same vanilla registries and dimension
 * generators used by Create New World. Structure placement, biome and terrain
 * queries are real 26.2 queries. Loot and generated block-entity inspection
 * remain unavailable until an in-memory structure-generation level is added.
 */
public final class Minecraft26WorldgenAnalyzer implements WorldGenerationAnalyzer {
    private final WorldCreationContext context;
    private volatile WorldgenAnalysisMetrics lastMetrics = WorldgenAnalysisMetrics.empty();
    private volatile StructurePlacementInspection lastInspection = StructurePlacementInspection.unverified();

    public Minecraft26WorldgenAnalyzer(WorldCreationContext context) {
        this.context = context;
    }

    @Override
    public StartEvaluationInput analyzeOverworld(long seed, StartType startType, FilterProfile profile) {
        long totalStart = System.nanoTime();
        var stem = context.selectedDimensions().get(LevelStem.OVERWORLD)
                .orElseThrow(() -> new IllegalStateException("Overworld dimension is unavailable"));
        ChunkGenerator generator = stem.generator();
        if (!(generator instanceof NoiseBasedChunkGenerator noiseGenerator)) {
            recordMetrics(0, 0, 0);
            return unverified(seed, startType);
        }

        var randomState = RandomState.create(context.worldgenLoadContext(),
                noiseGenerator.generatorSettings().unwrapKey()
                        .orElse(NoiseGeneratorSettings.OVERWORLD), seed);
        var structureSets = context.worldgenLoadContext().lookupOrThrow(Registries.STRUCTURE_SET);
        var structureState = generator.createState(structureSets, randomState, seed);
        long placementStart = System.nanoTime();
        var found = findStructure(structureState, startType, profile);
        long placementMillis = (System.nanoTime() - placementStart) / 1_000_000L;
        if (found == null) {
            lastInspection = StructurePlacementInspection.unverified();
            recordMetrics(placementMillis, 0, System.nanoTime() - totalStart);
            return unverified(seed, startType);
        }

        var type = stem.type().value();
        var height = LevelHeightAccessor.create(type.minY(), type.height());
        long terrainStart = System.nanoTime();
        int surface = generator.getBaseHeight(found.getMiddleBlockX(), found.getMiddleBlockZ(),
                Heightmap.Types.WORLD_SURFACE_WG, height, randomState);
        var biome = generator.getBiomeSource().getNoiseBiome(
                found.getMiddleBlockX() >> 2, surface >> 2,
                found.getMiddleBlockZ() >> 2, randomState.sampler());
        boolean riverNearby = biome.value().getClass() != null
                && biomeMatchesNearby(generator, randomState,
                found.getMiddleBlockX(), surface, found.getMiddleBlockZ(), "river");

        double distance = Math.hypot(found.x(), found.z());
        lastInspection = new StructurePlacementInspection(
                true, found.structureKey(), found.chunk(), null, false,
                "StructureStart/pieces require a WorldGenLevel and template manager");
        recordMetrics(placementMillis, System.nanoTime() - terrainStart,
                System.nanoTime() - totalStart);
        return new StartEvaluationInput(
                seed, startType, distance,
                0, 0, false, riverNearby ? 0 : Double.POSITIVE_INFINITY,
                0, false, 0, false, false, 0, 0,
                false, false, false, false, true
        );
    }

    /**
     * Placement/biome/height generation is real. The complete filter remains
     * NOT_VERIFIED because exact structure loot and block entities are not yet
     * reproducible without a mutable in-memory WorldGenLevel.
     */
    @Override
    public boolean isVerified() {
        return false;
    }

    public WorldgenAnalysisMetrics lastMetrics() {
        return lastMetrics;
    }

    public StructurePlacementInspection lastInspection() {
        return lastInspection;
    }

    private ChunkCandidate findStructure(
            ChunkGeneratorStructureState state,
            StartType type,
            FilterProfile profile
    ) {
        String[] tokens = switch (type) {
            case VILLAGE -> new String[]{"village"};
            case SHIPWRECK -> new String[]{"shipwreck"};
            case DESERT_TEMPLE -> new String[]{"desert", "pyramid"};
            case RUINED_PORTAL -> new String[]{"ruined_portal"};
            case BURIED_TREASURE -> new String[]{"buried_treasure"};
        };
        ChunkPos best = null;
        String bestStructureKey = "";
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Holder<StructureSet> holder : state.possibleStructureSets()) {
            if (holder.unwrapKey().isEmpty()) continue;
            String setName = holder.unwrapKey().orElseThrow().identifier()
                    .getPath().toLowerCase(Locale.ROOT);
            boolean matches = java.util.Arrays.stream(tokens).anyMatch(setName::contains);
            if (!matches) continue;
            StructureSet set = holder.value();
            StructurePlacement placement = set.placement();
            int radius = radiusFor(type, profile);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (!placement.isStructureChunk(state, x, z)) continue;
                    var candidate = new ChunkPos(x, z);
                    double distance = Math.hypot(candidate.x(), candidate.z());
                    if (distance < bestDistance) {
                        best = candidate;
                        bestDistance = distance;
                        bestStructureKey = matchingStructureKey(set, tokens);
                    }
                }
            }
        }
        return best == null ? null : new ChunkCandidate(best, bestStructureKey);
    }

    private int radiusFor(StartType type, FilterProfile profile) {
        return switch (type) {
            case VILLAGE -> profile.villageMaxDistanceChunks();
            case SHIPWRECK -> profile.shipwreckMaxDistanceChunks();
            case DESERT_TEMPLE -> profile.desertTempleMaxDistanceChunks();
            case RUINED_PORTAL -> profile.ruinedPortalMaxDistanceChunks();
            case BURIED_TREASURE -> profile.buriedTreasureMaxDistanceChunks();
        };
    }

    private String matchingStructureKey(StructureSet set, String[] tokens) {
        return set.structures().stream()
                .map(entry -> entry.structure().unwrapKey()
                        .map(key -> key.identifier().toString()).orElse("unknown"))
                .filter(key -> java.util.Arrays.stream(tokens)
                        .anyMatch(token -> key.toLowerCase(Locale.ROOT).contains(token)))
                .findFirst()
                .orElse("unknown");
    }

    private boolean biomeMatchesNearby(
            ChunkGenerator generator,
            RandomState randomState,
            int x,
            int y,
            int z,
            String token
    ) {
        for (int dx = -24; dx <= 24; dx += 8) {
            for (int dz = -24; dz <= 24; dz += 8) {
                var biome = generator.getBiomeSource().getNoiseBiome(
                        (x + dx) >> 2, y >> 2, (z + dz) >> 2, randomState.sampler());
                var key = biome.unwrapKey();
                if (key.isPresent() && key.orElseThrow().identifier().getPath().contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private StartEvaluationInput unverified(long seed, StartType type) {
        return new StartEvaluationInput(seed, type, Double.POSITIVE_INFINITY,
                0, 0, false, Double.POSITIVE_INFINITY, 0, false, 0,
                false, false, 0, 0, false, false, false, false, false);
    }

    private void recordMetrics(long placementMillis, long terrainNanos, long totalNanos) {
        lastMetrics = new WorldgenAnalysisMetrics(
                placementMillis, terrainNanos / 1_000_000L, 0, 0, 0, 0,
                totalNanos / 1_000_000L);
    }

    private record ChunkCandidate(ChunkPos chunk, String structureKey) {
        int x() {
            return chunk.x();
        }

        int z() {
            return chunk.z();
        }

        int getMiddleBlockX() {
            return chunk.getMiddleBlockX();
        }

        int getMiddleBlockZ() {
            return chunk.getMiddleBlockZ();
        }
    }
}
