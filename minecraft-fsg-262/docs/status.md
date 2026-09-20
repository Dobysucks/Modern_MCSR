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
- Client-only FSG controls embedded in the Create New World screen.
- Pausable/resumable background search handle and versioned cache constants.
- Deterministic natural-lava qualification and exactly-three fallback planner
  tests.
- Native Create New World controls and an exact-seed handoff gate; the former
  Atum abstraction has been removed.

## Verified boundary and remaining work

1. Resolve exact 26.2 biome and structure APIs from the dependency sources.
2. Implement `WorldGenerationAnalyzer` using placement, generation, loot, and
   terrain APIs without full-world loading.
3. Add intended-structure candidate provenance and real seed reports.
4. Persist Create New World settings across screen instances and connect the
   coordinator to a verified 26.2 world-generation evaluator.
5. Add verified, narrow mixins for standardized drops and golem/barter behavior.

The client screen intentionally reports `NOT VERIFIED` for seed search and does not claim that
the current placeholder analyzer inspected Minecraft world generation. The
fallback planner produces deterministic, validated placement plans from real
adapter-supplied terrain candidates; it does not mutate playable chunks until a
26.2 world-generation hook is implemented.