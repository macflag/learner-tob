# Learner ToB

A RuneLite plugin that helps players learning **Theatre of Blood** get raid-ready. It reads your bank, equipment, and inventory, works out the standard loadout for your role, shows what you have versus what you are missing, and reminds you about the easy-to-forget pre-raid steps right as you walk in the door.

Built for the **[Rancour PvM Clan](https://discord.gg/rancour-pvm)** to help new learners prepare quickly and consistently.

The plugin is **bank-driven** — there are no gear dropdowns to configure. Open your bank once and it detects your setup automatically.

> **📖 Full documentation is on the [Wiki](../../wiki).** This README is a quick overview; the wiki has the per-role loadouts, accepted item substitutions, check logic, settings reference, and developer notes.

---

## Features

- **Automatic setup detection** — reads your bank, equipment, and inventory and resolves the correct loadout for your role and setup variant (Scythe, No-Scythe, or MDPS Oathplate-Whip), with no configuration required.
- **Sidebar loadout visualizer** — your expected loadout laid out like the in-game screen (inventory grid, equipment cross, spellbook, rune pouch). Hover any cell to identify it.
- **Gear check** — validates what you are wearing and carrying against your role's loadout. Run it manually (button or `::tobcheck`), or let it run automatically at the raid-start door.
- **Pre-raid checklist** — spellbook, runes, auto-retaliate, pre-pot boosts, and HP overheal, each individually toggleable.
- **Maiden in-room guidance** — setup prompt on entry (drop Salve, equip your spec weapon), prayer prompt (role and setup aware), HP call-outs at 75/55/35%, and floor tile markers for standing positions, Nylocas Matomenos, and blood spawns.
- **Dark-cockpit design** — no green. **Yellow** = in bank, **red** = missing/wrong, **neutral** = ready. Your eye is only drawn to what still needs action.
- **Smart popups** — click-inside-to-close (the click is swallowed so you don't walk), timed auto-dismiss, comply-style (stays until you act), and optional flashing for urgent alerts.

See the wiki for the full per-role breakdown and the exact check rules.

---

## Roles

Pick your role from the **Role** dropdown at the top of the sidebar. Setup variants are detected automatically from your gear — MDPS has three: Scythe, No-Scythe (Void), and Oathplate-Whip.

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

Grouped into **General**, **Raid checklist**, **Maiden**, **Popup**, and **Testing (dev)** sections. Highlights:

- Toggle the gear check, each individual checklist item, and the automatic raid-entry door check.
- Maiden prompts and tile markers, each individually toggleable.
- Popup font size, position, opacity, issue colours, and flashing.

See the **[Settings wiki page](../../wiki/Settings)** for every option.

---

## Roadmap

Built in phases; every feature is individually toggleable.

- **✅ Phase 1 — Setups & core:** all four roles with Scythe/No-Scythe variants, per-role spellbooks and runes, priority-based gear detection with bank-upgrade hints.
- **✅ Phase 2 — Standalone features:** sidebar loadout visualizer, full pre-raid checklist, reusable proximity-zone engine (raid-start door check), dark-cockpit colours, per-popup dismissal and flashing.
- **🔄 Phase 3 — In-room guidance (in progress):** per-room, per-role entry checks, prayer reminders, tile markers, and HP callouts. ✅ Maiden complete. Bloat, Nylocas, Sotetseg, Xarpus, Verzik coming next.
- **🔭 Phase 4 — Quality of life:** hide the Bloat floor, hide other players, highlight Nylocas aggros.

Detail on each phase lives on the **[Roadmap wiki page](../../wiki/Roadmap)**.

---

## Changelog

### 1.1.2 — Item ID expansion and bank snapshot
- **Bank snapshot:** bank contents are now cached the first time you open your bank and persist for the session — no need to re-open the bank when switching roles or checking gear mid-raid
- **Defender slot:** corrected item IDs and added all variants — Avernic Defender (normal + locked), Ghommal's Avernic 5 & 6 (normal + locked), Dragon Defender (normal + locked), Dragon Defender (t) (normal + locked). Avernic variants are flagged as an upgrade over Dragon Defender
- **Boots slot:** added Avernic Treads (pr), (pr)(et), (pr)(pe) variants and Dragon Boots, (cr), (g). Full priority ladder: Treads Max → PR variants → base Treads → Primordial → Dragon Boots
- **Anguish:** Necklace of Anguish (base) accepted alongside Anguish (or) in all setups that use it
- **Blessing (ammo slot):** Rada's Blessing 3, Ancient, Holy, Honourable, and Peaceful Blessings all accepted alongside Rada's Blessing 4. Rada's 4 is flagged as an upgrade over the others
- **Freeze staff:** full weapon ladder accepted for NFRZ/SFRZ — Volatile Nightmare Staff (DM) → Kodai Wand → Ice Ancient Sceptre → Blood Ancient Sceptre → Ancient Sceptre (all with locked variants). Upgrade hints fire for freeze roles when a better staff is in the bank

### 1.1.1 — Maiden
- Maiden setup prompt on entry: reminds you to drop Salve and equip Elder Maul or Dragon Warhammer (comply-style, once per raid)
- Maiden prayer prompt on entry: tells you which prayers to activate based on your role and setup (Magic + Piety for scythe, Rigour for all other MDPS/RDPS including Oathplate-Whip, Augury for freezers). Two modes: flash-once (skipped if already praying correctly) or stay-until-correct (toggle in settings)
- Maiden HP call-outs at 75%, 55%, and 35% with role-specific instructions
- Floor tile markers: role-specific 4x4 standing box, Nylocas Matomenos true-tile (SFRZ/NFRZ, darker red), blood spawn true-tile (all roles, grey). All colors tuned to the room palette
- Config restructured: Maiden settings moved to their own section; Popup section moved above Testing
- MDPS Oathplate-Whip variant: new third MELEE setup, auto-detected from gear
- RDPS fixes: Oathplate ordering, chin badge display, rune pouch filtering

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

Questions, suggestions, or want to help build out a room? Join us at **[discord.gg/rancour-pvm](https://discord.gg/rancour-pvm)**. Found a bug or have a feature request? Open an issue on **[GitHub](../../issues)**. Developer and architecture notes are on the **[Development wiki page](../../wiki/Development)**.
