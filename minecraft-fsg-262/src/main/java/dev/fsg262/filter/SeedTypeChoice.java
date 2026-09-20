package dev.fsg262.filter;

import java.util.Set;

public enum SeedTypeChoice {
    ANY,
    VILLAGE,
    SHIPWRECK,
    DESERT_TEMPLE,
    RUINED_PORTAL,
    BURIED_TREASURE;

    public Set<StartType> startTypes() {
        return this == ANY
                ? Set.of(StartType.values())
                : Set.of(StartType.valueOf(name()));
    }
}