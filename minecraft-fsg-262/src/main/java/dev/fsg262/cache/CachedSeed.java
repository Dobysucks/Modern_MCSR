package dev.fsg262.cache;

import dev.fsg262.completion.VerificationStatus;
import java.time.Instant;

public record CachedSeed(
        SeedCacheKey key,
        VerificationStatus filterResult,
        VerificationStatus completionResult,
        VerificationStatus lavaResult,
        String overworldSummary,
        String netherSummary,
        String strongholdSummary,
        String failureReason,
        String evidenceSummary,
        long analysisTimeMillis,
        Instant timestamp
) {
    public CachedSeed(SeedCacheKey key, VerificationStatus filterResult,
                      VerificationStatus completionResult, VerificationStatus lavaResult,
                      String overworldSummary, String netherSummary,
                      String strongholdSummary, Instant timestamp) {
        this(key, filterResult, completionResult, lavaResult, overworldSummary,
                netherSummary, strongholdSummary, "", "", 0L, timestamp);
    }
}