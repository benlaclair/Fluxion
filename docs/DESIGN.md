# Fluxion — Power Generation Mod: Claude Code Handoff

> Working name "Fluxion" (rename freely). A Forge 1.20.1 power generation mod with tiered upgradeable generators, a GUI-based infinitely-scalable endgame generator, and wireless power transfer to machines and directly to the player's inventory (for jetpacks etc.).

---

## 1. Project Setup

- **Minecraft version:** 1.20.1
- **Mod loader:** Forge (latest stable for 1.20.1, e.g. 47.3.x)
- **Language:** Java 17
- **Build:** Gradle with ForgeGradle (use the official 1.20.1 MDK as the base)
- **Mod ID:** `fluxion` (all registry names lowercase snake_case under this namespace)
- **Energy system:** Forge Energy (FE) via `IEnergyStorage` capability — NO custom power type. Must interop out of the box with Powah, Mekanism, Thermal, jetpack mods, etc.
- **Dependencies:**
  - Required: Forge only
  - Optional/soft: JEI (recipe display), Curios (for the personal wireless receiver — implement with a fallback to chest armor slot if Curios absent)
- **From day one:** use Forge datagen for recipes, loot tables, block/item models, tags, and lang files. Config via ForgeConfigSpec (TOML) for ALL balance numbers (FE/t, buffer sizes, ranges, transfer rates, upgrade costs, scaling formula constants).

---

## 2. Core Design Principles

1. **Single functional block per machine.** No physical scaling via placing many blocks (lag concern). Multiblock allowed ONLY for the Reactor, and only as passive casing validation (casing blocks have no BlockEntity, no ticker).
2. **Tier upgrades happen via crafting recipes, not in-world placement.** Player breaks the block (or crafts the block item directly with upgrade materials) and receives the next-tier block item. **Stored energy and settings must survive the upgrade** — see the Upgrade Recipe System below.
3. **Each generator family has its own independent tier ladder** (T1→T2→T3). Tiers increase FE/t output, internal buffer, fuel efficiency, and max transfer rate.
4. **All balance numbers live in config**, never hardcoded.
5. **Performance-first:** no per-tick world scanning; cache capability references and invalidate on neighbor changes; wireless networks resolved via saved data lookups, not entity/block searches.

---

## 3. Content Plan

### 3.1 Generator Families

#### A) Combustion Generator (Tiers 1–3)
- Burns furnace fuels (respect vanilla `getBurnTime`).
- T1: baseline FE/t (suggest 40 FE/t default), small buffer (100k FE).
- T2: ~4x output, larger buffer, +25% fuel efficiency (burn time multiplier).
- T3: ~16x output, large buffer, +50% fuel efficiency.
- GUI: fuel slot, flame progress, energy bar, per-side output config (optional stretch).

#### B) Thermal Generator (Tiers 1–3)
- Consumes lava (internal fluid tank, accepts buckets + pipes via fluid capability).
- Higher output than combustion at equal tier, but fuel is a fluid (logistics cost).
- T2/T3 scale output/buffer/tank size.
- GUI: tank display, energy bar, bucket slot.

#### C) Reactor (Tiers 1–3) — the risk/reward generator
- Consumes **fuel rod items** (new item, craftable; add a depleted rod byproduct) plus **coolant** (water or a custom coolant fluid in a tank).
- Highest passive output of the three families.
- **Heat mechanic:** running generates heat; coolant dissipates it. If coolant runs dry, heat climbs; at max heat → meltdown (configurable: explosion, or safer default = machine damages itself / drops to a "scrammed" state requiring a repair item). Meltdown severity in config.
- Optional casing multiblock: reactor core is the single functional block; requires N passive casing blocks in a 3x3x3 shell to run at full output (structure check on placement / neighbor change only — never per tick). Casings = plain Blocks, no BE.
- GUI: fuel rod slots, coolant tank, heat bar, energy bar, scram button.

