package dev.fsg262.search;

import dev.fsg262.completion.VerificationStatus;
import dev.fsg262.filter.SeedTypeChoice;
import dev.fsg262.filter.UnverifiedWorldGenerationAnalyzer;
import dev.fsg262.filter.FilterProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalSeedFilterTest {
    @Test
    void allFiveSeedTypesUseTheSameExplicitLocalPipeline() {
        var filter = new LocalSeedFilter(new UnverifiedWorldGenerationAnalyzer());
        for (var type : new SeedTypeChoice[]{
                SeedTypeChoice.VILLAGE, SeedTypeChoice.SHIPWRECK,
                SeedTypeChoice.DESERT_TEMPLE, SeedTypeChoice.RUINED_PORTAL,
                SeedTypeChoice.BURIED_TREASURE
        }) {
            var result = filter.evaluate(123L, 123L, type,
                    FilterProfile.strictRankedStyle(), true);
            assertEquals(VerificationStatus.NOT_VERIFIED, result.status(), type.name());
            assertEquals(VerificationStatus.NOT_VERIFIED, result.rng());
        }
    }
}
