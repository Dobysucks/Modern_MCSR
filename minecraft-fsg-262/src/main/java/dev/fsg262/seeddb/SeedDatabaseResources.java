package dev.fsg262.seeddb;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SeedDatabaseResources {
    private static final Logger LOGGER = Logger.getLogger("fsg262");

    private SeedDatabaseResources() {}

    public static SeedDatabase overworld() {
        return OverworldHolder.DATABASE;
    }

    public static SeedDatabase nether() {
        return NetherHolder.DATABASE;
    }

    private static SeedDatabase load(String name) {
        var source = SeedDatabaseResources.class.getResourceAsStream(
                "/seed-databases/" + name);
        if (source == null) throw new IllegalStateException("Missing seed database resource: " + name);
        return load(new InputStreamReader(source, StandardCharsets.UTF_8), name);
    }

    static SeedDatabase load(Reader source, String name) {
        try {
            var database = SeedDatabase.parse(source);
            if (database.malformedLineCount() > 0 || !database.declaredCountMatchesParsedCount()) {
                LOGGER.log(Level.WARNING, "Seed database {0}: loaded {1} valid entries; "
                                + "{2} malformed rows at lines {3}; declared count {4}",
                        new Object[]{name, database.size(), database.malformedLineCount(),
                                database.malformedLineNumbers(), database.declaredCount()});
            }
            return database;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read seed database resource: " + name, exception);
        }
    }

    private static final class OverworldHolder {
        private static final SeedDatabase DATABASE = load("overworld_seeds.txt");
    }

    private static final class NetherHolder {
        private static final SeedDatabase DATABASE = load("nether_seeds.txt");
    }
}
