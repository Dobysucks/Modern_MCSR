package dev.fsg262.persistence;

import dev.fsg262.filter.StartType;
import java.util.Map;

public record SeedRecord(
        String seed,
        String minecraftVersion,
        String filterVersion,
        StartType startType,
        Map<String, Object> spawn,
        Map<String, Object> overworld,
        Map<String, Object> nether,
        Map<String, Object> bastion,
        Map<String, Object> fortress,
        String rngSeed,
        Map<String, Object> requirements,
        boolean passed
) {
    public SeedRecord {
        spawn = Map.copyOf(spawn);
        overworld = Map.copyOf(overworld);
        nether = Map.copyOf(nether);
        bastion = Map.copyOf(bastion);
        fortress = Map.copyOf(fortress);
        requirements = Map.copyOf(requirements);
    }
}