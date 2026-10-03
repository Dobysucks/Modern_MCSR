package dev.fsg262.client.world;

import java.util.List;

public record WorldCreationSettings(
        boolean enabled,
        String profileName,
        boolean completable,
        boolean standardizedRng,
        boolean disablePiglinBrutes,
        String overworldCategory,
        String netherCategory,
        Long overworldSeed,
        Long netherSeed
) {
    public static final List<String> PROFILES = List.of(
            "STRICT_RANKED_STYLE", "BALANCED", "COMPLETABLE");
    public static final List<String> OVERWORLD_CATEGORIES = List.of(
            "Village", "Shipwreck", "Desert Temple", "Ruined Portal", "Buried Treasure");
    public static final List<String> NETHER_CATEGORIES = List.of(
            "Fortress", "Stables", "Treasure", "Bridge", "Housing");

    public WorldCreationSettings {
        if (profileName == null || !profileName.matches("[A-Za-z0-9_-]{1,32}")) {
            throw new IllegalArgumentException("A valid seed profile is required");
        }
        if (!OVERWORLD_CATEGORIES.contains(overworldCategory)) {
            throw new IllegalArgumentException("A valid Overworld seed category is required");
        }
        if (!NETHER_CATEGORIES.contains(netherCategory)) {
            throw new IllegalArgumentException("A valid Nether seed category is required");
        }
    }

    public static WorldCreationSettings defaults() {
        return new WorldCreationSettings(true, PROFILES.getFirst(), true, true,
                true, OVERWORLD_CATEGORIES.getFirst(), NETHER_CATEGORIES.getFirst(),
                null, null);
    }

    public WorldCreationSettings withEnabled(boolean value) {
        return new WorldCreationSettings(value, profileName, completable, standardizedRng,
                disablePiglinBrutes, overworldCategory, netherCategory,
                overworldSeed, netherSeed);
    }

    public WorldCreationSettings withProfile(String value) {
        return new WorldCreationSettings(enabled, value, completable, standardizedRng,
                disablePiglinBrutes, overworldCategory, netherCategory, null, null);
    }

    public WorldCreationSettings withCompletable(boolean value) {
        return new WorldCreationSettings(enabled, profileName, value, standardizedRng,
                disablePiglinBrutes, overworldCategory, netherCategory,
                overworldSeed, netherSeed);
    }

    public WorldCreationSettings withStandardizedRng(boolean value) {
        return new WorldCreationSettings(enabled, profileName, completable, value,
                disablePiglinBrutes, overworldCategory, netherCategory,
                overworldSeed, netherSeed);
    }

    public WorldCreationSettings withDisablePiglinBrutes(boolean value) {
        return new WorldCreationSettings(enabled, profileName, completable, standardizedRng,
                value, overworldCategory, netherCategory, overworldSeed, netherSeed);
    }

    public WorldCreationSettings withOverworldCategory(String value) {
        return new WorldCreationSettings(enabled, profileName, completable, standardizedRng,
                disablePiglinBrutes, value, netherCategory, null, netherSeed);
    }

    public WorldCreationSettings withNetherCategory(String value) {
        return new WorldCreationSettings(enabled, profileName, completable, standardizedRng,
                disablePiglinBrutes, overworldCategory, value, overworldSeed, null);
    }

    public WorldCreationSettings withOverworldSeed(Long value) {
        return new WorldCreationSettings(enabled, profileName, completable, standardizedRng,
                disablePiglinBrutes, overworldCategory, netherCategory, value, netherSeed);
    }

    public WorldCreationSettings withNetherSeed(Long value) {
        return new WorldCreationSettings(enabled, profileName, completable, standardizedRng,
                disablePiglinBrutes, overworldCategory, netherCategory, overworldSeed, value);
    }
}
