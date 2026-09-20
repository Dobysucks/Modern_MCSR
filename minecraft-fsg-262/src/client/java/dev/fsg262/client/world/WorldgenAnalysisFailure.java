package dev.fsg262.client.world;

import dev.fsg262.completion.VerificationStatus;

/**
 * A failure in an analysis stage.  Keeping this separate from a human-facing
 * reason prevents bootstrap errors from being mistaken for a negative result.
 */
public record WorldgenAnalysisFailure(
        long seed,
        String stage,
        String exceptionType,
        String reason,
        long elapsedMillis,
        boolean fallbackAttempted,
        VerificationStatus verificationStatus
) {
    public WorldgenAnalysisFailure {
        if (stage == null || stage.isBlank()) stage = "unknown";
        if (exceptionType == null || exceptionType.isBlank()) exceptionType = "Unknown";
        if (reason == null || reason.isBlank()) reason = "No reason supplied";
        if (elapsedMillis < 0) elapsedMillis = 0;
        if (verificationStatus == null) verificationStatus = VerificationStatus.NOT_VERIFIED;
    }

    public static WorldgenAnalysisFailure of(
            long seed, String stage, Throwable failure, long started, boolean fallbackAttempted) {
        String reason = failure.getMessage();
        if (reason == null || reason.isBlank()) reason = failure.toString();
        return new WorldgenAnalysisFailure(seed, stage,
                failure.getClass().getName(), reason,
                (System.nanoTime() - started) / 1_000_000L,
                fallbackAttempted, VerificationStatus.NOT_VERIFIED);
    }
}
