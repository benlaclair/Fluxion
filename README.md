# Fluxion

A Forge **1.20.1** power generation mod: tiered upgradeable generators, a GUI-scaled
infinite endgame generator (Singularity Core), and wireless power transfer to machines
and directly into the player's inventory (jetpacks etc.).

Uses **Forge Energy (FE)** exclusively — interops out of the box with Powah, Mekanism,
Thermal, jetpack mods, and anything else speaking `IEnergyStorage`.

## Status

| Milestone | Contents | Status |
|---|---|---|
| **M1 — Skeleton + first energy flow** | Mod scaffolding, server config, creative tab, Flux Ingot, Machine Frames T1–T3, Combustion Generator T1–T3, Energy Cell T1–T3, full datagen | ✅ done |
| M2 — Tier upgrade system | `fluxion:tier_upgrade` recipe type with NBT preservation, break/re-place state retention, JEI | ⬜ |
| M3 — Thermal + Reactor | Fluid-fueled thermal generator; reactor with fuel rods, heat, coolant, scram/meltdown | ⬜ |
| M4 — Wireless block network | Transmitter/Receiver blocks, Flux Linker, SavedData network registry | ⬜ |
| M5 — Personal wireless receiver | Inventory-charging wearable, Curios soft-dep, tier range rules | ⬜ |
| M6 — Singularity Core | GUI segment construction, config-driven scaling formula, Singularity Catalyst | ⬜ |
| M7 — Polish | JEI categories, tooltips, sounds/particles, `/fluxion` debug command | ⬜ |

## Building

Requires Java 17+ on PATH (the Gradle toolchain will fetch JDK 17 automatically if needed).

```bash
./gradlew runData      # generate recipes/models/lang/loot/tags into src/generated/resources
./gradlew runClient    # launch the dev client
./gradlew build        # build the release jar into build/libs/
```

Run `runData` once before `runClient` the first time (and again after changing any
datagen provider) — models, recipes, loot tables, tags, and lang files are all generated.

## Testing M1

1. `./gradlew runData`, then `./gradlew runClient`, create a world.
2. The **Fluxion** creative tab has: Flux Ingot, Machine Frames T1–T3, Combustion
   Generators T1–T3, Energy Cells T1–T3.
3. Place a Combustion Generator, right-click, drop coal in the fuel slot → flame burns,
   energy bar fills, block front glows and emits light.
4. Place an Energy Cell against any face of the generator → the generator pushes FE
   into it; open the cell to watch it fill.
5. Hoppers can feed fuel into the generator (item capability is exposed).
6. Interop check: drop any FE-consuming mod's jar (e.g. Powah) into `run/mods` and
   confirm the generator charges its machines directly.

All balance numbers (FE/t, buffers, transfer rates, fuel efficiency) live in the
**server config**: `<world>/serverconfig/fluxion-server.toml`.

Notes for M1:
- T2/T3 machines currently use placeholder shapeless upgrade recipes (machine + frame +
  flux ingots). M2 replaces these with the custom NBT-preserving upgrade recipe type.
- The Energy Cell is passive (accepts and provides FE on all sides); per-side IO config
  comes later.

## Design notes

Core principles from the project plan: single functional block per machine, tier
upgrades via crafting (never in-world placement), all balance numbers in config,
performance-first (no per-tick scanning, cached capability references, SavedData-backed
wireless networks).
