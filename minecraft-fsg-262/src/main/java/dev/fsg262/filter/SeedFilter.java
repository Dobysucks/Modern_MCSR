package dev.fsg262.filter;

import java.util.ArrayList;
import java.util.List;

public final class SeedFilter {
    private final OverworldFilter overworldFilter;
    private final WorldGenerationAnalyzer analyzer;

    public SeedFilter(WorldGenerationAnalyzer analyzer) {
        this.overworldFilter = new OverworldFilter();
        this.analyzer = analyzer;
    }

    public List<SeedEvaluation> evaluate(long seed, FilterProfile profile) {
        var results = new ArrayList<SeedEvaluation>();
        for (var startType : profile.enabledStartTypes()) {
            results.add(overworldFilter.evaluate(
                    analyzer.analyzeOverworld(seed, startType, profile), profile));
        }
        return List.copyOf(results);
    }

    public String report(long seed, FilterProfile profile) {
        var out = new StringBuilder();
        for (var evaluation : evaluate(seed, profile)) {
            out.append(evaluation.report()).append('\n');
        }
        return out.toString();
    }
}