# FSG Seed Types 26.2 design record

This document is the pre-implementation design record required by the supplied
brief. It is intentionally independent of MCSR Ranked and FSG source code.

## 1. Exact architecture

`WorldGenerationAnalyzer` is the Minecraft-facing boundary. It produces
observed facts for one intended start structure. `OverworldFilter` and
`NetherFilter` evaluate those facts without loading a complete world.
`StandardizedSpeedrunRng` and `LegacyPiglinBartering` provide explicit,
indexed deterministic streams. Commands and future config screens are
presentation layers. `SeedRecord` is the versioned persistence boundary.

## 2. 26.2 classes and APIs

Verified against the official Fabric 26.2 documentation and example project:

- `net.fabricmc.api.ModInitializer`
- `net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback`
- `net.minecraft.commands.Commands`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.network.chat.Component`
- Brigadier `CommandDispatcher`, `CommandContext`, and `LongArgumentType`

The following are not claimed as verified until the 26.2 Minecraft dependency
is inspected for exact names and signatures:

- biome source and noise sampling;
- structure placement and structure-set candidate enumeration;
- structure template/variant identification;
- structure loot generation;
- terrain and open-path sampling;
- golem, piglin, blaze, pearl, hoglin, animal, flint, and suspicious-stew
  behavior injection points;
- client config screen classes.

No speculative mixins are included.

## 3. Historical 1.16.1 behavior target

The project brief identifies these compatibility targets:

- classic five filtered start types;
- fixed four-iron golem drop;
- documented food, iron, diamond, obsidian, river, lava, and ravine checks;
- deterministic piglin barter indexing;
- indexed standardized mob-drop RNG hooks.

The numeric start and Nether requirements are encoded in
`FilterProfile.strictRankedStyle()`. Historical loot weights and any mechanic
not numerically specified in the brief remain `TODO — NEEDS VERIFICATION`.

## 4. Complete filter specification

The complete threshold table is in `docs/filter-spec.md`. Each runtime
requirement produces a `RequirementResult` with label, pass/fail state,
expected threshold, observed value, and explanation.

## 5. RNG specification

The default RNG seed is the Overworld seed. A separate configured seed is
accepted. `StandardizedSpeedrunRng` selects a named legacy or modern stream
without replacing Minecraft's global random state.

Piglin bartering is indexed from zero. Each 72-barter window uses a deterministic
permutation to guarantee exactly three pearl trades and at least six obsidian
trades. The output item, quantity, gold input, seed, and index are all recorded
in `BarterResult`.

## 6. Data structures

- `FilterProfile`: immutable thresholds and enabled start types.
- `StartEvaluationInput`: adapter-observed overworld facts.
- `NetherEvaluationInput`: adapter-observed bastion/fortress facts.
- `RequirementResult`: auditable rule result.
- `SeedEvaluation` / `NetherEvaluation`: reportable evaluation results.
- `GapCriteria` / `GapObservation`: explicit stables terrain contract.
- `SeedRecord`: versioned accepted-seed JSON shape.
- `RNGMechanicDefinition`: future standardized-drop contract.

## 7. Test plan

The automated suite covers:

- block/chunk/region conversions, including negative coordinates;
- inclusive overworld distance and resource thresholds;
- village river, lava, blacksmith, and taiga routes;
- shipwreck and buried-treasure ravine thresholds;
- desert temple wood/river/lava requirements;
- ruined portal nuggets and enter route;
- bastion origin, separation, loot, terrain, and stables gap rules;
- fortress distance and terrain;
- deterministic barter sequences at 72, 144, 216, and 1008 results;
- equal-seed equality and different-seed inequality.

## 8. Known uncertainties

The supplied brief does not define the exact public candidate-enumeration
algorithm for intended structures, the full stables gap geometry, or historical
loot-weight tables. Those are explicit adapter/specification work items, not
assumptions hidden behind a generic “good seed” predicate.
