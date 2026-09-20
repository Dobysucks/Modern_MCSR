package dev.fsg262.client.world;

/**
 * Timings for the staged, read-only world-generation analysis.
 * Values are milliseconds and are intended for diagnostics, not filtering.
 */
public record WorldgenAnalysisMetrics(
        long placementMillis,
        long terrainMillis,
        long pieceInspectionMillis,
        long lootMillis,
        long netherMillis,
        long lavaMillis,
        long totalMillis
) {
    public static WorldgenAnalysisMetrics empty() {
        return new WorldgenAnalysisMetrics(0, 0, 0, 0, 0, 0, 0);
    }
}
