# Learner ToB

A RuneLite plugin that helps players preparing to learn **Theatre of Blood** confirm they are bringing the correct gear and inventory before a raid. It reads your bank, equipment and inventory, works out which standard loadout you are running, and shows you exactly what you have and what you are missing.

This plugin was made for the **[Rancour PvM Clan](https://discord.gg/rancour-pvm)** to help new learners get raid-ready quickly.

The plugin is **bank-driven**. There are no gear dropdowns to configure — open your bank once and the plugin detects your setup automatically.

## Features

### Automatic setup detection
When you open your bank (or change your equipment or inventory), the plugin reads everything you own and decides which Melee DPS variant you are running:

- **MDPS Scythe** — Scythe of Vitur setup (Torva/Oathplate/Bandos body armour).
- **MDPS No Scythe** — Abyssal Tentacle + full Void setup.

If a Scythe (any variant: charged, uncharged, holy, or sanguine) is detected, the Scythe loadout is used. Otherwise the No-Scythe Void loadout is used.

### Side panel loadout list
The plugin adds a panel to the RuneLite sidebar showing the full expected loadout for your detected setup, split into **Worn** and **Inventory** sections, followed by the spellbook.

Each row is colour-coded by what you actually own:

- **Green** — equipped or in your inventory (ready to raid).
- **Yellow** — only in your bank (you still need to withdraw it).
- **Red** — not found anywhere (you are missing it).

Where you own a specific valid item, the row shows that item's real name. Where you are missing it, the row shows the acceptable options instead, so you know what you can bring.

A **Role** dropdown at the top of the panel lets you pick your raid role. Only **MDPS** has a loadout defined for now; other roles are placeholders.

### Gear check
Click the **Run Gear Check** button in the panel (or run `::tobcheck`) to validate what you are currently wearing and carrying (not your bank — it must actually be on you) against the detected loadout. Results appear in a popup overlay:

- **0 issues** — a green "PASSED" popup.
- **1–3 issues** — a yellow popup listing each fix, e.g. `Equip Infernal Cape`, `Bring 3x Saradomin Brew (have 1)`.
- **4 or more issues** — an orange "LOTS" popup, with the full list also printed to your chat box.

Every check result is copied to your clipboard so you can paste it into clan chat or Discord.

The popup is fully configurable in the plugin settings (see below).

## Commands

| Command | Description |
| --- | --- |
| `::tobcheck` | Same as the **Run Gear Check** button: checks your worn + inventory items against the detected loadout. Shows a popup, prints to chat if there are 4+ issues, and copies the result to your clipboard. |
| `::tobdump` | Print every item ID you currently have equipped and in your inventory, plus your spellbook, and copy it to the clipboard. Useful for reporting issues or adding new gear. |

## Accepted substitutions

The check treats the following as interchangeable — any one of them satisfies the slot:

- **Helm:** Torva / Oathplate (Void Melee Helm for the No-Scythe setup)
- **Body:** Torva / Oathplate / Bandos (Elite Void for No-Scythe)
- **Legs:** Torva / Oathplate / Bandos (Elite Void for No-Scythe)
- **Melee Cape:** Infernal Cape / Fire Cape (and their max-cape and locked variants)
- **Neck:** Amulet of Rancour / Amulet of Torture
- **Boots:** Avernic Treads / Primordial Boots
- **Ring:** Ultor Ring / Berserker Ring (i)
- **Mage Weapon:** Eye of Ayak / Sanguinesti Staff / Trident of the Swamp
- **Shield (No-Scythe):** any Avernic or Dragon Defender
- **DPS Spec:** Dragon Claws / Burning Claws
- **Defence Spec:** Elder Maul / Dragon Warhammer
- **Range Cape switch:** any Dizana's Quiver / Ava's Assembler variant

Cosmetic recolours (Sanguine Torva, Radiant Oathplate, Holy/Sanguine Scythe, ornamented amulets, etc.) are always accepted automatically.

## Settings

**General**
- *Enable gear check* — turn `::tobcheck` on or off.

**Popup**
- *Dismiss mode* — click to close, or auto-dismiss after 3, 5 or 10 seconds.
- *Font size* — Small / Medium / Large.
- *Position* — Top Left / Top Center / Top Right / Center / Bottom Left / Bottom Right.
- *Opacity* — popup background opacity (10–100).
- *Pass / Fail / LOTS colour* — header colours for each result state.

## Roadmap

Planned upgrades include:

- **Loadouts for every role** — Ranged DPS (RDPS), North Freeze (NFRZ) and South Freeze (SFRZ), in addition to the current Melee DPS setups. The Role dropdown already lists these; their loadouts will be filled in.
- **Room-by-room features** — per-room guidance and checks as you progress through the Theatre (Maiden, Bloat, Nylocas, Sotetseg, Xarpus, Verzik), so learners get the right information at the right time.

## Notes

The current loadouts follow the Rancour PvM learner MDPS setup on the Arceuus spellbook (Fire/Blood/Aether/Death for Thralls and Death Charge).

Questions, suggestions, or want to help build out the other roles? Join us at **[discord.gg/rancour-pvm](https://discord.gg/rancour-pvm)**.
