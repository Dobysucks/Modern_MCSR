package dev.fsg262.cache;

import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.SeedTypeChoice;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedCacheVersionTest {
    @Test
    void incompatibleVersionsAreNeverReturned() {
        var cache = new SeedCache();
        var staleKey = new SeedCacheKey(1L, SeedTypeChoice.VILLAGE, "STRICT",
                1L, "old-filter", "old-minecraft");
        cache.put(new CachedSeed(staleKey, VerificationStatus.PASS,
                VerificationStatus.PASS, VerificationStatus.PASS,
                "", "", "", Instant.EPOCH));
        assertTrue(cache.get(staleKey).isEmpty());
    }
}
