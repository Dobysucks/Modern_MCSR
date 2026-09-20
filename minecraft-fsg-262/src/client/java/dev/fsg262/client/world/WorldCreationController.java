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
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Native world-creation handoff. The accepted seed is written into the
 * vanilla UI state, which is the source Minecraft uses when creating a world.
 */
public final class WorldCreationController implements AutoCloseable {
    private static final WorldCreationController INSTANCE = new WorldCreationController();
    private final SeedSearchManager searchManager = new SeedSearchManager();
    private final LocalSeedFilter localFilter =
            new LocalSeedFilter(new dev.fsg262.filter.UnverifiedWorldGenerationAnalyzer());
    private final AtomicReference<SeedSearchHandle> activeSearch = new AtomicReference<>();
    private final AtomicReference<Long> acceptedSeed = new AtomicReference<>();
    private volatile WorldCreationSettings settings = WorldCreationSettings.defaults();

    public static WorldCreationController instance() {
        return INSTANCE;
    }

    public WorldCreationSettings settings() {
        return settings;
    }

    public void updateSettings(WorldCreationSettings settings) {
        this.settings = settings;
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
    }

    public void startSearch(
            CreateWorldScreen screen,
            Consumer<SeedSearchProgress> progressReporter,
            Consumer<String> statusReporter
    ) {
        cancel();
        var currentSettings = settings;
        var profile = FilterProfile.strictRankedStyle();
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
        });
        activeSearch.set(handle);
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

    @Override
    public void close() {
        cancel();
        searchManager.close();
    }
}
