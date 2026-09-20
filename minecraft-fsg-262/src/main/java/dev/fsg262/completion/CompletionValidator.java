package dev.fsg262.completion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Evaluates completion independently from structure filtering. It never
 * upgrades unknown mechanics to PASS.
 */
public final class CompletionValidator {
    public CompletionEvaluation validate(boolean filterPassed, CompletionEvidence evidence) {
        var failures = new ArrayList<String>();
        var unknown = new ArrayList<String>();
        if (!filterPassed) failures.add("Seed filter did not pass");
        if (evidence.evidenceType() == EvidenceType.UNAVAILABLE) {
            unknown.add("evidence source");
        }

        Map<String, VerificationStatus> checks = new LinkedHashMap<>();
        checks.put("resources", evidence.resources());
        checks.put("portal access", evidence.portalAccess());
        checks.put("bastion", evidence.bastion());
        checks.put("fortress", evidence.fortress());
        checks.put("blaze progression", evidence.blazeProgression());
        checks.put("pearl progression", evidence.pearlProgression());
        checks.put("stronghold", evidence.stronghold());
        checks.put("End progression", evidence.end());
        checks.forEach((name, status) -> {
            if (status == VerificationStatus.FAIL) failures.add(name + " failed");
            if (status == VerificationStatus.NOT_VERIFIED) unknown.add(name);
        });

        VerificationStatus result;
        if (!failures.isEmpty()) result = VerificationStatus.FAIL;
        else if (!unknown.isEmpty()) result = VerificationStatus.NOT_VERIFIED;
        else result = VerificationStatus.PASS;
        return new CompletionEvaluation(result, filterPassed, failures, unknown);
    }
}