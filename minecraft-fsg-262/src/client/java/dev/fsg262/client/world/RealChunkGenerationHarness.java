package dev.fsg262.client.world;

import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * Bounded direct use of Minecraft's 26.2 chunk pipeline.
 *
 * <p>This is intentionally not a mock and does not duplicate terrain logic:
 * it allocates vanilla ProtoChunks and invokes the selected ChunkGenerator's
 * biome and noise stages. Structure starts are generated with vanilla
 * Structure.generate when a template manager is supplied. Later stages are
 * reported unavailable rather than represented by fake world objects.</p>
 */
public final class RealChunkGenerationHarness {
    private final WorldCreationContext context;
    private final StructureTemplateManager templateManager;

    public RealChunkGenerationHarness(WorldCreationContext context,
                                      StructureTemplateManager templateManager) {
        this.context = context;
        this.templateManager = templateManager;
    }

    public GeneratedChunk generateOverworld(long seed, ChunkPos position,
                                            BooleanSupplier cancelled) {
        return generate(seed, LevelStem.OVERWORLD, Level.OVERWORLD, position, cancelled);
    }

    public GeneratedChunk generateNether(long seed, ChunkPos position,
                                         BooleanSupplier cancelled) {
        return generate(seed, LevelStem.NETHER, Level.NETHER, position, cancelled);
    }

    /**
     * Generates at most {@code maxChunks} requested chunks, preserving request
     * order. The limit is part of the API so a search cannot accidentally turn
     * this inspection adapter into an unbounded world generator.
     */
    public Map<ChunkPos, GeneratedChunk> generateOverworld(long seed,
                                                            Iterable<ChunkPos> positions,
                                                            int maxChunks,
                                                            BooleanSupplier cancelled) {
        if (maxChunks < 1) throw new IllegalArgumentException("maxChunks must be positive");
        var result = new LinkedHashMap<ChunkPos, GeneratedChunk>();
        for (var position : positions) {
            if (result.size() >= maxChunks || cancelled.getAsBoolean()) break;
            result.put(position, generateOverworld(seed, position, cancelled));
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public GeneratedChunk generate(long seed, net.minecraft.resources.ResourceKey<LevelStem> stemKey,
                                   net.minecraft.resources.ResourceKey<Level> dimension,
                                   ChunkPos position, BooleanSupplier cancelled) {
        var stages = new EnumMap<GenerationStage, GenerationStageResult>(GenerationStage.class);
        var starts = new HashMap<Structure, StructureStart>();
        var stem = context.selectedDimensions().get(stemKey)
                .orElseThrow(() -> new IllegalStateException("Dimension is unavailable: " + stemKey.identifier()));
        if (!(stem.generator() instanceof NoiseBasedChunkGenerator generator)) {
            return unavailable(null, starts, stages, "Dimension generator is not noise-based");
        }
        if (cancelled.getAsBoolean()) {
            return unavailable(null, starts, stages, "Cancelled before chunk allocation");
        }

        var type = stem.type().value();
        var height = LevelHeightAccessor.create(type.minY(), type.height());
        var factory = PalettedContainerFactory.create(context.worldgenLoadContext());
        ChunkAccess chunk = new ProtoChunk(position, UpgradeData.EMPTY, height, factory, null);
        var settings = generator.generatorSettings().unwrapKey().orElse(
                stemKey.equals(LevelStem.NETHER) ? NoiseGeneratorSettings.NETHER
                        : NoiseGeneratorSettings.OVERWORLD);
        var randomState = RandomState.create(context.worldgenLoadContext(), settings, seed);
        try {
            generator.createBiomes(randomState, Blender.empty(), null, chunk).join();
            stages.put(GenerationStage.BIOMES, GenerationStageResult.available(
                    "Vanilla ChunkGenerator.createBiomes"));
        } catch (RuntimeException failure) {
            stages.put(GenerationStage.BIOMES, failed(failure));
            return unavailable(chunk, starts, stages, "Biome generation failed");
        }
        if (cancelled.getAsBoolean()) {
            return unavailable(chunk, starts, stages, "Cancelled after biome generation");
        }
        try {
            generator.fillFromNoise(Blender.empty(), randomState, null, chunk).join();
            stages.put(GenerationStage.TERRAIN, GenerationStageResult.available(
                    "Vanilla ChunkGenerator.fillFromNoise"));
        } catch (RuntimeException failure) {
            stages.put(GenerationStage.TERRAIN, failed(failure));
            return unavailable(chunk, starts, stages, "Terrain generation failed");
        }
        if (cancelled.getAsBoolean()) {
            return unavailable(chunk, starts, stages, "Cancelled after terrain generation");
        }

        if (templateManager == null) {
            stages.put(GenerationStage.STRUCTURE_STARTS, GenerationStageResult.unavailable(
                    "Requires StructureTemplateManager"));
        } else {
            try {
                var state = generator.createState(
                        context.worldgenLoadContext().lookupOrThrow(Registries.STRUCTURE_SET),
                        randomState, seed);
                for (Holder<StructureSet> setHolder : state.possibleStructureSets()) {
                    var set = setHolder.value();
                    if (!set.placement().isStructureChunk(state, position.x(), position.z())) continue;
                    for (var entry : set.structures()) {
                        var structure = entry.structure();
                        var start = structure.value().generate(
                                structure, dimension, context.worldgenLoadContext(), generator,
                                generator.getBiomeSource(), randomState, templateManager, seed,
                                position, 0, height, holder -> true);
                        if (start.isValid()) {
                            starts.put(structure.value(), start);
                            chunk.setStartForStructure(structure.value(), start);
                        }
                    }
                }
                stages.put(GenerationStage.STRUCTURE_STARTS, GenerationStageResult.available(
                        "Vanilla Structure.generate (" + starts.size() + " starts)"));
            } catch (RuntimeException failure) {
                stages.put(GenerationStage.STRUCTURE_STARTS, failed(failure));
            }
        }
        stages.put(GenerationStage.STRUCTURE_BLOCKS, GenerationStageResult.unavailable(
                "Requires WorldGenRegion for template block placement"));
        stages.put(GenerationStage.LEVEL_CHUNK, GenerationStageResult.unavailable(
                "Requires a live ServerLevel; ProtoChunk is returned"));
        stages.put(GenerationStage.BLOCK_ENTITIES, GenerationStageResult.unavailable(
                "Structure blocks were not placed, so no generated block entities exist"));
        return new GeneratedChunk(chunk, Optional.empty(), starts, stages);
    }

    private static GeneratedChunk unavailable(ChunkAccess chunk,
                                              Map<Structure, StructureStart> starts,
                                              EnumMap<GenerationStage, GenerationStageResult> stages,
                                              String reason) {
        stages.putIfAbsent(GenerationStage.STRUCTURE_BLOCKS,
                GenerationStageResult.unavailable("Not reached: " + reason));
        stages.putIfAbsent(GenerationStage.LEVEL_CHUNK,
                GenerationStageResult.unavailable("Requires a live ServerLevel; ProtoChunk is returned"));
        stages.putIfAbsent(GenerationStage.BLOCK_ENTITIES,
                GenerationStageResult.unavailable("Not reached: " + reason));
        return new GeneratedChunk(chunk, Optional.empty(), starts, stages);
    }

    private static GenerationStageResult failed(RuntimeException failure) {
        return GenerationStageResult.unavailable(
                "Vanilla stage failed: " + failure.getClass().getSimpleName());
    }
}
