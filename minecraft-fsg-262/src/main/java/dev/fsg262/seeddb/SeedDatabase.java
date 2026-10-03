package dev.fsg262.seeddb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SeedDatabase {
    private static final String SEED_PREFIX = "SEED: ";

    private final long[] seeds;
    private final int declaredCount;
    private final int malformedLineCount;
    private final List<Integer> malformedLineNumbers;
    private final Map<String, int[]> indexesByTag;
    private final byte[] fingerprint;

    private SeedDatabase(long[] seeds, int declaredCount, int malformedLineCount,
                         List<Integer> malformedLineNumbers,
                         Map<String, int[]> indexesByTag) {
        this.seeds = seeds;
        this.declaredCount = declaredCount;
        this.malformedLineCount = malformedLineCount;
        this.malformedLineNumbers = List.copyOf(malformedLineNumbers);
        this.indexesByTag = Map.copyOf(indexesByTag);
        this.fingerprint = fingerprint(seeds);
    }

    public static SeedDatabase parse(Reader source) throws IOException {
        var seeds = new long[1024];
        var size = 0;
        var malformed = 0;
        var malformedLineNumbers = new java.util.ArrayList<Integer>();
        var tagIndexes = new HashMap<String, IntAccumulator>();
        var declaredCount = -1;
        var readingEntries = false;
        var lineNumber = 0;
        try (var reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                var trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("Generated count:")) {
                    try {
                        declaredCount = Integer.parseInt(
                                trimmed.substring("Generated count:".length()).trim());
                    } catch (NumberFormatException ignored) {
                        malformed++;
                        retainLineNumber(malformedLineNumbers, lineNumber);
                    }
                    readingEntries = true;
                    continue;
                }
                if (!readingEntries && !trimmed.startsWith(SEED_PREFIX)) continue;
                readingEntries = true;
                var parsed = parseSeed(trimmed);
                if (parsed == null) {
                    malformed++;
                    retainLineNumber(malformedLineNumbers, lineNumber);
                    continue;
                }
                if (size == seeds.length) seeds = Arrays.copyOf(seeds, seeds.length * 2);
                seeds[size] = parsed.seed();
                for (var tag : parsed.tags()) {
                    tagIndexes.computeIfAbsent(tag, ignored -> new IntAccumulator()).add(size);
                }
                size++;
            }
        }
        var indexesByTag = new HashMap<String, int[]>();
        tagIndexes.forEach((tag, indexes) -> indexesByTag.put(tag, indexes.toArray()));
        return new SeedDatabase(Arrays.copyOf(seeds, size), declaredCount, malformed,
                malformedLineNumbers, indexesByTag);
    }

    private static void retainLineNumber(List<Integer> lineNumbers, int lineNumber) {
        if (lineNumbers.size() < 20) lineNumbers.add(lineNumber);
    }

    private static ParsedSeed parseSeed(String line) {
        if (!line.startsWith(SEED_PREFIX)) return null;
        var start = SEED_PREFIX.length();
        var end = start;
        while (end < line.length() && (line.charAt(end) == '-' || isDigit(line.charAt(end)))) end++;
        if (end == start || end == start + 1 && line.charAt(start) == '-') return null;
        if (end >= line.length() || line.charAt(end) != ' ' || end + 2 >= line.length()
                || line.charAt(end + 1) != '(' || !line.endsWith(")")) return null;
        try {
            var seed = Long.parseLong(line.substring(start, end));
            var tagsText = line.substring(end + 2, line.length() - 1);
            var tags = Arrays.stream(tagsText.split(",", -1))
                    .map(String::trim)
                    .toList();
            if (tags.isEmpty() || tags.stream().anyMatch(String::isEmpty)) return null;
            return new ParsedSeed(seed, tags);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record ParsedSeed(long seed, List<String> tags) {}

    private static final class IntAccumulator {
        private int[] values = new int[8];
        private int size;

        private void add(int value) {
            if (size == values.length) values = Arrays.copyOf(values, values.length * 2);
            values[size++] = value;
        }

        private int[] toArray() {
            return Arrays.copyOf(values, size);
        }
    }

    private static boolean isDigit(char value) {
        return value >= '0' && value <= '9';
    }

    private static byte[] fingerprint(long[] seeds) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (long seed : seeds) {
                for (int shift = 56; shift >= 0; shift -= 8) {
                    digest.update((byte) (seed >>> shift));
                }
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is not available", impossible);
        }
    }

    public int size() {
        return seeds.length;
    }

    public long seedAt(int index) {
        return seeds[index];
    }

    int[] indexesWithTag(String tag) {
        return indexesByTag.getOrDefault(tag, new int[0]);
    }

    public int declaredCount() {
        return declaredCount;
    }

    public int malformedLineCount() {
        return malformedLineCount;
    }

    public List<Integer> malformedLineNumbers() {
        return malformedLineNumbers;
    }

    public boolean declaredCountMatchesParsedCount() {
        return declaredCount < 0 || declaredCount == size();
    }

    byte[] fingerprint() {
        return fingerprint.clone();
    }
}
