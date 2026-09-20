package dev.fsg262.completion;

/**
 * Describes where an observation came from.  Simulated observations are
 * deterministic test/prototype evidence and must never be presented as
 * vanilla-generated observations.
 */
public enum EvidenceType {
    OBSERVED,
    SIMULATED,
    UNAVAILABLE
}
