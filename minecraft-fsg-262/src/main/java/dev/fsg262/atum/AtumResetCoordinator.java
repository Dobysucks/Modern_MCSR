package dev.fsg262.atum;

import dev.fsg262.search.SeedSearchHandle;
import dev.fsg262.search.SeedSearchManager;
import dev.fsg262.search.SeedSearchRequest;
import dev.fsg262.search.SeedCandidateEvaluator;
import dev.fsg262.search.AcceptedSeed;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class AtumResetCoordinator implements AutoCloseable {
    private final SeedSearchManager searchManager;
    private final AtumResetBridge atum;
    private final AtomicReference<SeedSearchHandle> activeSearch = new AtomicReference<>();

    public AtumResetCoordinator(SeedSearchManager searchManager, AtumResetBridge atum) {
        this.searchManager = searchManager;
        this.atum = atum;
    }

    public void onResetRequested(
            SeedSearchRequest request,
            SeedCandidateEvaluator evaluator,
            Consumer<dev.fsg262.search.SeedSearchProgress> progressReporter
    ) {
        stop();
        var handle = searchManager.search(request, evaluator, progressReporter,
                seed -> {
                    // The bridge receives the accepted seed on its own safe
                    // handoff; no client object is touched by the worker.
                    // A richer AcceptedSeed is assembled by the client
                    // evaluator before it calls this coordinator in the
                    // verified Atum adapter.
                });
        activeSearch.set(handle);
    }

    public void accept(AcceptedSeed acceptedSeed) {
        stop();
        atum.injectAcceptedSeed(acceptedSeed);
    }

    public void stop() {
        var current = activeSearch.getAndSet(null);
        if (current != null) current.cancel();
    }

    @Override
    public void close() {
        stop();
        searchManager.close();
    }
}