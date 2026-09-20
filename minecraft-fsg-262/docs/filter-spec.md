# Filter specification

Filter version: `fsg262-filter-0.1.0`

Every rule has an exact threshold, pass condition, explanation, and report
label. The strict profile below is copied from the supplied project brief as a
specification, not inferred from code.

## Overworld start types

| Start | Distance | Resource rule | Additional requirements |
| --- | ---: | --- | --- |
| Village | `<= 7 chunks` | `iron >= 7 OR (iron >= 4 AND diamonds >= 3)` | blacksmith; food; river `<= 6`; three usable lava pools, or blacksmith obsidian `>= 8`; taiga obsidian `>= 10` |
| Shipwreck | `<= 4 chunks` | same iron/diamond rule | food; two eligible magma ravines `<= 10`; no blocking structures |
| Desert temple | `<= 5 chunks` | same iron/diamond rule | food; wood; river `<= 6`; three usable lava pools |
| Ruined portal | `<= 3 chunks` | `iron nuggets >= 18` | food; ignition; enter via obsidian or bucket; enter distribution target `80/20` where applicable |
| Buried treasure | `<= 5 chunks` | same iron/diamond rule | food; no blocking structures; two eligible magma ravines `<= 10` |

Distance thresholds are inclusive. Iron from allowed chests, golems, and
documented starting resources is already aggregated before evaluation.

## Nether

- Intended bastion must be within `14 chunks` of Nether `(0, 0)`.
- It must beat the competing-bastion threshold by at least `10 chunks` under
  the documented candidate distance calculation.
- Loot must contain at least `3 iron` and `5 obsidian`.
- The terrain/open-path requirement is a separate pass/fail rule.
- Stables require `2 good gaps`, or `1 good gap` plus one triple-chest rampart.
- Intended fortress origin must be within `16 chunks` of the intended bastion,
  and its pathability requirement is separate.

## RNG

- Default RNG seed is the Overworld seed.
- A separate explicit RNG seed is supported.
- Standardized barter output is indexed from zero.
- Each `72`-barter window must contain at least `6 obsidian` results and
  exactly `3 ender-pearl trades`.
- The sequence is deterministic for `(rngSeed, barterIndex)`.

The current sequence generator uses a stable SplitMix64-derived stream and
documents its deterministic mapping in `LegacyPiglinBartering`. It is an
independent implementation of the supplied sequence constraints. Historical
loot weights remain a verification item until a public technical specification
is attached.