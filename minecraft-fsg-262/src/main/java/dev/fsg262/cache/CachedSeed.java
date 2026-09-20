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
        Instant timestamp
) {}