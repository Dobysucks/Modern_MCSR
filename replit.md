# FSG Seed Types 26.2

Independent Fabric 26.2 speedrunning mod with reproducible 1.16.1-style seed
types, explicit filters, and deterministic practice RNG.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server (port 5000)
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

## Where things live

- `minecraft-fsg-262/` — standalone Fabric mod project.
- `minecraft-fsg-262/docs/filter-spec.md` — exact thresholds and pass rules.
- `minecraft-fsg-262/docs/architecture.md` — adapter boundary and coordinate model.
- `minecraft-fsg-262/docs/design.md` — pre-implementation design record.
- `minecraft-fsg-262/src/main/java/dev/fsg262/filter/` — pure filter engine.
- `minecraft-fsg-262/src/main/java/dev/fsg262/rng/` — deterministic RNG/bartering.

## Architecture decisions

- The mod uses Mojang mappings for 26.2; Yarn is not used for 26.1+ targets.
- World-generation facts enter through `WorldGenerationAnalyzer`; pure rules do
  not load complete worlds or choose arbitrary nearest structures.
- Strict profile thresholds are immutable and separate from custom settings.
- Standardized RNG is opt-in per mechanic and never replaces Minecraft globally.

## Product

The mod exposes explicit start-type filter evaluation, auditable seed reports,
deterministic indexed bartering, coordinate utilities, and a future path to
fast external seed scanning.

## User preferences

_Populate as you build — explicit user instructions worth remembering across sessions._

## Gotchas

- Minecraft 26.2 requires Java 25 and the official Fabric toolchain values in
  `minecraft-fsg-262/gradle.properties`.
- Structure commands intentionally report `TODO — NEEDS VERIFICATION` until
  the exact 26.2 world-generation adapter is implemented.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
