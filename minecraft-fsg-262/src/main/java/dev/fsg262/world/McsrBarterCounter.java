package dev.fsg262.world;

public final class McsrBarterCounter {
    private int nextIndex;

    public synchronized int next() {
        int index = nextIndex;
        nextIndex = index == Integer.MAX_VALUE ? 0 : index + 1;
        return index;
    }

    public synchronized int current() {
        return nextIndex;
    }

    public synchronized void restore(int index) {
        nextIndex = Math.max(0, index);
    }
}
