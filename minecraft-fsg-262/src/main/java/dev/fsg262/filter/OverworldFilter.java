package dev.fsg262.filter;

import java.util.ArrayList;
import java.util.List;

public final class OverworldFilter {
    public SeedEvaluation evaluate(StartEvaluationInput input, FilterProfile profile) {
        if (!profile.enabledStartTypes().contains(input.startType())) {
            return new SeedEvaluation(input.seed(), input.startType(), List.of(
                    RequirementResult.fail("Start type enabled", "enabled in profile", "disabled",
                            "The selected start type is disabled by this FilterProfile.")
            ));
        }
        if (!input.intendedStructureWasSelected()) {
            return new SeedEvaluation(input.seed(), input.startType(), List.of(
                    RequirementResult.fail("Intended structure", "selected by documented candidate rule", "not selected",
                            "Nearest-structure shortcuts are not accepted by the filter.")
            ));
        }

        var requirements = new ArrayList<RequirementResult>();
        switch (input.startType()) {
            case VILLAGE -> evaluateVillage(input, profile, requirements);
            case SHIPWRECK -> evaluateShipwreck(input, profile, requirements);
            case DESERT_TEMPLE -> evaluateDesertTemple(input, profile, requirements);
            case RUINED_PORTAL -> evaluateRuinedPortal(input, profile, requirements);
            case BURIED_TREASURE -> evaluateBuriedTreasure(input, profile, requirements);
        }
        return new SeedEvaluation(input.seed(), input.startType(), requirements);
    }

    private void evaluateVillage(StartEvaluationInput i, FilterProfile p, List<RequirementResult> out) {
        out.add(distance("Village distance", i.intendedStructureDistanceChunks(), p.villageMaxDistanceChunks()));
        out.add(RequirementResult.pass("Blacksmith", "present", i.blacksmith() ? "present" : "absent",
                "Village start requires a weaponsmith, toolsmith, or armorer blacksmith-type building.")
                .withPass(i.blacksmith()));
        out.add(resourceRule(i, p));
        out.add(food(i.foodAvailable()));
        out.add(river(i.riverDistanceChunks(), p.riverMaxDistanceChunks()));
        boolean lavaRoute = i.usableLavaPools() >= p.minimumLavaPools();
        boolean obsidianRoute = i.blacksmithObsidian() >= (i.taigaVariant() ? p.taigaBlacksmithObsidian() : p.villageBlacksmithObsidian());
        out.add(RequirementResult.pass("Lava or blacksmith enter",
                "lava pools >= " + p.minimumLavaPools() + " OR blacksmith obsidian >= " +
                        (i.taigaVariant() ? p.taigaBlacksmithObsidian() : p.villageBlacksmithObsidian()),
                "lava=" + i.usableLavaPools() + ", obsidian=" + i.blacksmithObsidian(),
                "Either route is accepted; artificially guaranteed lava is tagged by the adapter.")
                .withPass(lavaRoute || obsidianRoute));
    }

    private void evaluateShipwreck(StartEvaluationInput i, FilterProfile p, List<RequirementResult> out) {
        out.add(distance("Shipwreck distance", i.intendedStructureDistanceChunks(), p.shipwreckMaxDistanceChunks()));
        out.add(resourceRule(i, p));
        out.add(food(i.foodAvailable()));
        out.add(RequirementResult.pass("Magma ravines", "eligible ravines >= " + p.shipwreckMagmaRavines(),
                Integer.toString(i.eligibleMagmaRavines()),
                "Frozen Ocean exclusions are applied by the adapter before this count.")
                .withPass(i.eligibleMagmaRavines() >= p.shipwreckMagmaRavines()));
        out.add(noBlockers(i.hasBlockingStructures()));
    }

    private void evaluateDesertTemple(StartEvaluationInput i, FilterProfile p, List<RequirementResult> out) {
        out.add(distance("Desert temple distance", i.intendedStructureDistanceChunks(), p.desertTempleMaxDistanceChunks()));
        out.add(resourceRule(i, p));
        out.add(food(i.foodAvailable()));
        out.add(RequirementResult.pass("Nearby wood", "available", i.nearbyWood() ? "available" : "missing",
                "Temple starts require a documented source of wood.")
                .withPass(i.nearbyWood()));
        out.add(river(i.riverDistanceChunks(), p.riverMaxDistanceChunks()));
        out.add(RequirementResult.pass("Usable lava pools", ">=" + p.minimumLavaPools(),
                Integer.toString(i.usableLavaPools()),
                "Only usable, non-artificial pool facts from the adapter are counted.")
                .withPass(i.usableLavaPools() >= p.minimumLavaPools()));
    }

