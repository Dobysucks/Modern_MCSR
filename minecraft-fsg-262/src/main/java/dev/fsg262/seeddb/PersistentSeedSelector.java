package dev.fsg262.seeddb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.BitSet;
import java.util.random.RandomGenerator;

public final class PersistentSeedSelector {
    private static final int MAGIC = 0x4D435352;
    private static final int FORMAT_VERSION = 1;

    private final SeedDatabase database;
    private final Path persistenceFile;
    private final RandomGenerator random;
    private final BitSet consumed;
    private final BitSet reserved;
    private final int[] availableIndexes;
    private final int[] availablePositions;
    private int availableCount;

    public PersistentSeedSelector(SeedDatabase database, Path persistenceFile) throws IOException {
        this(database, persistenceFile, RandomGenerator.getDefault());
    }

    PersistentSeedSelector(SeedDatabase database, Path persistenceFile, RandomGenerator random)
            throws IOException {
        this.database = database;
        this.persistenceFile = persistenceFile;
        this.random = random;
        this.consumed = loadConsumed();
        this.reserved = new BitSet(database.size());
        this.availableCount = database.size() - consumed.cardinality();
        this.availableIndexes = new int[database.size()];
        this.availablePositions = new int[database.size()];
        java.util.Arrays.fill(availablePositions, -1);
        var next = 0;
        for (var index = 0; index < database.size(); index++) {
            if (!consumed.get(index)) {
                availableIndexes[next] = index;
                availablePositions[index] = next++;
            }
        }
    }

    public synchronized java.util.Optional<Reservation> reserveNext() {
        if (availableCount == 0) return java.util.Optional.empty();
        var selectedPosition = random.nextInt(availableCount);
        var selectedIndex = availableIndexes[selectedPosition];
        removeAvailable(selectedPosition);
        reserved.set(selectedIndex);
        return java.util.Optional.of(new Reservation(selectedIndex));
    }

    public synchronized java.util.Optional<Reservation> reserveNextMatching(String tag) {
        var matchingIndexes = database.indexesWithTag(tag);
        if (matchingIndexes.length == 0) return java.util.Optional.empty();

        for (var attempt = 0; attempt < 8; attempt++) {
            var index = matchingIndexes[random.nextInt(matchingIndexes.length)];
            if (availablePositions[index] >= 0) return reserveIndex(index);
        }

        var matchingAvailableCount = 0;
        var selectedIndex = -1;
        for (var index : matchingIndexes) {
            if (availablePositions[index] < 0) continue;
            matchingAvailableCount++;
            if (random.nextInt(matchingAvailableCount) == 0) selectedIndex = index;
        }
        return selectedIndex < 0
                ? java.util.Optional.empty() : reserveIndex(selectedIndex);
    }

    public synchronized java.util.Optional<Reservation> reserve(long seed) {
        for (var index = 0; index < database.size(); index++) {
            if (database.seedAt(index) != seed) continue;
            if (availablePositions[index] >= 0) return reserveIndex(index);
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<Reservation> reserveIndex(int index) {
        removeAvailable(availablePositions[index]);
        reserved.set(index);
        return java.util.Optional.of(new Reservation(index));
    }

    private void removeAvailable(int position) {
        var removedIndex = availableIndexes[position];
        var replacementIndex = availableIndexes[availableCount - 1];
        availableIndexes[position] = replacementIndex;
        availablePositions[replacementIndex] = position;
        availablePositions[removedIndex] = -1;
        availableCount--;
    }

    private synchronized void release(int index) {
        if (!reserved.get(index) || consumed.get(index)) return;
        reserved.clear(index);
        availableIndexes[availableCount] = index;
        availablePositions[index] = availableCount++;
    }

    private synchronized void commit(int index) throws IOException {
        if (!reserved.get(index)) {
            if (consumed.get(index)) return;
            throw new IllegalStateException("Seed is no longer reserved: " + database.seedAt(index));
        }
        consumed.set(index);
        try {
            persist();
        } catch (IOException exception) {
            consumed.clear(index);
            throw exception;
        }
        reserved.clear(index);
    }

    public synchronized int remainingCount() {
        return availableCount;
    }

    public synchronized int consumedCount() {
        return consumed.cardinality();
    }

    public synchronized int reservedCount() {
        return reserved.cardinality();
    }

    public final class Reservation {
        private final int index;
        private boolean completed;

        private Reservation(int index) {
            this.index = index;
        }

        public long seed() {
            return database.seedAt(index);
        }

        public synchronized void commit() throws IOException {
            if (completed) return;
            PersistentSeedSelector.this.commit(index);
            completed = true;
        }

        public synchronized void release() {
            if (completed) return;
            PersistentSeedSelector.this.release(index);
            completed = true;
        }
    }

    private BitSet loadConsumed() throws IOException {
        if (!Files.exists(persistenceFile)) return new BitSet(database.size());
        try (var input = new DataInputStream(new BufferedInputStream(
                Files.newInputStream(persistenceFile)))) {
            if (input.readInt() != MAGIC || input.readInt() != FORMAT_VERSION) {
                throw new IOException("Unsupported consumed-seed state: " + persistenceFile);
            }
            var storedSize = input.readInt();
            var storedFingerprint = input.readNBytes(32);
            if (storedSize != database.size()
                    || !java.util.Arrays.equals(storedFingerprint, database.fingerprint())) {
                throw new IOException("Consumed-seed state does not match the bundled database: "
                        + persistenceFile);
            }
            var bytesLength = input.readInt();
            var maximumBytes = (database.size() + 7) / 8;
            if (bytesLength < 0 || bytesLength > maximumBytes) {
                throw new IOException("Invalid consumed-seed bitmap length: " + bytesLength);
            }
            var bytes = input.readNBytes(bytesLength);
            if (bytes.length != bytesLength || input.read() != -1) {
                throw new EOFException("Truncated or trailing consumed-seed state: " + persistenceFile);
            }
            var consumed = BitSet.valueOf(bytes);
            if (consumed.length() > database.size()) {
                throw new IOException("Consumed-seed bitmap exceeds database size: " + persistenceFile);
            }
            return consumed;
        }
    }

    private void persist() throws IOException {
        var parent = persistenceFile.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        var temporary = Files.createTempFile(parent, "mcsr-seeds-", ".tmp");
        try {
            try (var output = new DataOutputStream(new BufferedOutputStream(
                    Files.newOutputStream(temporary)))) {
                output.writeInt(MAGIC);
                output.writeInt(FORMAT_VERSION);
                output.writeInt(database.size());
                output.write(database.fingerprint());
                var bytes = consumed.toByteArray();
                output.writeInt(bytes.length);
                output.write(bytes);
            }
            try {
                Files.move(temporary, persistenceFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, persistenceFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
