package dev.fsg262.client.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;

import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * Owns one disposable, server-side world.  Unlike the client world-creation
 * context this runs the normal WorldLoader and ServerChunkCache pipeline.
 */
public final class TemporaryServerWorld implements AutoCloseable {
    private final Path directory;
    private final LevelStorageSource.LevelStorageAccess storage;
    private final WorldStem worldStem;
    private final TemporaryDedicatedServer server;
    private final ExecutorService serverExecutor;
    private final ExecutorService reloadExecutor;
    private final AtomicBoolean closed = new AtomicBoolean();

    private TemporaryServerWorld(Path directory,
                                 LevelStorageSource.LevelStorageAccess storage,
                                 WorldStem worldStem,
                                 TemporaryDedicatedServer server,
                                 ExecutorService serverExecutor,
                                 ExecutorService reloadExecutor) {
        this.directory = directory;
        this.storage = storage;
        this.worldStem = worldStem;
        this.server = server;
        this.serverExecutor = serverExecutor;
        this.reloadExecutor = reloadExecutor;
    }

    public static TemporaryServerWorld open(WorldCreationContext context, long seed)
            throws IOException {
        Path directory = Files.createTempDirectory("fsg262-server-");
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "fsg262-temporary-server"));
        ExecutorService reloadExecutor = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "fsg262-temporary-reload"));
        LevelStorageSource.LevelStorageAccess storage = null;
        WorldStem stem = null;
        try {
            storage = LevelStorageSource.createDefault(directory).createAccess("world");
            PackRepository packs = Minecraft.getInstance().getResourcePackRepository();
            WorldDataConfiguration dataConfiguration = context.dataConfiguration();
            var packConfig = new WorldLoader.PackConfig(packs, dataConfiguration, false, false);
            var init = new WorldLoader.InitConfig(packConfig,
                    Commands.CommandSelection.DEDICATED,
                    net.minecraft.server.permissions.LevelBasedPermissionSet.ALL);
            WorldOptions options = new WorldOptions(seed, true, false);
            CompletableFuture<WorldStem> loaded = WorldLoader.load(init, loadContext -> {
                WorldGenSettings generation = WorldGenSettings.of(options,
                        loadContext.datapackDimensions());
                WorldDimensions.Complete dimensions = generation.dimensions().bake(
                        loadContext.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM));
                LevelSettings settings = new LevelSettings("fsg262-temporary",
                        GameType.SURVIVAL,
                        new LevelSettings.DifficultySettings(
                                net.minecraft.world.Difficulty.NORMAL, false, false),
                        true, loadContext.dataConfiguration());
                PrimaryLevelData data = new PrimaryLevelData(settings,
                        PrimaryLevelData.SpecialWorldProperty.NONE,
                        com.mojang.serialization.Lifecycle.stable());
                return new WorldLoader.DataLoadOutput<>(
                        new LevelDataAndDimensions.WorldDataAndGenSettings(data, generation),
                        loadContext.datapackDimensions());
            }, WorldStem::new, serverExecutor, reloadExecutor);
            stem = loaded.join();
            TemporaryDedicatedServer server = new TemporaryDedicatedServer(
                    Thread.currentThread(), storage, packs, stem,
                    new DedicatedServerSettings(directory.resolve("server.properties")),
                    Minecraft.getInstance().getFixerUpper(), null, null);
            server.initializeLevels();
            return new TemporaryServerWorld(directory, storage, stem, server,
                    serverExecutor, reloadExecutor);
        } catch (Throwable failure) {
            if (stem != null) stem.close();
            if (storage != null) storage.safeClose();
            serverExecutor.shutdownNow();
            reloadExecutor.shutdownNow();
            delete(directory);
            if (failure instanceof IOException io) throw io;
            throw new IOException("Unable to bootstrap temporary 26.2 server world", failure);
        }
    }

    public ServerLevel overworld() {
        ensureOpen();
        return server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
    }

    public MinecraftServer server() {
        ensureOpen();
        return server;
    }

    public CompletableFuture<ServerChunkGenerationResult> generate(
            net.minecraft.world.level.ChunkPos position, BooleanSupplier cancelled) {
        return generate(overworld(), position, cancelled);
    }

    public CompletableFuture<ServerChunkGenerationResult> generate(
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
            net.minecraft.world.level.ChunkPos position, BooleanSupplier cancelled) {
        ensureOpen();
        ServerLevel level = server.getLevel(dimension);
        if (level == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                    "Temporary server did not create dimension: " + dimension.identifier()));
        }
        return generate(level, position, cancelled);
    }

    public CompletableFuture<ServerChunkGenerationResult> generate(
            ServerLevel level, net.minecraft.world.level.ChunkPos position,
            BooleanSupplier cancelled) {
        ensureOpen();
        if (cancelled.getAsBoolean()) {
            return CompletableFuture.failedFuture(new java.util.concurrent.CancellationException());
        }
        return CompletableFuture.supplyAsync(
                () -> new ServerLevelChunkHarness(level).generate(position, cancelled),
                serverExecutor);
    }

    public Path directory() {
        return directory;
    }

    private void ensureOpen() {
        if (closed.get()) throw new IllegalStateException("Temporary server world is closed");
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        try {
            server.halt(false);
        } finally {
            serverExecutor.shutdownNow();
            reloadExecutor.shutdownNow();
            worldStem.close();
            storage.safeClose();
            delete(directory);
        }
    }

    private static void delete(Path path) {
        try (var files = Files.walk(path)) {
            files.sorted(java.util.Comparator.reverseOrder()).forEach(file -> {
                try { Files.deleteIfExists(file); } catch (IOException ignored) { }
            });
        } catch (IOException ignored) { }
    }

    private static final class TemporaryDedicatedServer extends DedicatedServer {
        private TemporaryDedicatedServer(Thread thread,
                                         LevelStorageSource.LevelStorageAccess storage,
                                         PackRepository packs,
                                         WorldStem stem,
                                         DedicatedServerSettings settings,
                                         com.mojang.datafixers.DataFixer fixer,
                                         net.minecraft.server.Services services,
                                         net.minecraft.server.jsonrpc.ManagementServer management) {
            super(thread, storage, packs, stem, Optional.empty(), settings, fixer,
                    services, management, new NotificationManager());
        }

        private void initializeLevels() {
            createLevels();
        }
    }
}
