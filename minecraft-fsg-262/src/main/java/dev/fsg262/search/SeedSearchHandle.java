package dev.fsg262.search;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SeedSearchHandle {
    private final AtomicBoolean cancelled;
    private final Future<?> future;

    SeedSearchHandle(AtomicBoolean cancelled, Future<?> future) {
        this.cancelled = cancelled;
        this.future = future;
    }

    public void cancel() {
        cancelled.set(true);
        future.cancel(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean isDone() {
        return future.isDone();
    }
}