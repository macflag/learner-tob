# Learner ToB — Project Brief

A RuneLite plugin that coaches players through Theatre of Blood (ToB) by checking
their gear/setup and giving room-by-room prompts and on-floor markers. Live on the
RuneLite Plugin Hub.

## Who you're working with

Jake is a **beginner coder**. Explain reasoning in plain terms, avoid unexplained
jargon, and don't assume deep Java or Gradle knowledge. He knows OSRS and ToB
mechanics extremely well — that domain knowledge comes from him; the code craft
comes from you. When something is ambiguous, ask rather than guess, especially for
in-game coordinates and mechanics.

## Environment

- **IDE:** IntelliJ IDEA, with Claude Code running in the IDE terminal.
- **JDK:** Temurin 11 (Java 11). Don't use language features newer than Java 11.
- **Build:** Gradle (the RuneLite plugin template). You can compile to check your work.
- **OS:** Windows.

## Repo & distribution

- **GitHub:** `macflag/learner-tob`
- **Status:** v1.1 is LIVE on the Plugin Hub. Everything past v1.1 is unreleased working copy.
- **License:** BSD 2-Clause. Keep the existing license header on every source file.
- **Package:** `com.learnertob`, all sources under `src/main/java/com/learnertob/`.

## Source files

- `LearnerTobPlugin.java` — main plugin: game-tick logic, zone triggers, Maiden room engine, overlay wiring.
- `LearnerTobConfig.java` — settings (sections: General, Raid checklist, Maiden, Popup, Testing).
- `LearnerTobPanel.java` — sidebar visualizer.
- `GearCheckOverlay.java` — screen-space popup (extends OverlayPanel). DismissMode: CLICK / TIMED / COMPLY.
- `TileMarkerOverlay.java` — world overlay (ABOVE_SCENE) that draws floor tile boxes and NPC true-tile markers.
- `ZoneTrigger.java` — proximity/zone framework with directional entry.
- `Presets.java` — gear loadouts per role/setup.
- `Role.java`, `SlotReq.java`, and small enums (overlay font size/position).

## Working conventions

- **Verify RuneLite API against real source, not memory.** Before using any client/API
  method, confirm its signature against the actual RuneLite GitHub source
  (raw.githubusercontent.com / the runelite repo). The API has many deprecations and
  subtle instance-coordinate behaviors.
- **Keep braces balanced** and compile after non-trivial edits.
- **Edit files in place** — that's the whole point of using you here. (In the separate
  web chat, the convention was "deliver complete files only"; that does NOT apply to you.
  You should make direct edits.)
- **Don't chase deprecation warnings** unless a future RuneLite update actually breaks the
  build. Jake ships with the existing warnings on purpose.

## Domain model — roles & setups

Four roles (config dropdown), internal enum names in parens:

- **MDPS** (`MELEE`) — melee DPS
- **RDPS** (`RANGED`) — ranged DPS
- **NFRZ** (`NORTH_FREEZE`) — north freezer
- **SFRZ** (`SOUTH_FREEZE`) — south freezer

MDPS/RDPS have a **scythe** vs **no-scythe** setup (auto-detected from gear). There's also
an MDPS **Oathplate-Whip** no-scythe variant. At Maiden, only the scythe setup melees her;
every other setup (incl. Oathplate-Whip) ranges her.

## Key technical solutions (don't regress these)

- **Instance coordinates.** ToB rooms are instanced. `getWorldLocation()` returns DYNAMIC
  coords inside an instance, while room boxes are defined in TEMPLATE coords. Player position
  is resolved via `WorldPoint.fromLocalInstance(client, getLocalLocation())` when
  `isInInstancedRegion()`. To DRAW a template-coord box, translate with
  `WorldPoint.toLocalInstance(client, wp)` → `LocalPoint.fromWorld` → `Perspective.getCanvasTilePoly`.
- **Per-raid resets.** ToB fires LOADING between rooms within one raid. Per-raid flags must
  reset only when back in the lobby (`!isInInstancedRegion()`), NOT on every LOADING.
- **NPC true tiles.** `NPC.getWorldLocation()` is the server tile at the SW corner of the
  footprint. For multi-tile NPCs, shift to the footprint center using
  `getTransformedComposition().getSize()` and draw with `Perspective.getCanvasTileAreaPoly`,
  matching RuneLite's own true-tile highlight.

## Feature status

**v1.1 (live):** gear/loadout checking per role/setup, raid-entry checklist, popup overlay.

**Phase 3 — Maiden room (in progress, unreleased):**
- Setup prompt on entry (drop Salve, equip Elder Maul / DWH) — comply-style, once per raid.
- Prayer prompt on entry — role/setup based: scythe → "Pray Magic and Piety"; other
  MDPS/RDPS → Rigour; freezers → Augury. Two modes (toggle): flash-once (skipped if already
  praying correctly) or stay-until-correct (persists until prayers are actually on). Checks
  active prayers via `client.isPrayerActive(...)`.
- HP call-outs at 75 / 55 / 35% (Maiden has no real HP; uses healthRatio/healthScale).
- Floor tile markers (see below).

## Maiden floor markers (TileMarkerOverlay)

Standing boxes are role-specific, shown only while Maiden is in the room. All are
`{minX, maxX, minY, maxY}` template world coords, plane 0. Current values (being fine-tuned
in-game, so expect Jake to hand you updated numbers):

- `BOX_MDPS_NOSCY = {3166, 3169, 4445, 4448}`
- `BOX_RDPS_NOSCY = {3166, 3169, 4437, 4440}`
- `BOX_NFRZ = {3168, 3171, 4452, 4455}`
- `BOX_SFRZ = {3169, 3172, 4438, 4441}`

Scythe setups get no box (they melee Maiden). Colors are tuned to the room's maroon palette
but kept distinct: standing box = muted dusty rose; Nylocas Matomenos = darker red (freezers
only); blood spawns = medium grey (all roles).

NPC IDs:
- Maiden: 8360–8365 (phases), 10814–10819 (story/entry variants)
- Nylocas Matomenos: 10820 (entry), 8366 (normal)
- Blood spawn: 8367, 10821, 10829

Jake captures tile coords in-game using RuneLite dev tools (Local + World readouts). When he
gives local coords, convert with 128 local units = 1 world tile, anchoring off a known
local↔world pair from prior captures.

## Release workflow

- Versioning: incremental patch per room (1.1.1 = Maiden, 1.1.2 = Bloat, …). 1.2 = milestone
  when all rooms are done. Changelog header currently `1.1.1`.
- Hub update: bump version in `build.gradle` and `runelite-plugin.properties` → commit + push
  to `macflag/learner-tob` → grab the full 40-char commit hash → update the `commit=` line in
  the `plugins/learner-tob` manifest in Jake's plugin-hub fork (sync the fork first) → PR to
  `runelite/plugin-hub` → watch the build CI.

## Roadmap

Finish Maiden → cut 1.1.1. Then Bloat, Nylocas, Sotetseg, Xarpus, Verzik (each its own config
section and patch version), then QoL polish. For each new room Jake will provide NPC IDs, room
coords, and tile coords from in-game.