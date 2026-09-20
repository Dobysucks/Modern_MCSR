package dev.fsg262.search;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Single-purpose background worker. No Minecraft world or client object may
 * be touched by the evaluator; game-thread handoff belongs in the callback.
 */
public final class SeedSearchManager implements AutoCloseable {
    private final ExecutorService executor;

    public SeedSearchManager() {
        this(Executors.newSingleThreadExecutor(runnable -> {
            var thread = new Thread(runnable, "fsg262-seed-search");
            thread.setDaemon(true);
            return thread;
        }));
    }

    SeedSearchManager(ExecutorService executor) {
        this.executor = executor;
    }

    public SeedSearchHandle search(
            SeedSearchRequest request,
            SeedCandidateEvaluator evaluator,
            Consumer<SeedSearchProgress> progressReporter,
            Consumer<Long> acceptedSeedReporter
    ) {
        var cancelled = new AtomicBoolean(false);
        var future = executor.submit(() -> {
            var generator = new CandidateSeedGenerator(request.generatorSeed());
            long tested = 0;
            long rejected = 0;
            long filterPassed = 0;
            long completable = 0;
            for (long index = 0; index < request.maximumCandidates(); index++) {
                if (cancelled.get() || Thread.currentThread().isInterrupted()) return;
                long candidate = generator.seedAt(index);
                var evaluation = evaluator.evaluate(candidate, request);
                tested++;
                if (evaluation.decision() == SearchDecision.REJECTED) rejected++;
                if (evaluation.decision() == SearchDecision.ACCEPTED) {
                    filterPassed++;
                    completable++;
                    progressReporter.accept(new SeedSearchProgress(
                            tested, rejected, filterPassed, completable,
                            candidate, evaluation.reason()));
                    acceptedSeedReporter.accept(candidate);
                    return;
                }
                progressReporter.accept(new SeedSearchProgress(
                        tested, rejected, filterPassed, completable,
                        candidate, evaluation.reason()));
            }
        });
        return new SeedSearchHandle(cancelled, future);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}