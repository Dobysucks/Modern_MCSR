package dev.fsg262.filter;

public record RequirementResult(
        String label,
        boolean passed,
        String expected,
        String found,
        String explanation
) {
    public RequirementResult withPass(boolean actualPassed) {
        return new RequirementResult(label, actualPassed, expected, found, explanation);
    }

    public static RequirementResult pass(String label, String expected, String found, String explanation) {
        return new RequirementResult(label, true, expected, found, explanation);
    }

    public static RequirementResult fail(String label, String expected, String found, String explanation) {
        return new RequirementResult(label, false, expected, found, explanation);
    }
}