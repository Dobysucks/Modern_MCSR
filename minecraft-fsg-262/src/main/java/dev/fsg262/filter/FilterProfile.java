package dev.fsg262.filter;

import java.util.EnumSet;
import java.util.Set;

/**
 * Immutable numeric contract for one filter profile.
 *
 * <p>Strict values are sourced from the supplied project brief. Values not
 * stated by that brief are intentionally not represented as guessed defaults.
 */
public record FilterProfile(
        String name,
        Set<StartType> enabledStartTypes,
        int villageMaxDistanceChunks,
        int shipwreckMaxDistanceChunks,
        int desertTempleMaxDistanceChunks,
        int ruinedPortalMaxDistanceChunks,
        int buriedTreasureMaxDistanceChunks,
        int minimumIron,
        int ironWithDiamonds,
        int minimumDiamonds,
        int riverMaxDistanceChunks,
        int minimumLavaPools,
        int villageBlacksmithObsidian,
        int taigaBlacksmithObsidian,
        int shipwreckMagmaRavines,
        int buriedTreasureMagmaRavines,
        int ruinedPortalIronNuggets,
        int bastionMaxDistanceFromOriginChunks,
        int bastionMinimumSeparationChunks,
        int bastionMinimumIron,
        int bastionMinimumObsidian,
        int stableMinimumGoodGaps,
        int fortressMaxDistanceFromBastionChunks,
        int barterWindowSize,
        int barterMinimumObsidianPerWindow,
        int barterExactPearlTradesPerWindow,
        long filterVersion
) {
    public FilterProfile {
        enabledStartTypes = Set.copyOf(enabledStartTypes);
        if (minimumIron < 0 || ironWithDiamonds < 0 || minimumDiamonds < 0) {
            throw new IllegalArgumentException("Resource thresholds cannot be negative");
        }
        if (barterWindowSize <= 0) {
            throw new IllegalArgumentException("barterWindowSize must be positive");
        }
    }

    public static FilterProfile strictRankedStyle() {
        return new FilterProfile(
                "STRICT_RANKED_STYLE",
                EnumSet.allOf(StartType.class),
                7, 4, 5, 3, 5,
                7, 4, 3,
                6, 3,
                8, 10,
                2, 2,
                18,
                14, 10, 3, 5, 2, 16,
                72, 6, 3,
                1L
        );
    }

    /**
     * A deliberately named non-strict profile. It is not a claim about an
     * official Ranked configuration; it is a documented convenience profile.
     */
    public static FilterProfile balanced() {
        return new FilterProfile(
                "BALANCED",
                EnumSet.allOf(StartType.class),
                9, 6, 7, 4, 7,
                6, 4, 3,
                8, 2,
                6, 8,
                1, 1,
                14,
                16, 8, 2, 4, 1, 18,
                72, 4, 3,
                2L
        );
    }

    /**
     * Completable uses the strict structure thresholds and requires the
     * separate CompletionValidator to return PASS before accepting a seed.
     */
    public static FilterProfile completable() {
        return strictRankedStyle().named("COMPLETABLE", 3L);
    }

    public FilterProfile named(String newName, long newFilterVersion) {
        return new FilterProfile(
                newName, enabledStartTypes,
                villageMaxDistanceChunks, shipwreckMaxDistanceChunks,
                desertTempleMaxDistanceChunks, ruinedPortalMaxDistanceChunks,
                buriedTreasureMaxDistanceChunks, minimumIron,
                ironWithDiamonds, minimumDiamonds, riverMaxDistanceChunks,
                minimumLavaPools, villageBlacksmithObsidian,
                taigaBlacksmithObsidian, shipwreckMagmaRavines,
                buriedTreasureMagmaRavines, ruinedPortalIronNuggets,
                bastionMaxDistanceFromOriginChunks,
                bastionMinimumSeparationChunks, bastionMinimumIron,
                bastionMinimumObsidian, stableMinimumGoodGaps,
                fortressMaxDistanceFromBastionChunks, barterWindowSize,
                barterMinimumObsidianPerWindow,
                barterExactPearlTradesPerWindow, newFilterVersion
        );
    }

    public static Builder customBuilder() {
        return new Builder(strictRankedStyle());
    }

    public static final class Builder {
        private String name;
        private Set<StartType> enabledStartTypes;
        private int villageMaxDistanceChunks;
        private int shipwreckMaxDistanceChunks;
        private int desertTempleMaxDistanceChunks;
        private int ruinedPortalMaxDistanceChunks;
        private int buriedTreasureMaxDistanceChunks;
        private int minimumIron;
        private int ironWithDiamonds;
        private int minimumDiamonds;
        private int riverMaxDistanceChunks;
        private int minimumLavaPools;
        private int villageBlacksmithObsidian;
        private int taigaBlacksmithObsidian;
        private int shipwreckMagmaRavines;
        private int buriedTreasureMagmaRavines;
        private int ruinedPortalIronNuggets;
        private int bastionMaxDistanceFromOriginChunks;
        private int bastionMinimumSeparationChunks;
        private int bastionMinimumIron;
        private int bastionMinimumObsidian;
        private int stableMinimumGoodGaps;
        private int fortressMaxDistanceFromBastionChunks;
        private int barterWindowSize;
        private int barterMinimumObsidianPerWindow;
        private int barterExactPearlTradesPerWindow;
        private long filterVersion;

        private Builder(FilterProfile source) {
            name = "CUSTOM";
            enabledStartTypes = source.enabledStartTypes;
            villageMaxDistanceChunks = source.villageMaxDistanceChunks;
            shipwreckMaxDistanceChunks = source.shipwreckMaxDistanceChunks;
            desertTempleMaxDistanceChunks = source.desertTempleMaxDistanceChunks;
            ruinedPortalMaxDistanceChunks = source.ruinedPortalMaxDistanceChunks;
            buriedTreasureMaxDistanceChunks = source.buriedTreasureMaxDistanceChunks;
            minimumIron = source.minimumIron;
            ironWithDiamonds = source.ironWithDiamonds;
            minimumDiamonds = source.minimumDiamonds;
            riverMaxDistanceChunks = source.riverMaxDistanceChunks;
            minimumLavaPools = source.minimumLavaPools;
            villageBlacksmithObsidian = source.villageBlacksmithObsidian;
            taigaBlacksmithObsidian = source.taigaBlacksmithObsidian;
            shipwreckMagmaRavines = source.shipwreckMagmaRavines;
            buriedTreasureMagmaRavines = source.buriedTreasureMagmaRavines;
            ruinedPortalIronNuggets = source.ruinedPortalIronNuggets;
            bastionMaxDistanceFromOriginChunks = source.bastionMaxDistanceFromOriginChunks;
            bastionMinimumSeparationChunks = source.bastionMinimumSeparationChunks;
            bastionMinimumIron = source.bastionMinimumIron;
            bastionMinimumObsidian = source.bastionMinimumObsidian;
            stableMinimumGoodGaps = source.stableMinimumGoodGaps;
            fortressMaxDistanceFromBastionChunks = source.fortressMaxDistanceFromBastionChunks;
            barterWindowSize = source.barterWindowSize;
            barterMinimumObsidianPerWindow = source.barterMinimumObsidianPerWindow;
            barterExactPearlTradesPerWindow = source.barterExactPearlTradesPerWindow;
            filterVersion = source.filterVersion + 1;
        }

        public Builder enabled(StartType type, boolean enabled) {
            var copy = enabledStartTypes.isEmpty()
                    ? EnumSet.noneOf(StartType.class)
                    : EnumSet.copyOf(enabledStartTypes);
            if (enabled) copy.add(type); else copy.remove(type);
            enabledStartTypes = copy;
            return this;
        }

        public Builder villageMaxDistanceChunks(int value) { villageMaxDistanceChunks = value; return this; }
        public Builder minimumIron(int value) { minimumIron = value; return this; }
        public Builder minimumLavaPools(int value) { minimumLavaPools = value; return this; }
        public Builder bastionMinimumObsidian(int value) { bastionMinimumObsidian = value; return this; }
        public Builder fortressMaxDistanceFromBastionChunks(int value) { fortressMaxDistanceFromBastionChunks = value; return this; }

        public FilterProfile build() {
            return new FilterProfile(
                    name, enabledStartTypes,
                    villageMaxDistanceChunks, shipwreckMaxDistanceChunks,
                    desertTempleMaxDistanceChunks, ruinedPortalMaxDistanceChunks,
                    buriedTreasureMaxDistanceChunks, minimumIron,
                    ironWithDiamonds, minimumDiamonds, riverMaxDistanceChunks,
                    minimumLavaPools, villageBlacksmithObsidian,
                    taigaBlacksmithObsidian, shipwreckMagmaRavines,
                    buriedTreasureMagmaRavines, ruinedPortalIronNuggets,
                    bastionMaxDistanceFromOriginChunks,
                    bastionMinimumSeparationChunks, bastionMinimumIron,
                    bastionMinimumObsidian, stableMinimumGoodGaps,
                    fortressMaxDistanceFromBastionChunks, barterWindowSize,
                    barterMinimumObsidianPerWindow,
                    barterExactPearlTradesPerWindow, filterVersion
            );
        }
    }
}