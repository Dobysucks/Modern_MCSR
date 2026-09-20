package dev.fsg262.search;

import dev.fsg262.completion.CompletionEvaluation;
import dev.fsg262.completion.CompletionEvidence;
import dev.fsg262.completion.CompletionValidator;
import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.OverworldFilter;
import dev.fsg262.filter.SeedEvaluation;
import dev.fsg262.filter.SeedFilter;
import dev.fsg262.filter.SeedTypeChoice;
import dev.fsg262.filter.StartType;
import dev.fsg262.filter.WorldGenerationAnalyzer;
import dev.fsg262.rng.LegacyPiglinBartering;

import java.util.List;

/**
 * The complete local pipeline boundary. Every stage runs in-process and has
 * explicit verification semantics; an unavailable Minecraft adapter cannot
 * become a successful seed by default.
 */
public final class LocalSeedFilter {
    private final WorldGenerationAnalyzer worldgen;
    private final SeedFilter overworld;
    private final LegacyPiglinBartering bartering = new LegacyPiglinBartering();
    private final CompletionValidator completion = new CompletionValidator();

    public LocalSeedFilter(WorldGenerationAnalyzer worldgen) {
        this.worldgen = worldgen;
        this.overworld = new SeedFilter(worldgen);
    }

    public LocalFilterResult evaluate(
            long worldSeed,
            long rngSeed,
            SeedTypeChoice seedType,
            FilterProfile profile,
            boolean requireCompletable
    ) {
        List<SeedEvaluation> evaluations = seedType == SeedTypeChoice.ANY
                ? overworld.evaluate(worldSeed, profile)
                : List.of(new OverworldFilter().evaluate(
                        worldgen.analyzeOverworld(worldSeed,
                                StartType.valueOf(seedType.name()), profileFor(seedType, profile)),
                        profileFor(seedType, profile)));

        if (!worldgen.isVerified()) {
            return new LocalFilterResult(VerificationStatus.NOT_VERIFIED,
                    "Minecraft 26.2 world-generation adapter is NOT VERIFIED",
                    evaluations, VerificationStatus.NOT_VERIFIED,
                    notVerifiedCompletion());
        }

        if (evaluations.stream().anyMatch(evaluation -> !evaluation.passed())) {
            return new LocalFilterResult(VerificationStatus.FAIL,
                    "Overworld requirements failed", evaluations,
                    VerificationStatus.NOT_VERIFIED, notVerifiedCompletion());
        }

        boolean rngPass = bartering.hasWindowGuarantees(rngSeed, 0);
        var rngStatus = rngPass ? VerificationStatus.PASS : VerificationStatus.FAIL;
        if (!rngPass) {
            return new LocalFilterResult(VerificationStatus.FAIL,
                    "Standardized 72-barter requirements failed", evaluations,
                    rngStatus, notVerifiedCompletion());
        }

        CompletionEvaluation completionResult = requireCompletable
                ? completion.validate(true, notVerifiedEvidence())
                : new CompletionEvaluation(VerificationStatus.PASS, true, List.of(), List.of());
        VerificationStatus result = requireCompletable
                ? completionResult.status()
                : VerificationStatus.PASS;
        return new LocalFilterResult(result, result == VerificationStatus.PASS
                ? "All local filter stages passed" : "Completion route is not verified",
                evaluations, rngStatus, completionResult);
    }

    private FilterProfile profileFor(SeedTypeChoice seedType, FilterProfile profile) {
        var enabled = FilterProfile.customBuilder();
        for (var type : StartType.values()) {
            enabled.enabled(type, type == StartType.valueOf(seedType.name()));
        }
        return enabled.build();
    }

    private CompletionEvaluation notVerifiedCompletion() {
        return completion.validate(false, notVerifiedEvidence());
    }

    private CompletionEvidence notVerifiedEvidence() {
        return new CompletionEvidence(
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                VerificationStatus.NOT_VERIFIED, VerificationStatus.NOT_VERIFIED,
                "Requires a verified Minecraft 26.2 world-generation adapter");
    }
}
