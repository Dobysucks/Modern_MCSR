package dev.fsg262.completion;

public record CompletionEvidence(
        VerificationStatus resources,
        VerificationStatus portalAccess,
        VerificationStatus bastion,
        VerificationStatus fortress,
        VerificationStatus blazeProgression,
        VerificationStatus pearlProgression,
        VerificationStatus stronghold,
        VerificationStatus end,
        String note
) {}