package dev.fsg262.search;

public record CandidateEvaluation(
        SearchDecision decision,
        String reason
){ 
    public static CandidateEvaluation accepted(String reason) {
        return new CandidateEvaluation(SearchDecision.ACCEPTED, reason);
    }

    public static CandidateEvaluation rejected(String reason) {
        return new CandidateEvaluation(SearchDecision.REJECTED, reason);
    }

    public static CandidateEvaluation notVerified(String reason) {
        return new CandidateEvaluation(SearchDecision.NOT_VERIFIED, reason);
    }
}