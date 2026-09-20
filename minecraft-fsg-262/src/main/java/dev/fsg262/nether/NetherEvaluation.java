package dev.fsg262.nether;

import dev.fsg262.filter.RequirementResult;
import java.util.List;

public record NetherEvaluation(long seed, List<RequirementResult> requirements) {
    public NetherEvaluation {
        requirements = List.copyOf(requirements);
    }

    public boolean passed() {
        return requirements.stream().allMatch(RequirementResult::passed);
    }

    public String report() {
        var out = new StringBuilder("NETHER\n");
        for (var requirement : requirements) {
            out.append(requirement.passed() ? "[PASS] " : "[FAIL] ")
                    .append(requirement.label()).append('\n')
                    .append("Expected: ").append(requirement.expected()).append('\n')
                    .append("Found: ").append(requirement.found()).append('\n')
                    .append("Why: ").append(requirement.explanation()).append('\n');
        }
        out.append("NETHER RESULT: ").append(passed() ? "PASS" : "FAIL").append('\n');
        return out.toString();
    }
}