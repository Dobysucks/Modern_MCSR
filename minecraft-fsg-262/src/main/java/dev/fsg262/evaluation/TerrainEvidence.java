package dev.fsg262.evaluation;

/**
 * A small, transport-safe summary of generated terrain.  The client adapter
 * may provide a real block summary; the command never treats an unavailable
 * summary as a successful check.
 */
public record TerrainEvidence(boolean verified, String summary) {
    public TerrainEvidence {
        summary = summary == null || summary.isBlank() ? "not available" : summary;
    }

    public static TerrainEvidence unavailable(String reason) {
        return new TerrainEvidence(false, reason);
    }
}
