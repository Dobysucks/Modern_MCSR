# Architecture and verification plan

## Product boundary

FSG Seed Types 26.2 is a single-player/practice speedrunning environment. It
does not implement Elo, matchmaking, or multiplayer ranking.

The design is split into four layers:

1. **Minecraft adapter** — obtains 26.2 biome, structure placement, structure
   generation, loot, and terrain facts.
2. **Pure filter engine** — evaluates those facts against an immutable
   `FilterProfile`. It has no Minecraft dependency.
3. **Deterministic RNG engine** — produces standardized speedrun sequences from
   an explicit seed and index.
4. **Presentation and persistence** — commands, debug reports, config, and
   versioned JSON seed records.

The adapter is an interface because filtering a million seeds must not load a
complete playable world for every candidate. The intended pipeline is:

```text
seed
  -> biome generation
  -> intended structure placement
  -> structure-specific generation
  -> loot/resource analysis
  -> terrain/path analysis
  -> pure FilterProfile evaluation
```

## 26.2 compatibility surface

The build uses the official Fabric 26.2 template values:

- Minecraft `26.2`
- Fabric Loader `0.19.5`
- Fabric API `0.161.0+26.2`
- Loom `1.17-SNAPSHOT`
- Mojang mappings (Yarn is not used for 26.1+)
- Java `25`

The Fabric entrypoint, command callback, client screen, and screen accessor are
the game-facing code in the current slice. The following adapter work is
deliberately isolated and must be implemented against locally resolved 26.2
names before merging:

- biome distance sampling;
- intended structure placement and variant identification;
- loot table analysis;
- terrain/open-path sampling;
- structure-specific block entity and obstruction checks;
- persistent client configuration and RNG mixins.

The FSG screen is deliberately non-blocking and uses the background search
boundary, but its START action remains a visible `NOT VERIFIED` status until a
real world-generation analyzer is supplied. This prevents a UI from turning
sentinel structure facts into accepted seeds.

## Coordinate model

- Overworld start is represented as a block coordinate. Distances to start
  structures are measured in chunks using floor division and chunk-center
  coordinates.
- Nether origin is the block coordinate `(0, 0)`, represented in the Nether
  dimension.
- Bastion and fortress origins are represented as chunk coordinates.
- Region coordinates use 32x32 chunks, with mathematical floor division so
  negative coordinates are correct.
- Conversion is explicit: `16 blocks = 1 chunk`, `32 chunks = 1 region`.

The filter never compares a block value to a chunk threshold without first
converting units.

## Intended structure selection

The adapter must return the intended structure, not the nearest arbitrary
structure. For each start type it must record the selection rule, candidate
list, and rejected candidates. The pure engine only accepts an
`IntendedStructure` record with that provenance.

The same rule applies in the Nether: `NetherFilter` receives the intended
bastion and fortress plus competing bastions, rather than selecting the first
structure returned by a search.

## Uncertainties to resolve before game hooks

- The attached brief documents numeric requirements but does not identify a
  canonical public algorithm for every Ranked-style coordinate candidate
  enumeration.
- “Good gap” requires a stable terrain definition. It is represented by
  `BastionGapAnalyzer.GapCriteria` and is not reduced to a vague boolean.
- 26.2's unobfuscated Mojang names must be resolved locally for structure
  placement and loot APIs.
- Historical 1.16.1 behavior must be verified against technical sources before
  changing vanilla drop or barter behavior.

Unverified behavior is marked `TODO — NEEDS VERIFICATION` in code and reports.