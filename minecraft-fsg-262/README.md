# FSG Seed Types 26.2

An independent Fabric mod for Minecraft Java Edition 26.2. It brings the
explicit, reproducible seed-type philosophy of 1.16.1 speedrunning into the
modern game without copying MCSR Ranked or FSG source code.

## Status

The first implementation establishes:

- a Fabric 26.2 / Mojang mappings project;
- strict, balanced, and custom filter profiles;
- exact unit-aware distance utilities;
- testable overworld and Nether filter evaluators;
- an explicit stables `BastionGapAnalyzer`;
- deterministic legacy-style speedrun RNG and piglin bartering;
- replayable seed and barter reports;
- the seed database record format and filter versioning model.
- a native MCSR Create World tab backed by bundled Overworld and Nether seed
  databases, with independent selection and per-profile consumed-seed state.

The Minecraft world-generation adapter is intentionally isolated behind
`WorldGenerationAnalyzer`. This keeps the filter engine testable without
loading a complete world and prevents unverified 26.2 mappings from being
presented as working code.

## Build

This project requires Java 25, Gradle 9.5.1, and the Fabric 26.2 toolchain.

```bash
./gradlew test
./gradlew build
```

The Replit image used to author this project currently ships Java 19. The
Gradle build therefore fails early with the correct Java requirement until a
Java 25 runtime is selected.

## Commands

The Fabric entrypoint registers:

```text
/speedrun version
/speedrun config
/speedrun rng
/speedrun barter
/speedrun seedinfo <seed>
/speedrun filter <seed>
/speedrun filterrange <start> <end>
```

Structure analysis commands return an explicit verification status until a
26.2 `WorldGenerationAnalyzer` implementation is wired to Minecraft's modern
biome, structure, loot, and terrain APIs.

## MCSR seed databases

The supplied adapted 26.2 seed lists are packaged unchanged as
`seed-databases/overworld_seeds.txt` and `seed-databases/nether_seeds.txt`.
They are parsed lazily once when the MCSR tab first needs a dimension list.
Selections are temporary reservations; a seed is consumed only after the
integrated server reports that the MCSR world has started. Cancelled or failed
creation releases the reservation without persisting consumption. Committed
membership is stored as a compact bitmap under
`config/consumed-seeds/<dimension>/<profile>.bitmap`; selection does not
rewrite or filter either bundled source file. The selected Overworld seed is
the vanilla world seed. Both `mcsr_overworld_seed` and `mcsr_nether_seed` are
saved separately in the world's level data. Malformed database rows are
skipped with a warning containing the resource name and affected line numbers;
the remaining valid seeds stay available.
The MCSR tab selects candidates by the bundled row tags (Overworld Village,
Shipwreck, Desert Temple, Ruined Portal, or Buried Treasure; Nether Fortress,
Stables, Treasure, Bridge, or Housing). The signed seed values are displayed
for information only; there are no editable per-dimension seed inputs.

MCSR worlds populate supported vanilla village, shipwreck, desert pyramid,
ruined portal, buried treasure, and bastion chests when their inventory is
first opened. The shared `RandomizableContainer.unpackLootTable` hook replaces
only the matching vanilla lazy loot table with structure-weighted speedrun
resources, writes randomized stacks directly into the chest block entity's
backing inventory, and persists an initialization marker only after a
nonempty write succeeds. Standardized mode derives both item/count selection
and slot placement from the dimension's accepted MCSR seed, chest position,
and loot-table identity; ordinary worlds and unsupported loot tables continue
through vanilla unchanged.

Standardized MCSR worlds use the existing indexed 72-barter model for real
Piglin barter responses. Each window deterministically emits exactly three
pearl trades and at least six obsidian trades, keyed by the seed for the
Piglin's dimension. Other results come from the modern vanilla barter table,
seeded per trade from the same MCSR stream; non-pearl trades remain intact,
and extra vanilla pearl results are replaced to preserve the three-pearl
window. Non-MCSR worlds continue to use the vanilla response unchanged, and
the MCSR barter index is saved with world data. The 26.2
`1.16.1 Ender Pearl Rates` MIT reference (pearl weight 23) was reviewed as a
behavior reference; no runtime dependency or global loot-table override is
used.

## Legal scope

This is an independent implementation. It does not copy source code or
proprietary implementation details from MCSR Ranked or FSG. Publicly stated
numeric requirements are represented as documented filter inputs and rules.