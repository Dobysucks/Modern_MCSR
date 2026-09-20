package dev.fsg262.evaluation;

import dev.fsg262.completion.CompletionEvidence;
import dev.fsg262.completion.CompletionEvaluation;
import dev.fsg262.completion.CompletionValidator;
import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.SeedEvaluation;
import dev.fsg262.filter.SeedFilter;
import dev.fsg262.filter.WorldGenerationAnalyzer;
import dev.fsg262.nether.NetherEvaluation;
import dev.fsg262.rng.StandardizedSpeedrunRng;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the evaluator in a fixed order.  A missing adapter is represented as
 * NOT_VERIFIED; it is never converted into PASS.
 */
public final class EvaluatorOrchestrator {
    private final SeedFilter seedFilter;
    private final WorldGenerationAnalyzer analyzer;
    private final EvaluatorEvidenceProvider evidence;
    private final CompletionValidator completionValidator;

    public EvaluatorOrchestrator(WorldGenerationAnalyzer analyzer) {
        this(analyzer, new EvaluatorEvidenceProvider() {});
    }

    public EvaluatorOrchestrator(WorldGenerationAnalyzer analyzer,
                                 EvaluatorEvidenceProvider evidence) {
        this.analyzer = analyzer;
        this.seedFilter = new SeedFilter(analyzer);
        this.evidence = evidence;
        this.completionValidator = new CompletionValidator();
    }

    public EvaluationResult evaluate(long seed, FilterProfile profile) {
        var stages = new ArrayList<EvaluationStage>();
        List<SeedEvaluation> overworld = seedFilter.evaluate(seed, profile);
        boolean filterPassed = !overworld.isEmpty()
                && overworld.stream().allMatch(SeedEvaluation::passed);
        VerificationStatus overworldStatus = filterPassed
                ? (analyzer.isVerified() ? VerificationStatus.PASS : VerificationStatus.NOT_VERIFIED)
                : VerificationStatus.FAIL;
        stages.add(new EvaluationStage("overworld",
                overworldStatus, summarizeOverworld(overworld)));

        TerrainEvidence terrain = evidence.terrain(seed);
        stages.add(new EvaluationStage("terrain/block summary",
                terrain.verified() ? VerificationStatus.PASS : VerificationStatus.NOT_VERIFIED,
                terrain.summary()));

        NetherEvaluation nether = evidence.nether(seed, profile);
        VerificationStatus netherStatus = nether == null ? VerificationStatus.NOT_VERIFIED
                : (nether.passed() ? VerificationStatus.PASS : VerificationStatus.FAIL);
        VerificationStatus geometryStatus = evidence.netherGeometry(seed);
        if (nether == null && geometryStatus != VerificationStatus.NOT_VERIFIED) {
            netherStatus = geometryStatus;
        }
        stages.add(new EvaluationStage("Nether geometry", netherStatus,
                nether == null ? "adapter did not provide geometry" : summarizeNether(nether)));

        // Computing the selected RNG stream is deterministic evidence, not a
        // claim that loot or progression was observed.
        long rngMarker = new StandardizedSpeedrunRng(seed,
                StandardizedSpeedrunRng.Mode.MODERN_262).nextLong(0);
        stages.add(new EvaluationStage("RNG", VerificationStatus.PASS,
                "modern-262 stream=0 marker=" + rngMarker));

        CompletionEvidence completionEvidence = evidence.completion(seed);
        if (completionEvidence == null) {
            completionEvidence = unknownCompletion("completion observations unavailable");
        }
        CompletionEvaluation completion = completionValidator.validate(filterPassed, completionEvidence);
        stages.add(new EvaluationStage("CompletionValidator", completion.status(),
                completion.failures().isEmpty()
                        ? (completion.notVerified().isEmpty() ? "all checks passed" : String.join(", ", completion.notVerified()))
                        : String.join(", ", completion.failures())));

        VerificationStatus result = stages.stream().map(EvaluationStage::status)
                .reduce(VerificationStatus.PASS, (current, next) ->
                        current == VerificationStatus.FAIL || next == VerificationStatus.FAIL
                                ? VerificationStatus.FAIL
                                : current == VerificationStatus.NOT_VERIFIED
                                || next == VerificationStatus.NOT_VERIFIED
                                ? VerificationStatus.NOT_VERIFIED : VerificationStatus.PASS);
        return new EvaluationResult(seed, result, stages, completion, overworld, nether,
                terrain, rngMarker);
    }

    private String summarizeOverworld(List<SeedEvaluation> values) {
        long passed = values.stream().filter(SeedEvaluation::passed).count();
        return passed + "/" + values.size() + " start profiles passed"
                + (analyzer.isVerified() ? "" : " (adapter unverified)");
    }

    private String summarizeNether(NetherEvaluation value) {
        long passed = value.requirements().stream().filter(r -> r.passed()).count();
        return passed + "/" + value.requirements().size() + " geometry checks passed";
    }

    private CompletionEvidence unknownCompletion(String note) {
        return new CompletionEvidence(VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, note);
    }
}
