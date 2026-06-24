# Learner ToB

A RuneLite plugin that helps players learning **Theatre of Blood** get raid-ready. It reads your bank, equipment, and inventory, works out the standard loadout for your role, shows what you have versus what you are missing, and reminds you about the easy-to-forget pre-raid steps right as you walk in the door.

Built for the **[Rancour PvM Clan](https://discord.gg/rancour-pvm)** to help new learners prepare quickly and consistently.

The plugin is **bank-driven** — there are no gear dropdowns to configure. Open your bank once and it detects your setup automatically.

> **📖 Full documentation is on the [Wiki](../../wiki).** This README is a quick overview; the wiki has the per-role loadouts, accepted item substitutions, check logic, settings reference, and developer notes.

---

## Features

- **Automatic setup detection** — reads your bank, equipment, and inventory and resolves the correct loadout for your role and Scythe/No-Scythe variant.
- **Sidebar loadout visualizer** — your expected loadout laid out like the in-game screen (inventory grid, equipment cross, spellbook, rune pouch). Hover any cell to identify it.
- **Gear check** — validates what you are wearing and carrying against your role's loadout. Run it manually (button or `::tobcheck`), or let it run automatically at the raid-start door.
- **Pre-raid checklist** — spellbook, runes, auto-retaliate, pre-pot boosts, and HP overheal, each individually toggleable.
- **Dark-cockpit design** — no green. **Yellow** = in bank, **red** = missing/wrong, **neutral** = ready. Your eye is only drawn to what still needs action.
- **Smart popups** — click-inside-to-close (the click is swallowed so you don't walk), timed auto-dismiss, and optional flashing for urgent alerts.

See the wiki for the full per-role breakdown and the exact check rules.

---

## Roles

Pick your role from the **Role** dropdown at the top of the sidebar. Each has a Scythe and No-Scythe variant, detected automatically from your gear.

| Role | Spellbook |
| --- | --- |
| **MDPS** — Melee DPS | Arceuus |
| **RDPS** — Ranged DPS | Lunar |
| **NFRZ** — North Freeze | Ancient |
| **SFRZ** — South Freeze | Ancient |

Full loadouts, rune lists, and accepted substitutions are on the **[Loadouts wiki page](../../wiki/Loadouts)**.

---

## Commands

| Command | Description |
| --- | --- |
| `::tobcheck` | Run the full gear + checklist check. Shows a popup, prints to chat for 4+ issues, and copies the result to your clipboard. |
| `::tobdump` | Print every equipped/inventory item ID, plus spellbook, rune pouch, and auto-retaliate state, and copy it to the clipboard. Useful for reporting issues or adding gear. |

---

## Settings

Grouped into **General**, **Popup**, and **Raid checklist** sections. Highlights:

- Toggle the gear check, each individual checklist item, and the automatic raid-entry door check.
- Popup font size, position, opacity, issue colours, and flashing.

See the **[Settings wiki page](../../wiki/Settings)** for every option.

---

## Roadmap

Built in phases; every feature is individually toggleable.

- **✅ Phase 1 — Setups & core:** all four roles with Scythe/No-Scythe variants, per-role spellbooks and runes, priority-based gear detection with bank-upgrade hints.
- **✅ Phase 2 — Standalone features:** sidebar loadout visualizer, full pre-raid checklist, reusable proximity-zone engine (raid-start door check), dark-cockpit colours, per-popup dismissal and flashing.
- **🔜 Phase 3 — In-room guidance:** per-room, per-role engine for entry checks, prayer reminders, tile markers, boss-HP callouts, and between-room supply reminders (Maiden → Verzik). Includes optional Xarpus sound muting.
- **🔭 Phase 4 — Quality of life:** hide the Bloat floor, hide other players, highlight Nylocas aggros.

Detail on each phase lives on the **[Roadmap wiki page](../../wiki/Roadmap)**.

---

## Changelog

### 1.1 — Phase 2
- Sidebar loadout visualizer (inventory grid, equipment cross, spellbook, rune pouch)
- Full pre-raid checklist: spellbook, runes, auto-retaliate, pre-pot boost matrix, HP overheal
- Automatic raid-start door check, built on a reusable proximity-zone engine
- Dark-cockpit colour scheme — removed green (yellow = bank, red = missing, neutral = ready)
- Per-popup dismissal (click-inside / timed) and optional flashing alerts
- Removed the global *Dismiss mode* and *Pass colour* settings (now decided per popup)

### 1.0 — Phase 1
- Initial Plugin Hub release
- Bank-driven gear checker for all four roles (MDPS, RDPS, NFRZ, SFRZ), Scythe and No-Scythe
- Per-role spellbooks and runes
- Priority-based armour and spec-weapon detection with bank-upgrade hints
- One-click gear check, plus the `::tobcheck` and `::tobdump` commands

---

## Contributing

Questions, suggestions, or want to help build out a room? Join us at **[discord.gg/rancour-pvm](https://discord.gg/rancour-pvm)**. Developer and architecture notes are on the **[Development wiki page](../../wiki/Development)**.