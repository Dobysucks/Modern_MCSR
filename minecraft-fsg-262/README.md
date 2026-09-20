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

## Legal scope

This is an independent implementation. It does not copy source code or
proprietary implementation details from MCSR Ranked or FSG. Publicly stated
numeric requirements are represented as documented filter inputs and rules.