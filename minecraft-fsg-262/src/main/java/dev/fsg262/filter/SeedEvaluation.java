package dev.fsg262.filter;

import java.util.List;

public record SeedEvaluation(long seed, StartType startType, List<RequirementResult> requirements) {
    public SeedEvaluation {
        requirements = List.copyOf(requirements);
    }

    public boolean passed() {
        return requirements.stream().allMatch(RequirementResult::passed);
    }

    public String report() {
        var out = new StringBuilder();
        out.append("SEED: ").append(seed).append('\n');
        out.append("START TYPE: ").append(startType).append('\n');
        for (var requirement : requirements) {
            out.append(requirement.passed() ? "[PASS] " : "[FAIL] ")
                    .append(requirement.label()).append('\n')
                    .append("Expected: ").append(requirement.expected()).append('\n')
                    .append("Found: ").append(requirement.found()).append('\n')
                    .append("Why: ").append(requirement.explanation()).append('\n');
        }
        out.append("FINAL RESULT: ").append(passed() ? "PASS" : "FAIL").append('\n');
        return out.toString();
    }
}