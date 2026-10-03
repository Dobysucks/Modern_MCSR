package dev.fsg262.client.world;

import dev.fsg262.search.AcceptedSeedPair;
import dev.fsg262.seeddb.PersistentSeedSelector;
import dev.fsg262.seeddb.SeedDatabase;
import dev.fsg262.seeddb.SeedDatabaseResources;
import dev.fsg262.seeddb.SeedCreationLifecycle;
import dev.fsg262.world.McsrWorldSeedState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorldCreationController {
    private static final WorldCreationController INSTANCE = new WorldCreationController();
    private static final Logger LOGGER = Logger.getLogger("fsg262");

    private final Map<String, PersistentSeedSelector> selectors = new HashMap<>();
    private final SeedCreationLifecycle creationLifecycle = new SeedCreationLifecycle();
    private volatile WorldCreationSettings settings = loadSettings();
    private volatile AcceptedSeedPair acceptedSeeds;
    private volatile String status = "Ready";
    private WorldCreationController() {}

    public static WorldCreationController instance() {
        return INSTANCE;
    }

    public synchronized WorldCreationSettings settings() {
        return settings;
    }

    public synchronized void updateSettings(WorldCreationSettings updated) {
        var next = updated;
        if (!next.enabled()) {
            creationLifecycle.creationFailed();
            next = next.withOverworldSeed(null).withNetherSeed(null);
        }
        if (!java.util.Objects.equals(settings.overworldCategory(), next.overworldCategory())
                || !java.util.Objects.equals(creationLifecycle.overworldSeed(), next.overworldSeed())) {
            creationLifecycle.selectOverworld(null);
        }
        if (!java.util.Objects.equals(settings.netherCategory(), next.netherCategory())
                || !java.util.Objects.equals(creationLifecycle.netherSeed(), next.netherSeed())) {
            creationLifecycle.selectNether(null);
        }
        settings = next;
        acceptedSeeds = next.overworldSeed() == null || next.netherSeed() == null
                ? null : new AcceptedSeedPair(next.overworldSeed(), next.netherSeed());
        try {
            saveSettings(next);
        } catch (IOException exception) {
            status = "Could not save MCSR settings: " + exception.getMessage();
            LOGGER.log(Level.WARNING, "Could not save MCSR settings", exception);
        }
    }

    public AcceptedSeedPair acceptedSeeds() {
        return acceptedSeeds;
    }

    public String status() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String countsText() {
        return "Seeds: Overworld " + SeedDatabaseResources.overworld().size()
                + " | Nether " + SeedDatabaseResources.nether().size();
    }

    public synchronized OptionalLong selectOverworldCategory(String category) throws IOException {
        if (!WorldCreationSettings.OVERWORLD_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Unsupported Overworld category: " + category);
        }
        creationLifecycle.selectOverworld(null);
        settings = settings.withOverworldCategory(category);
        acceptedSeeds = null;
        saveSettings(settings);
        if (!settings.enabled()) return OptionalLong.empty();
        return reserveOverworldCategory();
    }

    public synchronized OptionalLong selectNetherCategory(String category) throws IOException {
        if (!WorldCreationSettings.NETHER_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Unsupported Nether category: " + category);
        }
        creationLifecycle.selectNether(null);
        settings = settings.withNetherCategory(category);
        acceptedSeeds = null;
        saveSettings(settings);
        if (!settings.enabled()) return OptionalLong.empty();
        return reserveNetherCategory();
    }

    public synchronized void ensureCategorySelections() throws IOException {
        if (!settings.enabled()) return;
        try {
            if (settings.overworldSeed() == null) reserveOverworldCategory();
            if (settings.netherSeed() == null) reserveNetherCategory();
        } catch (IOException | RuntimeException exception) {
            creationLifecycle.creationFailed();
            settings = settings.withOverworldSeed(null).withNetherSeed(null);
            acceptedSeeds = null;
            persistSettings();
            throw exception;
        }
    }

    private OptionalLong reserveOverworldCategory() throws IOException {
        var current = settings;
        var selected = selector("overworld", current.profileName(),
                SeedDatabaseResources.overworld())
                .reserveNextMatching(current.overworldCategory());
        if (selected.isEmpty()) {
            status = "No available Overworld seeds tagged " + current.overworldCategory()
                    + " for profile " + current.profileName();
            return OptionalLong.empty();
        }
        var reservation = selected.get();
        creationLifecycle.selectOverworld(reservation);
        settings = settings.withOverworldSeed(reservation.seed());
        status = "Selected " + current.overworldCategory() + " Overworld candidate";
        refreshAcceptedSeeds();
        return OptionalLong.of(reservation.seed());
    }

    private OptionalLong reserveNetherCategory() throws IOException {
        var current = settings;
        var selected = selector("nether", current.profileName(),
                SeedDatabaseResources.nether())
                .reserveNextMatching(current.netherCategory());
        if (selected.isEmpty()) {
            status = "No available Nether seeds tagged " + current.netherCategory()
                    + " for profile " + current.profileName();
            return OptionalLong.empty();
        }
        var reservation = selected.get();
        creationLifecycle.selectNether(reservation);
        settings = settings.withNetherSeed(reservation.seed());
        status = "Selected " + current.netherCategory() + " Nether candidate";
        refreshAcceptedSeeds();
        return OptionalLong.of(reservation.seed());
    }

    private void refreshAcceptedSeeds() {
        acceptedSeeds = settings.overworldSeed() == null || settings.netherSeed() == null
                ? null : new AcceptedSeedPair(settings.overworldSeed(), settings.netherSeed());
    }

    public synchronized boolean prepareWorldCreation(CreateWorldScreen screen) {
        if (!settings.enabled()) {
            McsrWorldSeedState.clearPending();
            return true;
        }
        try {
            ensureCategorySelections();
            if (settings.overworldSeed() == null || settings.netherSeed() == null) {
                creationLifecycle.creationFailed();
                settings = settings.withOverworldSeed(null).withNetherSeed(null);
                acceptedSeeds = null;
                return false;
            }
            var current = settings;
            if (creationLifecycle.overworldSeed() == null) {
                var reservation = selector("overworld", current.profileName(),
                        SeedDatabaseResources.overworld()).reserve(current.overworldSeed())
                        .orElseThrow(() -> new IllegalStateException(
                                "Selected Overworld seed was already consumed or is not in the database"));
                creationLifecycle.selectOverworld(reservation);
            }
            if (creationLifecycle.netherSeed() == null) {
                var reservation = selector("nether", current.profileName(),
                        SeedDatabaseResources.nether()).reserve(current.netherSeed())
                        .orElseThrow(() -> new IllegalStateException(
                                "Selected Nether seed was already consumed or is not in the database"));
                creationLifecycle.selectNether(reservation);
            }
            var accepted = new AcceptedSeedPair(current.overworldSeed(), current.netherSeed());
            acceptedSeeds = accepted;
            McsrWorldSeedState.prepareWorldCreation(accepted.mcsr_overworld_seed(),
                    accepted.mcsr_nether_seed(), current.disablePiglinBrutes(),
                    current.standardizedRng());
            creationLifecycle.beginCreation();
            screen.getUiState().setSeed(Long.toString(accepted.mcsr_overworld_seed()));
            status = "Creating world with separate MCSR Overworld and Nether seeds";
            return true;
        } catch (IOException | IllegalStateException exception) {
            creationLifecycle.creationFailed();
            settings = settings.withOverworldSeed(null).withNetherSeed(null);
            acceptedSeeds = null;
            persistSettings();
            status = "Could not select MCSR seeds: " + exception.getMessage();
            return false;
        }
    }

    public synchronized void worldCreationFailed() {
        if (!creationLifecycle.isCreating()) return;
        McsrWorldSeedState.clearPending();
        creationLifecycle.creationFailed();
        settings = settings.withOverworldSeed(null).withNetherSeed(null);
        acceptedSeeds = null;
        persistSettings();
        status = "World creation failed; selected seeds remain unconsumed";
    }

    public synchronized void creationScreenRemoved() {
        creationLifecycle.screenRemoved();
        if (!creationLifecycle.isCreating()) {
            McsrWorldSeedState.clearPending();
            settings = settings.withOverworldSeed(null).withNetherSeed(null);
            acceptedSeeds = null;
            persistSettings();
            status = "Create World closed; selected seeds remain unconsumed";
        }
    }

    public synchronized void worldCreationFinished() {
        if (!creationLifecycle.isCreating()) return;
        try {
            creationLifecycle.creationSucceeded();
            settings = settings.withOverworldSeed(null).withNetherSeed(null);
            acceptedSeeds = null;
            persistSettings();
            status = "MCSR seeds consumed for created world";
        } catch (IOException | IllegalStateException exception) {
            status = "World created, but seed consumption could not be persisted: "
                    + exception.getMessage();
            LOGGER.log(Level.SEVERE, status, exception);
        }
    }

    public String seedLabel(Long seed) {
        return seed == null ? "Not selected" : Long.toString(seed);
    }

    private void persistSettings() {
        try {
            saveSettings(settings);
        } catch (IOException exception) {
            status = "Could not save MCSR settings: " + exception.getMessage();
            LOGGER.log(Level.WARNING, "Could not save MCSR settings", exception);
        }
    }

    private PersistentSeedSelector selector(String dimension, String profile,
                                            SeedDatabase database) throws IOException {
        var key = dimension + "|" + profile;
        var existing = selectors.get(key);
        if (existing != null) return existing;
        var file = settingsPath().getParent().resolve("consumed-seeds")
                .resolve(dimension).resolve(profile + ".bitmap");
        var created = new PersistentSeedSelector(database, file);
        selectors.put(key, created);
        return created;
    }

    private static Path settingsPath() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("fsg262.properties");
    }

    private static WorldCreationSettings loadSettings() {
        var defaults = WorldCreationSettings.defaults();
        if (!Files.exists(settingsPath())) return defaults;
        var properties = new Properties();
        try (Reader reader = Files.newBufferedReader(settingsPath())) {
            properties.load(reader);
            return new WorldCreationSettings(
                    Boolean.parseBoolean(properties.getProperty("enabled",
                            Boolean.toString(defaults.enabled()))),
                    properties.getProperty("profile", defaults.profileName()),
                    Boolean.parseBoolean(properties.getProperty("completable",
                            Boolean.toString(defaults.completable()))),
                    Boolean.parseBoolean(properties.getProperty("standardizedRng",
                            Boolean.toString(defaults.standardizedRng()))),
                    Boolean.parseBoolean(properties.getProperty("disablePiglinBrutes",
                            Boolean.toString(defaults.disablePiglinBrutes()))),
                    properties.getProperty("overworldCategory", defaults.overworldCategory()),
                    properties.getProperty("netherCategory", defaults.netherCategory()),
                    null, null);
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.log(Level.WARNING, "Could not load MCSR settings; using defaults", exception);
            return defaults;
        }
    }

    private static void saveSettings(WorldCreationSettings settings) throws IOException {
        Files.createDirectories(settingsPath().getParent());
        var properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.enabled()));
        properties.setProperty("profile", settings.profileName());
        properties.setProperty("completable", Boolean.toString(settings.completable()));
        properties.setProperty("standardizedRng", Boolean.toString(settings.standardizedRng()));
        properties.setProperty("disablePiglinBrutes",
                Boolean.toString(settings.disablePiglinBrutes()));
        properties.setProperty("overworldCategory", settings.overworldCategory());
        properties.setProperty("netherCategory", settings.netherCategory());
        try (Writer writer = Files.newBufferedWriter(settingsPath())) {
            properties.store(writer, "MCSR seed database settings");
        }
    }
}
