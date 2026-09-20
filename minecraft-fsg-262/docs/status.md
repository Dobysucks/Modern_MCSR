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
- A shared local five-type filter pipeline now evaluates Overworld facts,
  standardized bartering, and Completable evidence with PASS/FAIL/NOT_VERIFIED
  propagation.
- A Minecraft 26.2 client-side analyzer now uses the Create New World
  registries, dimension generator, `RandomState`, structure placement state,
  biome source, and base-height queries for all five structure categories.
- Cache reads and writes now reject incompatible filter or Minecraft versions.
- The 26.2 adapter now bounds placement searches by the selected profile,
  records the matching vanilla structure key, exposes placement evidence, and
  records placement/terrain/total analysis timings.
- MCSR configuration now has a dedicated native-style screen reachable from
  the title screen and as a compact indicator on Create New World. Settings
  are shared through `WorldCreationController` and persisted to the client
  config file; the vanilla Game/World/More flow remains intact.

## Verified boundary and remaining work

1. Resolve exact 26.2 biome and structure APIs from the dependency sources.
2. Implement `WorldGenerationAnalyzer` using placement, generation, loot, and
   terrain APIs without full-world loading.
3. Add intended-structure candidate provenance and real seed reports.
4. Persist Create New World settings across screen instances and connect the
   coordinator to a verified 26.2 world-generation evaluator.
5. Add verified, narrow mixins for standardized drops and golem/barter behavior.

The local evaluator is wired into the Create New World search for all five seed
types and performs no network access. It now performs real placement, biome,
and terrain queries against the selected Create New World context, but still
intentionally reports
`NOT VERIFIED` for seed search because the current world-generation adapter is
not yet capable of exact generated structure pieces, loot, block entities,
Nether route, or natural lava inspection. Placement evidence is not promoted
to generated-piece evidence. No candidate can be accepted from incomplete
evidence.
The client screen does not claim that
the current placeholder analyzer inspected Minecraft world generation. The
fallback planner produces deterministic, validated placement plans from real
adapter-supplied terrain candidates; it does not mutate playable chunks until a
26.2 world-generation hook is implemented.

The dedicated MCSR screen is a configuration/presentation layer only. Search
still uses the existing asynchronous controller and requires a Create New
World context for worldgen analysis. No candidate is accepted when the
adapter reports `NOT_VERIFIED`.