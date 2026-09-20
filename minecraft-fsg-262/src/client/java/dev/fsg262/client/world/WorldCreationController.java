package dev.fsg262.client.world;

import dev.fsg262.filter.FilterProfile;
import dev.fsg262.search.CandidateEvaluation;
import dev.fsg262.search.LocalFilterResult;
import dev.fsg262.search.LocalSeedFilter;
import dev.fsg262.search.SeedCandidateEvaluator;
import dev.fsg262.search.SeedSearchHandle;
import dev.fsg262.search.SeedSearchManager;
import dev.fsg262.search.SeedSearchProgress;
import dev.fsg262.search.SeedSearchRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Native world-creation handoff. The accepted seed is written into the
 * vanilla UI state, which is the source Minecraft uses when creating a world.
 */
public final class WorldCreationController implements AutoCloseable {
    private static final WorldCreationController INSTANCE = new WorldCreationController();
    private final SeedSearchManager searchManager = new SeedSearchManager();
    private final AtomicReference<SeedSearchHandle> activeSearch = new AtomicReference<>();
    private final AtomicReference<Long> acceptedSeed = new AtomicReference<>();
    private final AtomicReference<SeedSearchProgress> progress = new AtomicReference<>();
    private volatile String status = "Idle";
    private volatile WorldCreationSettings settings = loadSettings();

    public static WorldCreationController instance() {
        return INSTANCE;
    }

    public WorldCreationSettings settings() {
        return settings;
    }

    public void updateSettings(WorldCreationSettings settings) {
        this.settings = settings;
        saveSettings(settings);
    }

    public SeedSearchProgress progress() {
        return progress.get();
    }

    public String status() {
        return status;
    }

    public boolean hasAcceptedSeed() {
        return acceptedSeed.get() != null;
    }

    public boolean acceptedSeedMatches(String seed) {
        try {
            return hasAcceptedSeed() && acceptedSeed() == Long.parseLong(seed.trim());
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
    public long acceptedSeed() {
        var seed = acceptedSeed.get();
        if (seed == null) throw new IllegalStateException("No accepted seed is available");
        return seed;
    }

    public void cancel() {
        var current = activeSearch.getAndSet(null);
        if (current != null) current.cancel();
        acceptedSeed.set(null);
        status = "Stopped";
    }

    public void startSearch(
            CreateWorldScreen screen,
            Consumer<SeedSearchProgress> progressReporter,
            Consumer<String> statusReporter
    ) {
        cancel();
        var currentSettings = settings;
        var profile = profile(currentSettings.profileName());
        var localFilter = new LocalSeedFilter(
                new Minecraft26WorldgenAnalyzer(screen.getUiState().getSettings()));
        var request = new SeedSearchRequest(
                screen.getUiState().getSeed().hashCode(),
                currentSettings.seedType(), profile, 100_000);
        SeedCandidateEvaluator evaluator = (candidate, ignored) -> {
            LocalFilterResult result = localFilter.evaluate(candidate,
                    currentSettings.rngSeed(candidate), currentSettings.seedType(),
                    profile, currentSettings.completable());
            return switch (result.status()) {
                case PASS -> CandidateEvaluation.accepted(result.reason());
                case FAIL -> CandidateEvaluation.rejected(result.reason());
                case NOT_VERIFIED -> CandidateEvaluation.notVerified(result.reason());
            };
        };
        var handle = searchManager.search(request, evaluator, progressReporter, seed -> {
            acceptedSeed.set(seed);
            screen.getUiState().setSeed(Long.toString(seed));
            statusReporter.accept("ACCEPTED SEED FOUND: " + seed);
            status = "Seed Found";
        });
        activeSearch.set(handle);
        status = "Searching";
        statusReporter.accept("Searching for " + currentSettings.seedType().name() + "...");
    }

    public void requireAcceptedSeed(CreateWorldScreen screen) {
        if (!settings.enabled()) return;
        var accepted = acceptedSeed.get();
        if (accepted == null) {
            throw new IllegalStateException("FSG search has not produced an accepted seed");
        }
        screen.getUiState().setSeed(Long.toString(accepted));
    }

    private FilterProfile profile(String name) {
        return switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "BALANCED" -> FilterProfile.balanced();
            case "COMPLETABLE" -> FilterProfile.completable();
            default -> FilterProfile.strictRankedStyle();
        };
    }

    private static Path settingsPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("fsg262.properties");
    }

    private static WorldCreationSettings loadSettings() {
        var defaults = WorldCreationSettings.defaults();
        var properties = new Properties();
        try (Reader reader = Files.newBufferedReader(settingsPath())) {
            properties.load(reader);
            var type = dev.fsg262.filter.SeedTypeChoice.valueOf(
                    properties.getProperty("seedType", defaults.seedType().name()));
            var custom = properties.getProperty("rngSeed", "");
            return new WorldCreationSettings(
                    Boolean.parseBoolean(properties.getProperty("enabled", Boolean.toString(defaults.enabled()))),
                    type, properties.getProperty("profile", defaults.profileName()),
                    Boolean.parseBoolean(properties.getProperty("completable",
                            Boolean.toString(defaults.completable()))),
                    Boolean.parseBoolean(properties.getProperty("standardizedRng",
                            Boolean.toString(defaults.standardizedRng()))),
                    custom.isBlank() ? null : Long.valueOf(custom),
                    Boolean.parseBoolean(properties.getProperty("backgroundFiltering",
                            Boolean.toString(defaults.backgroundFiltering()))));
        } catch (IOException | IllegalArgumentException ignored) {
            return defaults;
        }
    }

    private static void saveSettings(WorldCreationSettings settings) {
        try {
            Files.createDirectories(settingsPath().getParent());
            var properties = new Properties();
            properties.setProperty("enabled", Boolean.toString(settings.enabled()));
            properties.setProperty("seedType", settings.seedType().name());
            properties.setProperty("profile", settings.profileName());
            properties.setProperty("completable", Boolean.toString(settings.completable()));
            properties.setProperty("standardizedRng", Boolean.toString(settings.standardizedRng()));
            properties.setProperty("rngSeed", settings.customRngSeedText());
            properties.setProperty("backgroundFiltering", Boolean.toString(settings.backgroundFiltering()));
            try (Writer writer = Files.newBufferedWriter(settingsPath())) {
                properties.store(writer, "FSG 26.2 MCSR settings");
            }
        } catch (IOException ignored) {
            // Runtime settings remain available for this session.
        }
    }

    @Override
    public void close() {
        cancel();
        searchManager.close();
    }
}
