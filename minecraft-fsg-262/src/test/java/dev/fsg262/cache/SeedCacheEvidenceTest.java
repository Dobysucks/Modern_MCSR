package dev.fsg262.cache;

import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.SeedTypeChoice;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeedCacheEvidenceTest {
    @Test
    void persistsFailureEvidenceAndTiming() throws Exception {
        var cache = new SeedCache();
        var key = new SeedCacheKey(42L, SeedTypeChoice.VILLAGE, "STRICT",
                7L, SeedCache.FILTER_VERSION, SeedCache.MINECRAFT_VERSION);
        cache.put(new CachedSeed(key, VerificationStatus.FAIL,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                "village", "nether", "stronghold", "loot unavailable",
                "pieces=4", 123L, Instant.parse("2026-01-01T00:00:00Z")));
        var path = Files.createTempFile("fsg-cache", ".tsv");
        try {
            cache.save(path);
            var loaded = new SeedCache();
            loaded.load(path);
            var value = loaded.get(key).orElseThrow();
            assertEquals("loot unavailable", value.failureReason());
            assertEquals("pieces=4", value.evidenceSummary());
            assertEquals(123L, value.analysisTimeMillis());
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
