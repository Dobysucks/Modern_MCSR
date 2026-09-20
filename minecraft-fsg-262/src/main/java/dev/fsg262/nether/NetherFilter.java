package dev.fsg262.nether;

import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.RequirementResult;
import java.util.ArrayList;

public final class NetherFilter {
    public NetherEvaluation evaluate(NetherEvaluationInput input, FilterProfile profile) {
        var out = new ArrayList<RequirementResult>();
        out.add(RequirementResult.pass("Intended bastion",
                "selected by candidate/separation rule",
                input.intendedBastionSelected() ? "selected" : "not selected",
                "The filter does not accept an arbitrary nearest bastion.")
                .withPass(input.intendedBastionSelected()));
        out.add(threshold("Bastion distance from Nether origin",
                input.bastionDistanceFromOriginChunks(),
                profile.bastionMaxDistanceFromOriginChunks(),
                "The origin is Nether block coordinate (0, 0), measured in chunks."));
        out.add(threshold("Bastion separation",
                input.bastionSeparationFromCompetitorChunks(),
                profile.bastionMinimumSeparationChunks(),
                "The intended bastion must beat competing bastions by the inclusive separation threshold."));
        out.add(RequirementResult.pass("Bastion open terrain", "pass",
                input.openTerrainToBastion() ? "pass" : "fail",
                "Pathability is supplied by the terrain adapter, not Euclidean distance.")
                .withPass(input.openTerrainToBastion()));
        out.add(RequirementResult.pass("Bastion iron", ">=" + profile.bastionMinimumIron(),
                Integer.toString(input.bastionIron()),
                "Housing bastions may use the appropriate housing chest set.")
                .withPass(input.bastionIron() >= profile.bastionMinimumIron()));
        out.add(RequirementResult.pass("Bastion obsidian", ">=" + profile.bastionMinimumObsidian(),
                Integer.toString(input.bastionObsidian()),
                "Loot is analyzed from the intended bastion chest configuration.")
                .withPass(input.bastionObsidian() >= profile.bastionMinimumObsidian()));

        if (input.bastionType() == BastionType.HOGLIN_STABLES) {
            boolean stablePass = input.goodGaps() >= profile.stableMinimumGoodGaps()
                    || (input.goodGaps() >= 1 && input.tripleChestRamparts() >= 1);
            out.add(RequirementResult.pass("Stables gaps",
                    profile.stableMinimumGoodGaps() + " good gaps OR 1 good gap + 1 triple-chest rampart",
                    "goodGaps=" + input.goodGaps() + ", tripleChestRamparts=" + input.tripleChestRamparts(),
                    "Good gaps are evaluated by BastionGapAnalyzer with explicit geometry.")
                    .withPass(stablePass));
        }

        out.add(RequirementResult.pass("Intended fortress",
                "selected by bastion-relative candidate rule",
                input.intendedFortressSelected() ? "selected" : "not selected",
                "The fortress is selected relative to the intended bastion.")
                .withPass(input.intendedFortressSelected()));
        out.add(threshold("Fortress distance from bastion",
                input.fortressDistanceFromBastionChunks(),
                profile.fortressMaxDistanceFromBastionChunks(),
                "Chunk/region path interpretation is supplied by the adapter."));
        out.add(RequirementResult.pass("Fortress open terrain", "pass",
                input.openTerrainToFortress() ? "pass" : "fail",
                "The route is checked separately from the origin distance.")
                .withPass(input.openTerrainToFortress()));
        return new NetherEvaluation(input.seed(), out);
    }

    private RequirementResult threshold(String label, double found, int maximum, String explanation) {
        boolean pass = found <= maximum;
        return RequirementResult.pass(label, "<=" + maximum + " chunks",
                String.format(java.util.Locale.ROOT, "%.3f chunks", found),
                explanation).withPass(pass);
    }
}