#### D) Singularity Core (Tier 4 / endgame) — GUI-scaled infinite generator
- **Single block. All scaling is virtual — data in the BlockEntity, nothing placed in world.**
- Right-click opens a GUI showing a **segment construction interface** (Dyson-Sphere-Program-style): a grid/ring of segments (e.g. shells of 10 segments each, unlimited shells).
- Player feeds resources into a build queue (input slots in the GUI). Each completed segment permanently increases FE/t output.
- **Scaling formula (config-driven):**
  - `output = baseOutput * segments` (linear per segment)
  - `costPerSegment = baseCost * growthFactor ^ shellIndex` (cost grows per shell so it is infinite-but-diminishing, like Dyson Cube)
  - Defaults to tune later: baseOutput 1,000 FE/t per segment, baseCost e.g. 64 of a mid-tier component, growthFactor 1.5.
- Segment progress stored as simple ints/longs in BE NBT → zero performance cost regardless of "size."
- Requires a crafted **Singularity Catalyst** item to place/activate (endgame recipe gated behind T3 materials from all three generator families — makes the whole mod's progression converge here).
- GUI: segment map visual, current output, next-segment cost, input/build queue slots, total energy generated stat.

### 3.2 Energy Storage & IO (supporting blocks)
- **Energy Cell** (Tiers 1–3): simple battery block, upgradeable like generators. Needed so players can buffer and test. Per-side input/output config.
- (Cables are OUT OF SCOPE — players use pipes from other mods; everything exposes `IEnergyStorage` on all sides per its IO config.)

### 3.3 Wireless Power — Block Network
- **Wireless Transmitter (Tiers 1–3):** attaches to any energy source (pulls from adjacent inventories/its own buffer fed by pipes) and pushes FE to linked Receivers.
  - T1 range: 16 blocks; T2: 64; T3: entire dimension. Transfer rate scales per tier. All in config.
- **Wireless Receiver (Tiers 1–3):** outputs FE to adjacent blocks. Tier gates max accepted rate.
- **Linking Tool item ("Flux Linker"):** shift-click transmitter to copy its network frequency, click receivers to bind. Networks are per-player-owned frequencies stored in world `SavedData` (dimension + BlockPos registry). No chunk scanning: transmitter iterates its bound receiver list; skip unloaded chunks gracefully.
- Cross-dimension transfer: **only** via the T3 Personal system and/or a T3 transmitter upgrade ("Dimensional Antenna" upgrade item, config-gated).

### 3.4 Wireless Power — Personal (the jetpack use case)
- **Personal Flux Receiver (Tiers 1–3):** wearable item. Curios slot if Curios is loaded, otherwise chestplate-compatible fallback or held-in-inventory functionality (charging works from anywhere in inventory — simplest and most useful; do this).
- Bound to a transmitter network with the Flux Linker.
- Each tick (or every N ticks, config), pulls FE from its bound network and **distributes to energy-accepting items in the player's inventory** (query each ItemStack for the energy capability; jetpacks, tools, etc. charge automatically).
- Tiers:
  - **T1 Local:** works within 32 blocks of the bound transmitter, low rate.
  - **T2 Regional:** works anywhere in the same dimension, medium rate.
  - **T3 Universal:** works across all dimensions, high rate. Requires Singularity-tier materials to craft.
- Charging priority: armor/curios first, then hotbar, then main inventory (config toggle for "charge held item only").

### 3.5 Materials & Progression Items (minimal set)
- Flux Ingot / Flux Alloy (base crafting material — recipe from vanilla materials, no new world gen ore to keep scope down; optionally add ore later)
- Machine Frame (T1/T2/T3 — the tier-defining component in every upgrade recipe)
- Fuel Rod + Depleted Fuel Rod
- Coolant (fluid, optional — water acceptable for v1)
- Dimensional Antenna (upgrade item)
- Singularity Catalyst (endgame)
- Flux Linker (tool)

---

## 4. The Upgrade Recipe System (build this early — it's the backbone)

**Requirement:** crafting a T1 machine block item + upgrade materials → T2 machine block item, **preserving NBT** (stored energy, tank contents, settings, network binding).

Implementation:
- Custom recipe type (`fluxion:tier_upgrade`) with a custom `Recipe` class + serializer, JSON-driven via datagen: `{ base: item, ingredients: [...], result: item }`.
- In `assemble()`, locate the base machine ItemStack in the crafting grid, copy its `BlockEntityTag` NBT to the result stack (energy, fluids, settings — but recompute tier-derived caps on placement: e.g. stored energy carries over, max capacity comes from the new tier).
- Blocks must implement proper NBT save-to-item on break (loot table with `copy_nbt` / override `getDrops`) so break→carry→re-place also preserves state.
- JEI plugin: register a category or ensure these render in the crafting category so upgrade paths are discoverable.
- Write this ONCE as a generic system; every machine family and the personal receiver reuse it.

---

## 5. Technical Architecture Notes

- **Registration:** DeferredRegister for blocks, items, block entities, menus, recipe types/serializers, creative tab.
- **BlockEntities:** one base class `AbstractFluxMachineBE` handling: energy storage (with configurable IO per side), tier field, NBT save/load, capability caching + invalidation, sync to client (for GUIs) via `ContainerData`/packets. Generator families extend it.
- **Tiers:** an enum or record `MachineTier(int fePerTick, long buffer, int transferRate, ...)` populated FROM CONFIG at load; blocks/items carry a tier reference. Prefer **one block registration per tier** (fluxion:combustion_generator_t1, _t2, _t3) over NBT-tier-on-one-block — simpler models, loot, recipes.
- **GUIs:** AbstractContainerMenu + Screen per machine family (tiers share the menu class, parameterized).
- **Networking:** SimpleChannel packets for: Singularity Core segment build requests, scram button, side-config changes.
- **Wireless network data:** `SavedData` per level (or one global for cross-dim), mapping `frequencyId -> {ownerUUID, transmitterPos+dim, Set<receiverPos+dim>}`. Personal receivers store frequencyId in item NBT.
- **Player charging tick:** a single server-side `PlayerTickEvent` handler (every N ticks) — check for Personal Flux Receiver, resolve network, validate tier range rules, transfer FE. Keep it O(inventory size), no world queries beyond the transmitter BE lookup (skip if chunk unloaded, except T2+ which draw from the network's buffered transmitter only if loaded — document this limitation in tooltip, or give transmitters a small always-works internal buffer strategy: transmitter must be chunkloaded to generate, but its buffer snapshot is stored in SavedData so remote draw works. Pick the simpler option for v1: require transmitter chunk to be loaded; note it in the item tooltip).

---

## 6. Milestones (build in this order)

**M1 — Skeleton + first energy flow**
Mod scaffolding, config system, creative tab, Flux Ingot/Machine Frames, Combustion Generator T1, Energy Cell T1. Verify FE flows into the cell and into a third-party mod's machine in a dev environment.

**M2 — Tier upgrade system**
Custom upgrade recipe type + NBT preservation + break/re-place state retention. Apply to Combustion T1→T2→T3 and Energy Cell. Datagen for all recipes. JEI showing.

**M3 — Thermal + Reactor**
Thermal generator with fluid tank (all tiers). Reactor with fuel rods, heat, coolant, scram/meltdown, optional passive casing check (all tiers).

**M4 — Wireless block network**
Transmitter/Receiver blocks (all tiers), Flux Linker, SavedData network registry, range/rate enforcement, cross-dim antenna upgrade.

**M5 — Personal wireless receiver**
Wearable/inventory item, Curios soft-dependency, player tick charging into inventory items, tier range rules (local/dimensional/universal).

**M6 — Singularity Core**
Block + GUI segment interface, build queue, scaling formula from config, Singularity Catalyst gating recipe.

**M7 — Polish pass**
Full datagen audit (models, lang, loot, tags), JEI categories, tooltips (show FE/t, range, tier, network binding), sounds/particles for generators, config documentation, `/fluxion` debug command (dump network info).

Each milestone should end in a runnable dev-client test. Write brief testing instructions per milestone.

---

## 7. Default Balance Numbers (starting points — all config)

| Machine | T1 | T2 | T3 |
|---|---|---|---|
| Combustion FE/t | 40 | 160 | 640 |
| Thermal FE/t | 80 | 320 | 1,280 |
| Reactor FE/t | 200 | 800 | 3,200 |
| Buffers | 100k | 400k | 1.6M |
| Transmitter range | 16 | 64 | dimension |
| Personal receiver rate (FE/t) | 250 | 1,000 | 5,000 |
| Singularity: per segment | 1,000 FE/t | — | — |
| Singularity growth factor | 1.5x cost per shell (10 segments/shell) | | |

---

## 8. Out of Scope (v1)
- Cables/pipes (rely on other mods)
- Custom world gen / ores
- Custom jetpack (this mod powers existing ones)
- Client-side fancy renders for the Core (GUI-only visualization is fine for v1)
