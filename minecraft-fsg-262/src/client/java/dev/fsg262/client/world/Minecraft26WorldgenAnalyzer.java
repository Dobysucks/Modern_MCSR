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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Locale;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Read-only seed analysis over the same vanilla registries and dimension
 * generators used by Create New World. Structure placement, biome and terrain
 * queries are real 26.2 queries. Loot and generated block-entity inspection
 * remain unavailable until an in-memory structure-generation level is added.
 */
public final class Minecraft26WorldgenAnalyzer implements WorldGenerationAnalyzer {
    private final WorldCreationContext context;
    private final StructureTemplateManager templateManager;
    private volatile WorldgenAnalysisMetrics lastMetrics = WorldgenAnalysisMetrics.empty();
    private volatile StructurePlacementInspection lastInspection = StructurePlacementInspection.unverified();
    private volatile VillageStructureInspectionResult lastVillageInspection =
            VillageStructureInspectionResult.unavailable(0, "Not inspected");
    private volatile StructureGenerationInspectionResult lastGeneratedInspection =
            StructureGenerationInspectionResult.unavailable(0, "Not inspected");

    public Minecraft26WorldgenAnalyzer(WorldCreationContext context) {
        this(context, null);
    }

    public Minecraft26WorldgenAnalyzer(WorldCreationContext context,
                                       StructureTemplateManager templateManager) {
        this.context = context;
        this.templateManager = templateManager;
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

        if (startType == StartType.VILLAGE) {
            inspectVillage(seed, generator, randomState, found, stem, () -> false);
        } else {
            inspectStructure(seed, startType, profile, () -> false);
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
                true, found.structureKey(), found.chunk(),
                lastGeneratedInspection.structureBounds(),
                lastGeneratedInspection.verified(),
                lastGeneratedInspection.verified()
                        ? "" : lastGeneratedInspection.failureReason(),
                lastGeneratedInspection.pieceCount(), lastGeneratedInspection.pieceBounds(),
                lastGeneratedInspection.generationTimeMillis());
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

    public VillageStructureInspectionResult lastVillageInspection() {
        return lastVillageInspection;
    }

    public StructureGenerationInspectionResult lastGeneratedInspection() {
        return lastGeneratedInspection;
    }

    /**
     * Generates the selected Overworld structure through Minecraft's actual
     * Structure.generate implementation. This is geometry-only evidence.
     */
    public StructureGenerationInspectionResult inspectStructure(
            long seed,
            StartType startType,
            FilterProfile profile,
            BooleanSupplier cancelled
    ) {
        if (cancelled.getAsBoolean()) {
            return unavailableGenerated(seed, "Cancelled before generation");
        }

        var stem = context.selectedDimensions().get(LevelStem.OVERWORLD)
                .orElseThrow(() -> new IllegalStateException("Overworld dimension is unavailable"));
        if (!(stem.generator() instanceof NoiseBasedChunkGenerator generator)) {
            return unavailableGenerated(seed, "Overworld is not noise-based");
        }
        var randomState = RandomState.create(context.worldgenLoadContext(),
                generator.generatorSettings().unwrapKey()
                        .orElse(NoiseGeneratorSettings.OVERWORLD), seed);
        var structureState = generator.createState(
                context.worldgenLoadContext().lookupOrThrow(Registries.STRUCTURE_SET),
                randomState, seed);
        var found = findStructure(structureState, startType, profile);
        if (found == null) return unavailableGenerated(seed, "No structure placement found");
        if (templateManager == null) {
            return unavailableGenerated(seed, "No vanilla StructureTemplateManager was supplied");
        }
        long started = System.nanoTime();
        try {
            var height = LevelHeightAccessor.create(stem.type().value().minY(),
                    stem.type().value().height());
            var structureStart = found.structure.value().generate(
                    found.structure,
                    net.minecraft.world.level.Level.OVERWORLD,
                    context.worldgenLoadContext(),
                    generator,
                    generator.getBiomeSource(),
                    randomState,
                    templateManager,
                    seed,
                    found.chunk,
                    0,
                    height,
                    holder -> true);
            if (cancelled.getAsBoolean()) {
                return unavailableGenerated(seed, "Cancelled after generation");
            }
            var bounds = structureStart.getPieces().stream()
                    .map(piece -> piece.getBoundingBox()).toList();
            var result = new StructureGenerationInspectionResult(
                    seed, found.structureKey(), found.chunk,
                    structureStart.getBoundingBox(), bounds.size(), bounds,
                    (System.nanoTime() - started) / 1_000_000L,
                    structureStart.isValid() && !bounds.isEmpty(), "");
            lastGeneratedInspection = result;
            return result;
        } catch (RuntimeException failure) {
            return unavailableGenerated(seed,
                    "Vanilla structure generation failed: " + failure.getClass().getSimpleName());
        }
    }

    /**
     * Performs real structure-start/piece inspection against the selected
     * Nether generator. This remains geometry-only until a vanilla
     * WorldGenRegion is available for block placement.
     */
    public StructureGenerationInspectionResult inspectNetherStructure(
            long seed,
            boolean bastion,
            boolean fortress,
            BooleanSupplier cancelled
    ) {
        if (bastion == fortress) {
            return StructureGenerationInspectionResult.unavailable(seed,
                    "Select exactly one Nether structure type");
        }
        if (cancelled.getAsBoolean()) {
            return StructureGenerationInspectionResult.unavailable(seed,
                    "Cancelled before Nether generation");
        }
        var stem = context.selectedDimensions().get(LevelStem.NETHER)
                .orElseThrow(() -> new IllegalStateException("Nether dimension is unavailable"));
        if (!(stem.generator() instanceof NoiseBasedChunkGenerator generator)) {
            return StructureGenerationInspectionResult.unavailable(seed,
                    "Nether is not noise-based");
        }
        var settings = generator.generatorSettings().unwrapKey()
                .orElse(NoiseGeneratorSettings.NETHER);
        var randomState = RandomState.create(context.worldgenLoadContext(), settings, seed);
        var state = generator.createState(
                context.worldgenLoadContext().lookupOrThrow(Registries.STRUCTURE_SET),
                randomState, seed);
        var tokens = bastion ? new String[]{"bastion"} : new String[]{"fortress"};
        var found = findStructureByTokens(state, tokens, 128);
        if (found == null) {
            return StructureGenerationInspectionResult.unavailable(seed,
                    "No Nether structure placement found");
        }
        if (templateManager == null) {
            return StructureGenerationInspectionResult.unavailable(seed,
                    "No vanilla StructureTemplateManager was supplied");
        }
        long started = System.nanoTime();
        try {
            var type = stem.type().value();
            var height = LevelHeightAccessor.create(type.minY(), type.height());
            var structureStart = found.structure.value().generate(
                    found.structure,
                    net.minecraft.world.level.Level.NETHER,
                    context.worldgenLoadContext(),
                    generator,
                    generator.getBiomeSource(),
                    randomState,
                    templateManager,
                    seed,
                    found.chunk,
                    0,
                    height,
                    holder -> true);
            if (cancelled.getAsBoolean()) {
                return StructureGenerationInspectionResult.unavailable(seed,
                        "Cancelled after Nether generation");
            }
            var bounds = structureStart.getPieces().stream()
                    .map(piece -> piece.getBoundingBox()).toList();
            return new StructureGenerationInspectionResult(
                    seed, "minecraft:the_nether/" + found.structureKey(), found.chunk,
                    structureStart.getBoundingBox(), bounds.size(), bounds,
                    (System.nanoTime() - started) / 1_000_000L,
                    structureStart.isValid() && !bounds.isEmpty(), "");
        } catch (RuntimeException failure) {
            return StructureGenerationInspectionResult.unavailable(seed,
                    "Vanilla Nether generation failed: " + failure.getClass().getSimpleName());
        }
    }

    public VillageStructureInspectionResult inspectVillage(
            long seed,
            FilterProfile profile,
            BooleanSupplier cancelled
    ) {
        if (cancelled.getAsBoolean()) {
            return VillageStructureInspectionResult.unavailable(seed, "Cancelled before inspection");
        }
        var stem = context.selectedDimensions().get(LevelStem.OVERWORLD)
                .orElseThrow(() -> new IllegalStateException("Overworld dimension is unavailable"));
        if (!(stem.generator() instanceof NoiseBasedChunkGenerator generator)) {
            return VillageStructureInspectionResult.unavailable(seed, "Overworld is not noise-based");
        }
        var randomState = RandomState.create(context.worldgenLoadContext(),
                generator.generatorSettings().unwrapKey()
                        .orElse(NoiseGeneratorSettings.OVERWORLD), seed);
        var structureState = generator.createState(
                context.worldgenLoadContext().lookupOrThrow(Registries.STRUCTURE_SET),
                randomState, seed);
        var found = findStructure(structureState, StartType.VILLAGE, profile);
        if (found == null) {
            return VillageStructureInspectionResult.unavailable(seed, "No village placement found");
        }
        return inspectVillage(seed, generator, randomState, found, stem, cancelled);
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
        Holder<Structure> bestStructure = null;
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
                        bestStructure = matchingStructure(set, tokens);
                    }

                }
            }
        }
        return best == null || bestStructure == null
                ? null : new ChunkCandidate(best, bestStructureKey, bestStructure);
    }

    private ChunkCandidate findStructureByTokens(
            ChunkGeneratorStructureState state,
            String[] tokens,
            int radius
    ) {
        ChunkCandidate best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Holder<StructureSet> holder : state.possibleStructureSets()) {
            if (holder.unwrapKey().isEmpty()) continue;
            String setName = holder.unwrapKey().orElseThrow().identifier()
                    .getPath().toLowerCase(Locale.ROOT);
            if (!java.util.Arrays.stream(tokens).anyMatch(setName::contains)) continue;
            var set = holder.value();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (!set.placement().isStructureChunk(state, x, z)) continue;
                    double distance = Math.hypot(x, z);
                    if (distance < bestDistance) {
                        var key = matchingStructureKey(set, tokens);
                        var structure = matchingStructure(set, tokens);
                        if (structure != null) {
                            best = new ChunkCandidate(new ChunkPos(x, z), key, structure);
                            bestDistance = distance;
                        }
                    }
                }
            }
        }
        return best;
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

    private Holder<Structure> matchingStructure(StructureSet set, String[] tokens) {
        return set.structures().stream()
                .map(entry -> entry.structure())
                .filter(holder -> holder.unwrapKey().isPresent()
                        && java.util.Arrays.stream(tokens).anyMatch(token ->
                        holder.unwrapKey().orElseThrow().identifier().getPath()
                                .toLowerCase(Locale.ROOT).contains(token)))
                .findFirst().orElse(null);
    }

    private VillageStructureInspectionResult inspectVillage(
            long seed,
            ChunkGenerator generator,
            RandomState randomState,
            ChunkCandidate found,
            LevelStem stem,
            BooleanSupplier cancelled
    ) {
        if (templateManager == null) {
            var result = VillageStructureInspectionResult.unavailable(
                    seed, "No vanilla StructureTemplateManager was supplied");
            lastVillageInspection = result;
            lastGeneratedInspection = new StructureGenerationInspectionResult(
                    result.seed(), "minecraft:village", result.startChunk(),
                    result.structureBounds(), result.pieceCount(), result.pieceBounds(),
                    result.generationTimeMillis(), result.verified(), result.failureReason());
            return result;
        }

        if (cancelled.getAsBoolean()) {
            var result = VillageStructureInspectionResult.unavailable(seed, "Cancelled before generation");
            lastVillageInspection = result;
            return result;
        }
        long started = System.nanoTime();
        StructureStart start;
        try {
            var height = LevelHeightAccessor.create(stem.type().value().minY(),
                    stem.type().value().height());
            start = found.structure.value().generate(
                    found.structure,
                    net.minecraft.world.level.Level.OVERWORLD,
                    context.worldgenLoadContext(),
                    generator,
                    generator.getBiomeSource(),
                    randomState,
                    templateManager,
                    seed,
                    found.chunk,
                    0,
                    height,
                    holder -> true);
        } catch (RuntimeException failure) {
            var result = VillageStructureInspectionResult.unavailable(
                    seed, "Vanilla village generation failed: " + failure.getClass().getSimpleName());
            lastVillageInspection = result;
            return result;
        }
        if (cancelled.getAsBoolean()) {
            var result = VillageStructureInspectionResult.unavailable(seed, "Cancelled after generation");
            lastVillageInspection = result;
            return result;
        }
        var pieces = start.getPieces();
        var bounds = pieces.stream().map(piece -> piece.getBoundingBox()).toList();
        var result = new VillageStructureInspectionResult(seed, found.chunk,
                start.getBoundingBox(), pieces.size(), bounds,
                (System.nanoTime() - started) / 1_000_000L,
                start.isValid() && !pieces.isEmpty(), "");
        lastVillageInspection = result;
        lastGeneratedInspection = new StructureGenerationInspectionResult(
                result.seed(), "minecraft:village", result.startChunk(),
                result.structureBounds(), result.pieceCount(), result.pieceBounds(),
                result.generationTimeMillis(), result.verified(), result.failureReason());
        return result;
    }

    private StructureGenerationInspectionResult unavailableGenerated(long seed, String reason) {
        var result = StructureGenerationInspectionResult.unavailable(seed, reason);
        lastGeneratedInspection = result;
        return result;
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

    private record ChunkCandidate(ChunkPos chunk, String structureKey, Holder<Structure> structure) {
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
