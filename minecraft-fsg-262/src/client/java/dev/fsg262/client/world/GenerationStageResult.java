package dev.fsg262.client.world;

public record GenerationStageResult(boolean available, String detail) {
    public static GenerationStageResult available(String detail) {
        return new GenerationStageResult(true, detail);
    }

    public static GenerationStageResult unavailable(String detail) {
        return new GenerationStageResult(false, detail);
    }
}
