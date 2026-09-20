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
        String note,
        EvidenceType evidenceType
) {
    public CompletionEvidence {
        evidenceType = evidenceType == null ? EvidenceType.UNAVAILABLE : evidenceType;
    }

    public CompletionEvidence(VerificationStatus resources,
                               VerificationStatus portalAccess,
                               VerificationStatus bastion,
                               VerificationStatus fortress,
                               VerificationStatus blazeProgression,
                               VerificationStatus pearlProgression,
                               VerificationStatus stronghold,
                               VerificationStatus end,
                               String note) {
        this(resources, portalAccess, bastion, fortress, blazeProgression,
                pearlProgression, stronghold, end, note, EvidenceType.OBSERVED);
    }
}