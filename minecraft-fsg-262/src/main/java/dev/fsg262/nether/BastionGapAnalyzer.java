package dev.fsg262.nether;

import java.util.List;

/**
 * Defines a good gap as a path segment that is explicit and inspectable.
 *
 * <p>The numeric terrain definition is a project-level contract pending
 * verification against the public historical filter specification. It is not
 * hidden inside a vague "terrain looks open" check.
 */
public final class BastionGapAnalyzer {
    public record GapCriteria(
            int minimumLengthBlocks,
            int minimumWidthBlocks,
            int minimumHeadroomBlocks,
            int maximumBlockedFloorCells,
            boolean lavaForbidden
    ) {
        public static GapCriteria documentedPendingVerification() {
            // TODO — NEEDS VERIFICATION: confirm these geometric values against
            // the public stables-gap technical specification.
            return new GapCriteria(3, 3, 3, 0, true);
        }
    }

    public record GapObservation(
            int lengthBlocks,
            int widthBlocks,
            int headroomBlocks,
            int blockedFloorCells,
            boolean containsLava
    ) {}

    private final GapCriteria criteria;

    public BastionGapAnalyzer() {
        this(GapCriteria.documentedPendingVerification());
    }

    public BastionGapAnalyzer(GapCriteria criteria) {
        this.criteria = criteria;
    }

    public boolean isGoodGap(GapObservation observation) {
        return observation.lengthBlocks() >= criteria.minimumLengthBlocks()
                && observation.widthBlocks() >= criteria.minimumWidthBlocks()
                && observation.headroomBlocks() >= criteria.minimumHeadroomBlocks()
                && observation.blockedFloorCells() <= criteria.maximumBlockedFloorCells()
                && (!criteria.lavaForbidden() || !observation.containsLava());
    }

    public int countGoodGaps(List<GapObservation> observations) {
        return (int) observations.stream().filter(this::isGoodGap).count();
    }

    public GapCriteria criteria() {
        return criteria;
    }
}