    private void evaluateRuinedPortal(StartEvaluationInput i, FilterProfile p, List<RequirementResult> out) {
        out.add(distance("Ruined portal distance", i.intendedStructureDistanceChunks(), p.ruinedPortalMaxDistanceChunks()));
        out.add(RequirementResult.pass("Iron nuggets", ">=" + p.ruinedPortalIronNuggets(),
                Integer.toString(i.ironNuggets()),
                "Portal start excludes nuggets needed for bucket and ignition when applicable.")
                .withPass(i.ironNuggets() >= p.ruinedPortalIronNuggets()));
        out.add(food(i.foodAvailable()));
        out.add(RequirementResult.pass("Reliable ignition", "flint and steel, fire charge, or equivalent",
                i.reliableIgnition() ? "available" : "missing",
                "A portal must have a reproducible light method.")
                .withPass(i.reliableIgnition()));
        out.add(RequirementResult.pass("Nether enter route", "obsidian OR bucket",
                "obsidian=" + i.canEnterWithObsidian() + ", bucket=" + i.canEnterWithBucket(),
                "The 80/20 enter-type target is a distribution concern, not a per-seed pass condition.")
                .withPass(i.canEnterWithObsidian() || i.canEnterWithBucket()));
    }

    private void evaluateBuriedTreasure(StartEvaluationInput i, FilterProfile p, List<RequirementResult> out) {
        out.add(distance("Buried treasure distance", i.intendedStructureDistanceChunks(), p.buriedTreasureMaxDistanceChunks()));
        out.add(resourceRule(i, p));
        out.add(food(i.foodAvailable()));
        out.add(noBlockers(i.hasBlockingStructures()));
        out.add(RequirementResult.pass("Magma ravines", "eligible ravines >= " + p.buriedTreasureMagmaRavines(),
                Integer.toString(i.eligibleMagmaRavines()),
                "Frozen Ocean exclusions are applied by the adapter before this count.")
                .withPass(i.eligibleMagmaRavines() >= p.buriedTreasureMagmaRavines()));
    }

    private RequirementResult resourceRule(StartEvaluationInput i, FilterProfile p) {
        boolean pass = i.iron() >= p.minimumIron()
                || (i.iron() >= p.ironWithDiamonds() && i.diamonds() >= p.minimumDiamonds());
        return RequirementResult.pass("Iron and diamonds",
                "iron >= " + p.minimumIron() + " OR iron >= " + p.ironWithDiamonds() +
                        " AND diamonds >= " + p.minimumDiamonds(),
                "iron=" + i.iron() + ", diamonds=" + i.diamonds(),
                "Counts only explicitly allowed start resources.")
                .withPass(pass);
    }

    private RequirementResult distance(String label, double found, int maximum) {
        return RequirementResult.pass(label, "<=" + maximum + " chunks",
                String.format(java.util.Locale.ROOT, "%.3f chunks", found),
                "Inclusive chunk-distance threshold.")
                .withPass(found <= maximum);
    }

    private RequirementResult river(double found, int maximum) {
        return RequirementResult.pass("River distance", "<=" + maximum + " chunks",
                String.format(java.util.Locale.ROOT, "%.3f chunks", found),
                "Distance is measured from the intended structure center.")
                .withPass(found <= maximum);
    }

    private RequirementResult food(boolean available) {
        return RequirementResult.pass("Food", "available", available ? "available" : "missing",
                "Food may come only from documented start-area sources.")
                .withPass(available);
    }

    private RequirementResult noBlockers(boolean blockers) {
        return RequirementResult.pass("Blocking structures", "none relevant", blockers ? "found" : "none",
                "Buried treasures, shipwrecks, block entities, and dungeons are checked by the adapter.")
                .withPass(!blockers);
    }
}