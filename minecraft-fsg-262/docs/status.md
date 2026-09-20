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
- The Village path now invokes the real 26.2 `Structure.generate` API with
  the selected vanilla Village holder, `Level.OVERWORLD`, the active
  `RegistryAccess`, generator, seed-derived `RandomState`, and a vanilla
  `StructureTemplateManager`. When template loading succeeds, the adapter
  records the actual `StructureStart`, piece count, structure bounds, and
  piece bounding boxes with generation timing.
- A reusable geometry-only structure inspection result now exposes the same
  actual `StructureStart`/`StructurePiece` evidence for the other four
  Overworld start categories when requested. It does not infer blocks,
  entities, or loot.
- A bounded diagnostic benchmark reports tested, PASS, FAIL, NOT_VERIFIED,
  cancellation, elapsed time, and candidates per second without changing
  evaluator decisions.
- Nether structure inspection now uses the selected 26.2 Nether generator,
  Nether-specific random state and height access, and real Bastion or
  Fortress `Structure.generate` calls. It records actual piece geometry and
  remains geometry-only evidence.
- The analyzer now also uses the actual 26.2 `ChunkGenerator.createBiomes`
  and `fillFromNoise` pipeline to populate a bounded in-memory vanilla
  `ProtoChunk`. `GeneratedWorldInspector` scans real block states, fluid
  source blocks, and any block entities stored by those generation stages.
- MCSR configuration is integrated as a fourth native Create New World tab
  beside the vanilla Game, World, and More tabs. Settings are shared through
  `WorldCreationController` and persisted to the client config file; no
  standalone title-screen MCSR screen is used.

## Verified boundary and remaining work

1. Resolve exact 26.2 biome and structure APIs from the dependency sources.
2. Build or enter a real 26.2 `WorldGenRegion`/`ServerLevel` generation
   lifecycle before inspecting generated blocks, block entities, or loot.
   `ProtoChunk` exposes storage, but 26.2 has no standalone
   `ProtoChunk`-to-`WorldGenLevel` adapter; `WorldGenRegion` requires live
   server generation holders and a `ChunkStep`.
3. Add intended-structure candidate provenance and real seed reports.
4. Persist Create New World settings across screen instances and connect the
   coordinator to a verified 26.2 world-generation evaluator.
5. Add verified, narrow mixins for standardized drops and golem/barter behavior.

The local evaluator is wired into the Create New World search for all five seed
types and performs no network access. It now performs real placement, biome,
terrain, and optional direct structure-start/piece geometry queries against the
selected Create New World context, including Nether Bastion/Fortress geometry.
It still intentionally reports `NOT VERIFIED` for seed search because the
current world-generation adapter cannot safely inspect generated blocks, block
entities, loot, Nether routes, strongholds, End progression, or natural lava.
Placement or piece evidence is not promoted to filter acceptance; no candidate
can be accepted from incomplete evidence.
The client screen does not claim that
the current placeholder analyzer inspected Minecraft world generation. The
fallback planner produces deterministic, validated placement plans from real
adapter-supplied terrain candidates; it does not mutate playable chunks until a
26.2 world-generation hook is implemented.

The dedicated MCSR screen is a configuration/presentation layer only. Search
still uses the existing asynchronous controller and requires a Create New
World context for worldgen analysis. Search progress now reports explicit
NOT_VERIFIED counts and candidates per second. No candidate is accepted when
the adapter reports `NOT_VERIFIED`.

The following requirements remain intentionally NOT_VERIFIED rather than
fabricated: structure-template-generated block scans, block entities/chests, loot tables,
resource totals, nearby generated-structure exclusions, natural lava pools,
actual artificial lava block placement, Nether terrain/routes, Bastion and
Fortress contents, the 26.2 equivalent of the historical magma-ravine rule,
stronghold/End/Dragon verification, evidence-rich seedbank rows, and the
end-to-end accepted-seed runtime proof. Terrain blocks are now generated and
scanned through `ProtoChunk`; structure-template placement and lazy chest loot
still require the server world lifecycle described above, so those results are
not promoted to acceptance.