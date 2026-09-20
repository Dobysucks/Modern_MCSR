# Implementation status

## Complete

- Fabric 26.2 project metadata using official Mojang-mapping-era template values.
- Java 25 build configuration and Gradle wrapper.
- Strict, balanced, and custom filter profile models.
- Five start types and exact pure-engine overworld rules from the brief.
- Nether bastion/fortress rule model.
- Explicit stables gap analyzer.
- Unit-aware coordinate utilities.
- Deterministic legacy/modern RNG abstractions.
- Indexed 72-barter sequence with the requested guarantees.
- Debug replay output.
- Seed record model and JSON schema.
- All current unit tests, `./gradlew test`, and `./gradlew build`.
- Initial `/speedrun` command tree.

## Next implementation slice

1. Resolve exact 26.2 biome and structure APIs from the dependency sources.
2. Implement `WorldGenerationAnalyzer` using placement, generation, loot, and
   terrain APIs without full-world loading.
3. Add intended-structure candidate provenance and real seed reports.
4. Add the config screen and persistent config serialization.
5. Add verified, narrow mixins for standardized drops and golem/barter behavior.

Until those steps are complete, structure commands fail or report
`TODO — NEEDS VERIFICATION` instead of returning fabricated seed results.