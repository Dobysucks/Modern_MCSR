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
- Client-only FSG settings screen with title-screen entry button.
- Pausable/resumable background search handle and versioned cache constants.
- Deterministic natural-lava qualification and exactly-three fallback planner
  tests.

## Verified boundary and remaining work

1. Resolve exact 26.2 biome and structure APIs from the dependency sources.
2. Implement `WorldGenerationAnalyzer` using placement, generation, loot, and
   terrain APIs without full-world loading.
3. Add intended-structure candidate provenance and real seed reports.
4. Connect the screen's controls to a persistent config and search coordinator.
5. Add verified, narrow mixins for standardized drops and golem/barter behavior.

The client screen intentionally reports `NOT VERIFIED` and does not claim that
the current placeholder analyzer inspected Minecraft world generation. The
fallback planner produces deterministic, validated placement plans from real
adapter-supplied terrain candidates; it does not mutate playable chunks until a
26.2 world-generation hook is implemented.