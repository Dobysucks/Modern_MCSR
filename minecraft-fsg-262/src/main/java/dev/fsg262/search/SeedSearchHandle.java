package dev.fsg262.search;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SeedSearchHandle {
    private final AtomicBoolean cancelled;
    private final AtomicBoolean paused;
    private final Future<?> future;

    SeedSearchHandle(AtomicBoolean cancelled, AtomicBoolean paused, Future<?> future) {
        this.cancelled = cancelled;
        this.paused = paused;
        this.future = future;
    }

    public void cancel() {
        cancelled.set(true);
        future.cancel(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void pause() {
        if (!isDone()) paused.set(true);
    }

    public void resume() {
        paused.set(false);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public boolean isDone() {
        return future.isDone();
    }
}