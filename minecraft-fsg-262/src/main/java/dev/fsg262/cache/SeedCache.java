package dev.fsg262.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Small local TSV cache. Each field is base64 encoded so debug summaries may
 * contain spaces or punctuation without corrupting the file.
 */
public final class SeedCache {
    public static final String FILTER_VERSION = "26.2-ranked-adapted-v1";
    public static final String MINECRAFT_VERSION = "26.2";
    private final Map<SeedCacheKey, CachedSeed> entries = new LinkedHashMap<>();

    public synchronized Optional<CachedSeed> get(SeedCacheKey key) {
        if (!key.isCurrent()) return Optional.empty();
        return Optional.ofNullable(entries.get(key));
    }

    public synchronized void put(CachedSeed entry) {
        if (entry.key().isCurrent()) entries.put(entry.key(), entry);
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void save(Path path) throws IOException {
        var lines = entries.values().stream().map(this::encode).toList();
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.write(path, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public synchronized void load(Path path) throws IOException {
        if (!Files.exists(path)) return;
        for (var line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                var entry = decode(line);
                if (entry.key().isCurrent()) put(entry);
            }
        }
    }

    public synchronized void exportTo(Path path) throws IOException {
        save(path);
    }

    public synchronized void importFrom(Path path) throws IOException {
        load(path);
    }

    private String encode(CachedSeed entry) {
        var key = entry.key();
        return String.join("\t",
                Long.toString(key.seed()),
                key.seedType().name(),
                b64(key.profileName()),
                Long.toString(key.rngSeed()),
                b64(key.filterVersion()),
                b64(key.minecraftVersion()),
                entry.filterResult().name(),
                entry.completionResult().name(),
                entry.lavaResult().name(),
                b64(entry.overworldSummary()),
                b64(entry.netherSummary()),
                b64(entry.strongholdSummary()),
                entry.timestamp().toString());
    }

    private CachedSeed decode(String line) {
        var parts = line.split("\t", -1);
        if (parts.length != 13) throw new IllegalArgumentException("Invalid FSG seed cache row");
        var key = new SeedCacheKey(Long.parseLong(parts[0]),
                dev.fsg262.filter.SeedTypeChoice.valueOf(parts[1]),
                unb64(parts[2]), Long.parseLong(parts[3]), unb64(parts[4]), unb64(parts[5]));
        return new CachedSeed(key,
                dev.fsg262.completion.VerificationStatus.valueOf(parts[6]),
                dev.fsg262.completion.VerificationStatus.valueOf(parts[7]),
                dev.fsg262.completion.VerificationStatus.valueOf(parts[8]),
                unb64(parts[9]), unb64(parts[10]), unb64(parts[11]),
                Instant.parse(parts[12]));
    }

    private String b64(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String unb64(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}