/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Learner ToB",
		description = "Bank-driven gear checks for learning Theatre of Blood",
		tags = {"tob", "theatre", "blood", "raid", "learner", "pvm"}
)
public class LearnerTobPlugin extends Plugin implements MouseListener
{
	private static final int LOTS_THRESHOLD = 4;
	private static final int AUTO_RETALIATE_VARP = 172; // 0 = ON, non-zero = OFF

	// Rune pouch: 6 slots, each has a type varbit (index into RUNEPOUCH_RUNE enum)
	// and an amount varbit. Uses the legacy Varbits constants which cover all 6 slots.
	private static final int[] RUNE_TYPE_VARBITS   = {
			Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3,
			Varbits.RUNE_POUCH_RUNE4, Varbits.RUNE_POUCH_RUNE5, Varbits.RUNE_POUCH_RUNE6
	};
	private static final int[] RUNE_AMOUNT_VARBITS = {
			Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3,
			Varbits.RUNE_POUCH_AMOUNT4, Varbits.RUNE_POUCH_AMOUNT5, Varbits.RUNE_POUCH_AMOUNT6
	};

	// Sentinel equipSlot value that tags a slot as "rune pouch entry" in the panel.
	private static final int RUNE_POUCH_SENTINEL = -99;

	// Dark cockpit: no green. Ready items get a neutral border so the eye is
	// only drawn to YELLOW (in bank) and RED (missing/problem).
	private static final Color NEUTRAL = new Color(140, 140, 140);
	private static final Color YELLOW  = new Color(230, 200, 60);
	private static final Color RED     = new Color(220, 90, 90);

	@Inject private Client client;
	@Inject private LearnerTobConfig config;
	@Inject private ConfigManager configManager;
	@Inject private ItemManager itemManager;
	@Inject private OverlayManager overlayManager;
	@Inject private MouseManager mouseManager;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ClientThread clientThread;
	@Inject private GearCheckOverlay overlay;
	@Inject private LearnerTobPanel panel;

	private NavigationButton navButton;
	private boolean lastScytheSetup = true;

	// Reusable proximity zones. The raid-start door is the first; Phase 3
	// rooms each add their own ZoneTrigger here.
	private final List<ZoneTrigger> zones = new ArrayList<>();

	@Provides
	LearnerTobConfig provideConfig(ConfigManager cm)
	{
		return cm.getConfig(LearnerTobConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		mouseManager.registerMouseListener(this);

		navButton = NavigationButton.builder()
				.tooltip("Learner ToB")
				.icon(createIcon())
				.priority(8)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);

		panel.setOnRunCheck(() -> clientThread.invokeLater(this::runCheck));
		panel.setRole(config.role());
		panel.setOnRoleChange(role ->
		{
			configManager.setConfiguration(LearnerTobConfig.GROUP, "role", role);
			clientThread.invokeLater(this::refreshFromOwned);
		});
		clientThread.invokeLater(this::refreshFromOwned);

		buildZones();
	}

	/** Defines the proximity zones. Add Phase 3 room gates here. */
	private void buildZones()
	{
		zones.clear();
		// Raid-start door lobby box (inclusive bounds), plane 0.
		zones.add(new ZoneTrigger("Raid entry", 3666, 3677, 3215, 3223, 0,
				this::raidEntryProblems));
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		mouseManager.unregisterMouseListener(this);
		clientToolbar.removeNavigation(navButton);
		overlay.dismiss();
	}

	// ------------------------------------------------------------------
	//  Container helpers
	// ------------------------------------------------------------------

	private Set<Integer> readyIds()
	{
		Set<Integer> ids = new HashSet<>();
		addContainer(ids, InventoryID.EQUIPMENT);
		addContainer(ids, InventoryID.INVENTORY);
		return ids;
	}

	private Set<Integer> bankOnlyIds()
	{
		Set<Integer> ids = new HashSet<>();
		addContainer(ids, InventoryID.BANK);
		return ids;
	}

	private Set<Integer> ownedIds()
	{
		Set<Integer> ids = new HashSet<>();
		addContainer(ids, InventoryID.BANK);
		addContainer(ids, InventoryID.EQUIPMENT);
		addContainer(ids, InventoryID.INVENTORY);
		return ids;
	}

	private void addContainer(Set<Integer> ids, InventoryID which)
	{
		ItemContainer c = client.getItemContainer(which);
		if (c == null) return;
		for (Item i : c.getItems())
			if (i.getId() > 0) ids.add(i.getId());
	}

	private Map<Integer, Integer> containerCounts(InventoryID which)
	{
		Map<Integer, Integer> map = new LinkedHashMap<>();
		ItemContainer c = client.getItemContainer(which);
		if (c == null) return map;
		for (Item i : c.getItems())
			if (i.getId() > 0)
				map.merge(i.getId(), Math.max(1, i.getQuantity()), Integer::sum);
		return map;
	}

	// ------------------------------------------------------------------
	//  Rune pouch helpers
	// ------------------------------------------------------------------

	/**
	 * Returns a map of rune item ID -> quantity for every filled slot
	 * in the rune pouch. Decodes the type index via EnumID.RUNEPOUCH_RUNE.
	 * Must be called on the client thread.
	 */
	private Map<Integer, Integer> runePouchContents()
	{
		Map<Integer, Integer> runes = new LinkedHashMap<>();
		net.runelite.api.EnumComposition runeEnum =
				client.getEnum(EnumID.RUNEPOUCH_RUNE);
		for (int i = 0; i < RUNE_TYPE_VARBITS.length; i++)
		{
			int typeIdx = client.getVarbitValue(RUNE_TYPE_VARBITS[i]);
			int amount  = client.getVarbitValue(RUNE_AMOUNT_VARBITS[i]);
			if (typeIdx == 0 || amount == 0) continue;
			int runeItemId = runeEnum.getIntValue(typeIdx);
			if (runeItemId > 0)
				runes.put(runeItemId, amount);
		}
		return runes;
	}

	/**
	 * Returns the expected runes for the current role, as a map of
	 * rune item ID -> minimum quantity required (always 1 for the check;
	 * the actual minimum is >=1 in pouch or inventory).
	 */
	/**
	 * Runes shown in the Rune Pouch panel section. For RDPS the pouch holds only
	 * Law/Aether/Blood/Death; Lava and Astral are carried loose in inventory, so
	 * they are excluded here (the full check still validates them via expectedRunes).
	 */
	private Map<Integer, String> pouchRunes(Role role)
	{
		if (role == Role.RANGED)
		{
			Map<Integer, String> runes = new LinkedHashMap<>();
			runes.put(563,   "Law");
			runes.put(30843, "Aether");
			runes.put(565,   "Blood");
			runes.put(560,   "Death");
			return runes;
		}
		return expectedRunes(role);
	}

	private Map<Integer, String> expectedRunes(Role role)
	{
		Map<Integer, String> runes = new LinkedHashMap<>();
		switch (role)
		{
			case MELEE:
				// Arceuus: Blood, Soul, Fire, Aether (Nature if using Resurrect)
				runes.put(565,  "Blood");   // Blood rune
				runes.put(566,  "Soul");    // Soul rune
				runes.put(554,  "Fire");    // Fire rune
				runes.put(9075, "Astral");  // Astral (Reanimate)
				break;
			case RANGED:
				// Lunar: Law, Aether, Blood, Death, Lava, Astral — checked across pouch + inventory
				runes.put(563,   "Law");    // Law rune
				runes.put(30843, "Aether"); // Aether rune
				runes.put(565,   "Blood");  // Blood rune
				runes.put(560,   "Death");  // Death rune
				runes.put(4699,  "Lava");   // Lava rune
				runes.put(9075,  "Astral"); // Astral rune
				break;
			case NORTH_FREEZE:
			case SOUTH_FREEZE:
				// Ancient: Blood, Soul, Death, Water
				runes.put(565,  "Blood");   // Blood rune
				runes.put(566,  "Soul");    // Soul rune
				runes.put(560,  "Death");   // Death rune
				runes.put(555,  "Water");   // Water rune
				break;
			default:
				break;
		}
		return runes;
	}

	// ------------------------------------------------------------------
	//  Refresh
	// ------------------------------------------------------------------

	private void refreshFromOwned()
	{
		Set<Integer> owned = ownedIds();
		if (!owned.isEmpty())
		{
			lastScytheSetup = Presets.hasScythe(owned);
			panel.setScytheSetup(lastScytheSetup);
		}

		Set<Integer> ready = readyIds();
		Set<Integer> bank  = bankOnlyIds();
		Map<Integer, Integer> pouchContents = runePouchContents();

		Role role      = config.role();
		boolean scythe = lastScytheSetup;

		int expectedBook = Presets.expectedSpellbook(role);
		boolean bookOk   = expectedBook == readSpellbook();
		String  bookName = spellbookName(expectedBook);

		List<SlotReq> reqs = Presets.requirements(role, scythe);
		List<LearnerTobPanel.ResolvedSlot> slots = resolveSlots(reqs, ready, bank);

		// Append rune slots (tagged with sentinel equipSlot = -99).
		// Each rune is checked across pouch + inventory combined — green if
		// found in either, red if missing from both. No assumption about
		// which runes live in the pouch vs inventory.
		Map<Integer, String> displayRunes = pouchRunes(role);
		for (Map.Entry<Integer, String> entry : displayRunes.entrySet())
		{
			int runeId      = entry.getKey();
			String runeName = entry.getValue();
			int inPouch     = pouchContents.getOrDefault(runeId, 0);
			boolean inInv   = ready.contains(runeId);

			Color  border;
			String displayName;

			if (inPouch > 0 || inInv)
			{
				border      = NEUTRAL;
				displayName = inPouch > 0
						? runeName + " (" + inPouch + ")"
						: runeName + " (inv)";
			}
			else
			{
				border      = RED;
				displayName = runeName + " \u2717";
			}

			BufferedImage sprite = null;
			try { sprite = itemManager.getImage(runeId); }
			catch (RuntimeException ignored) { }

			slots.add(new LearnerTobPanel.ResolvedSlot(
					runeId, displayName, sprite, border,
					1, false, RUNE_POUCH_SENTINEL, 0));
		}

		// Build the panel subtitle from the SAME rune data the check uses, so the
		// label can never drift from the actual requirements.
		StringBuilder sub = new StringBuilder(bookName).append(" | ");
		boolean firstRune = true;
		for (String label : pouchRunes(role).values())
		{
			if (!firstRune) sub.append("/");
			sub.append(label);
			firstRune = false;
		}
		final String subtitle = sub.toString();

		final List<LearnerTobPanel.ResolvedSlot> finalSlots = slots;
		SwingUtilities.invokeLater(() ->
		{
			panel.setResolvedSlots(finalSlots, bookName, bookOk, subtitle);
			panel.refresh();
		});
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == InventoryID.BANK.getId()
				|| id == InventoryID.EQUIPMENT.getId()
				|| id == InventoryID.INVENTORY.getId())
		{
			refreshFromOwned();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Refresh when any rune pouch varbit changes
		int b = event.getVarbitId();
		for (int v : RUNE_TYPE_VARBITS)   if (b == v) { refreshFromOwned(); return; }
		for (int v : RUNE_AMOUNT_VARBITS) if (b == v) { refreshFromOwned(); return; }
	}

	// ------------------------------------------------------------------
	//  Slot resolution (client thread)
	// ------------------------------------------------------------------

	private List<LearnerTobPanel.ResolvedSlot> resolveSlots(
			List<SlotReq> reqs, Set<Integer> ready, Set<Integer> bank)
	{
		List<LearnerTobPanel.ResolvedSlot> out = new ArrayList<>();
		for (SlotReq req : reqs)
		{
			Integer chosen = null;
			Color   border = RED;

			for (int id : req.validIds)
				if (ready.contains(id)) { chosen = id; border = NEUTRAL; break; }
			if (chosen == null)
				for (int id : req.validIds)
					if (bank.contains(id)) { chosen = id; border = YELLOW; break; }
			if (chosen == null && !req.validIds.isEmpty())
			{
				chosen = req.validIds.iterator().next();
				border = RED;
			}
			if (chosen == null) continue;

			String name = itemName(chosen);
			BufferedImage sprite = null;
			try { sprite = itemManager.getImage(chosen); }
			catch (RuntimeException ignored) { }

			int equipSlot = req.equipped ? equipSlotOf(chosen, req.label) : -1;

			// Stackable items (chinchompas, ammo) occupy ONE inventory slot regardless
			// of quantity, so render them as a single cell with a count badge.
			// Non-stackable consumables (brews, restores, Anglerfish) expand into N cells.
			int displayQty = req.quantity;
			int badge      = 0;
			try
			{
				if (itemManager.getItemComposition(chosen).isStackable() && req.quantity > 1)
				{
					displayQty = 1;
					badge      = req.quantity;
				}
			}
			catch (RuntimeException ignored) { }

			out.add(new LearnerTobPanel.ResolvedSlot(
					chosen, name, sprite, border,
					displayQty, req.equipped, equipSlot, badge));
		}
		return out;
	}

	// ------------------------------------------------------------------
	//  Mouse
	// ------------------------------------------------------------------

	// Set when a press lands on the popup, so we also swallow the matching
	// release + click and the game never registers a walk/interact there.
	private boolean consumePopupClick = false;

	@Override
	public MouseEvent mousePressed(MouseEvent e)
	{
		// OSRS registers the walk/interact on the PRESS, so dismiss and consume
		// here (not on click) to stop the character moving. Presses elsewhere
		// pass straight through so running/eating is unaffected.
		if (overlay.isVisible() && overlay.containsPoint(e.getPoint()))
		{
			overlay.dismiss();
			consumePopupClick = true;
			e.consume();
		}
		return e;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent e)
	{
		if (consumePopupClick)
			e.consume();
		return e;
	}

	@Override
	public MouseEvent mouseClicked(MouseEvent e)
	{
		if (consumePopupClick)
		{
			consumePopupClick = false;
			e.consume();
		}
		return e;
	}

	@Override public MouseEvent mouseEntered(MouseEvent e)  { return e; }
	@Override public MouseEvent mouseExited(MouseEvent e)   { return e; }
	@Override public MouseEvent mouseMoved(MouseEvent e)    { return e; }
	@Override public MouseEvent mouseDragged(MouseEvent e)  { return e; }

	// ------------------------------------------------------------------
	//  Commands
	// ------------------------------------------------------------------

	// ------------------------------------------------------------------
	//  Proximity engine (push-style: silent on pass, popup only on issue)
	// ------------------------------------------------------------------

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!config.raidEntryCheck())
			return;

		Player local = client.getLocalPlayer();
		if (local == null)
			return;

		WorldPoint wp = local.getWorldLocation();
		if (wp == null)
			return;

		for (ZoneTrigger zone : zones)
		{
			if (!zone.entered(wp))
				continue;

			List<String> problems = zone.runChecks();
			if (problems.isEmpty())
				continue; // dark cockpit: stay silent when everything is set

			String title = zone.getTitle() + " \u2014 " + problems.size() + " issue(s)";
			// Click-inside to close, no timer (you may be mid-action at the door).
			overlay.showResult(title, problems, GearCheckOverlay.DismissMode.CLICK, 0, false);
			if (problems.size() >= LOTS_THRESHOLD)
				for (String p : problems) message("  - " + p);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		// Reset on region load / hop / logout so a fresh approach re-fires.
		if (state == GameState.LOADING
				|| state == GameState.HOPPING
				|| state == GameState.LOGIN_SCREEN)
		{
			for (ZoneTrigger zone : zones) zone.reset();
		}
	}

	/**
	 * Raid-start door check — runs the SAME full check as the manual gear check
	 * (gear/consumables, runes, spellbook, retaliate, boost matrix, HP overheal),
	 * so nothing is missed at the door. Push-style: silent unless there's a problem.
	 */
	private List<String> raidEntryProblems()
	{
		return collectProblems(config.role(), lastScytheSetup);
	}

	/** Boost-presence check: stat boosted above its base level (decay-tolerant). */
	private boolean boosted(Skill skill)
	{
		return client.getBoostedSkillLevel(skill) > client.getRealSkillLevel(skill);
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		switch (event.getCommand().toLowerCase())
		{
			case "tobcheck": runCheck(); break;
			case "tobdump":  runDump();  break;
			default: break;
		}
	}

	private void runCheck()
	{
		if (!config.enableGearCheck())
		{
			message("Gear check is turned off.");
			return;
		}
		Role role      = config.role();
		boolean scythe = lastScytheSetup;

		List<String> problems = collectProblems(role, scythe);

		String title = problems.isEmpty()
				? "Gear check \u2014 no issues"
				: role + " " + (scythe ? "Scythe" : "No-Scythe") + " \u2014 " + problems.size() + " issue(s)";

		// Manual check is a PULL: confirm even when clean, using the global dismiss mode.
		overlay.showResult(title, problems);
		if (problems.size() >= LOTS_THRESHOLD)
			for (String p : problems) message("  - " + p);

		List<String> clip = new ArrayList<>();
		clip.add(title);
		for (String p : problems) clip.add("  - " + p);
		copyToClipboard(clip);
	}

	/**
	 * Shared problem collector used by BOTH the manual gear check (pull) and the
	 * raid-start door proximity check (push), so the two can never drift apart.
	 * Covers gear/consumable requirements (e.g. Anglerfish, restores), runes,
	 * spellbook, auto-retaliate, the full pre-pot boost matrix, HP overheal
	 * (3-state), and bank-upgrade hints.
	 */
	private List<String> collectProblems(Role role, boolean scythe)
	{
		Map<Integer, Integer> wornCounts = containerCounts(InventoryID.EQUIPMENT);
		Map<Integer, Integer> invCounts  = containerCounts(InventoryID.INVENTORY);

		List<String> problems = new ArrayList<>();

		// Gear + consumables (equipped vs. carried).
		for (SlotReq req : Presets.requirements(role, scythe))
		{
			Map<Integer, Integer> pool = req.equipped ? wornCounts : invCounts;
			int have = 0;
			for (int id : req.validIds) have += pool.getOrDefault(id, 0);
			if (have < req.quantity)
			{
				String where = req.equipped ? "Equip" : "Bring";
				problems.add(req.quantity > 1
						? where + " " + req.quantity + "x " + req.label + " (have " + have + ")"
						: where + " " + req.label);
			}
		}

		// Runes — each expected rune satisfied by pouch OR inventory (presence).
		Map<Integer, Integer> pouch = runePouchContents();
		for (Map.Entry<Integer, String> entry : expectedRunes(role).entrySet())
		{
			int id = entry.getKey();
			boolean inPouch = pouch.getOrDefault(id, 0) > 0;
			boolean inInv   = invCounts.containsKey(id);
			if (!inPouch && !inInv)
				problems.add("Missing " + entry.getValue() + " rune (not in pouch or inventory)");
		}

		// Checklist (spellbook, auto-retaliate, pre-pot, HP overheal).
		if (config.enableChecklist())
		{
			if (config.checkSpellbook())
			{
				int expected = Presets.expectedSpellbook(role);
				if (expected != readSpellbook())
					problems.add("Switch to " + spellbookName(expected) + " spellbook");
			}
			if (config.checkAutoRetaliate() && client.getVarpValue(AUTO_RETALIATE_VARP) == 0)
				problems.add("Turn auto-retaliate OFF");
			if (config.checkPrePot())
				addPrePotProblems(problems, role, scythe);
			if (config.checkHpOverheal())
				addHpProblems(problems);
		}

		// Bank-upgrade hints ("better item sitting in the bank").
		Set<Integer> wornInv = new HashSet<>(wornCounts.keySet());
		wornInv.addAll(invCounts.keySet());
		Set<Integer> bankOnly = bankOnlyIds();
		if (scythe)
		{
			addPriorityWarning(problems, "helm",  Presets.HELM_PRIORITY, wornInv, bankOnly);
			addPriorityWarning(problems, "body",  Presets.BODY_PRIORITY, wornInv, bankOnly);
			addPriorityWarning(problems, "legs",  Presets.LEGS_PRIORITY, wornInv, bankOnly);
		}
		addPriorityWarning(problems, "DPS spec",     Presets.DPS_SPEC_PRIORITY, wornInv, bankOnly);
		addPriorityWarning(problems, "defence spec", Presets.DEF_SPEC_PRIORITY, wornInv, bankOnly);

		return problems;
	}

	/** Pre-pot boost matrix: Strength (all) / +Ranged (all but scythe-MDPS) / +Magic (freeze). */
	private void addPrePotProblems(List<String> problems, Role role, boolean scythe)
	{
		if (!boosted(Skill.STRENGTH))
			problems.add("Pre-pot: Strength not boosted");

		boolean needsRanged = !(role == Role.MELEE && scythe);
		if (needsRanged && !boosted(Skill.RANGED))
			problems.add("Pre-pot: Ranged not boosted");

		boolean needsMagic = role == Role.NORTH_FREEZE || role == Role.SOUTH_FREEZE;
		if (needsMagic && !boosted(Skill.MAGIC))
			problems.add("Pre-pot: Magic not boosted");
	}

	/** HP overheal (3-state): below max and at-max both flag; overhealed passes silently. */
	private void addHpProblems(List<String> problems)
	{
		int hp    = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
		if (hp < maxHp)
			problems.add("HP below max \u2014 heal up (" + hp + "/" + maxHp + ")");
		else if (hp == maxHp)
			problems.add("HP not overhealed \u2014 eat Anglerfish");
	}

	private void runDump()
	{
		List<String> lines = new ArrayList<>();
		lines.add("=== TOBDUMP ===");

		ItemContainer eq = client.getItemContainer(InventoryID.EQUIPMENT);
		if (eq != null)
		{
			StringBuilder sb = new StringBuilder("EQUIP: ");
			for (Item item : eq.getItems())
				if (item.getId() > 0)
					sb.append(item.getId()).append("=").append(itemName(item.getId())).append("  ");
			lines.add(sb.toString());
		}

		Map<Integer, Integer> inv = containerCounts(InventoryID.INVENTORY);
		if (!inv.isEmpty())
		{
			StringBuilder sb = new StringBuilder("INV: ");
			for (Map.Entry<Integer, Integer> e : inv.entrySet())
				sb.append(e.getKey()).append("=").append(itemName(e.getKey()))
						.append("x").append(e.getValue()).append("  ");
			lines.add(sb.toString());
		}

		lines.add("SPELLBOOK: " + readSpellbook() + " = " + spellbookName(readSpellbook()));

		Map<Integer, Integer> pouch = runePouchContents();
		if (!pouch.isEmpty())
		{
			StringBuilder sb = new StringBuilder("POUCH: ");
			for (Map.Entry<Integer, Integer> e : pouch.entrySet())
				sb.append(e.getKey()).append("=").append(itemName(e.getKey()))
						.append("x").append(e.getValue()).append("  ");
			lines.add(sb.toString());
		}

		int ar = client.getVarpValue(AUTO_RETALIATE_VARP);
		lines.add("AUTO_RETALIATE varp(172) = " + ar + " (" + (ar == 0 ? "ON" : "OFF") + ")");
		lines.add("HP: " + client.getBoostedSkillLevel(Skill.HITPOINTS)
				+ "/" + client.getRealSkillLevel(Skill.HITPOINTS));
		lines.add("STR " + client.getBoostedSkillLevel(Skill.STRENGTH)
				+ "/" + client.getRealSkillLevel(Skill.STRENGTH)
				+ "  RNG " + client.getBoostedSkillLevel(Skill.RANGED)
				+ "/" + client.getRealSkillLevel(Skill.RANGED)
				+ "  MAG " + client.getBoostedSkillLevel(Skill.MAGIC)
				+ "/" + client.getRealSkillLevel(Skill.MAGIC));
		lines.add("=== END DUMP ===");

		for (String line : lines) message(line);
		copyToClipboard(lines);
		message("Copied to clipboard \u2705");
	}

	// ------------------------------------------------------------------
	//  Helpers
	// ------------------------------------------------------------------

	private void addPriorityWarning(List<String> problems, String slotLabel,
	                                List<Set<Integer>> tiers, Set<Integer> wornInv, Set<Integer> bankOnly)
	{
		int bestBroughtTier = -1;
		int bestBankTier    = -1;
		for (int i = 0; i < tiers.size(); i++)
		{
			Set<Integer> tier = tiers.get(i);
			if (tier.stream().anyMatch(wornInv::contains)  && bestBroughtTier == -1) bestBroughtTier = i;
			if (tier.stream().anyMatch(bankOnly::contains) && bestBankTier    == -1) bestBankTier    = i;
		}
		if (bestBankTier != -1 && (bestBroughtTier == -1 || bestBankTier < bestBroughtTier))
			problems.add("Better " + slotLabel + " in bank: " + firstItemName(tiers.get(bestBankTier)));
	}

	private String firstItemName(Set<Integer> ids)
	{
		for (int id : ids) return itemName(id);
		return "upgrade";
	}

	private int equipSlotOf(int itemId, String label)
	{
		try
		{
			ItemStats stats = itemManager.getItemStats(itemId);
			if (stats != null)
			{
				ItemEquipmentStats eq = stats.getEquipment();
				if (eq != null) return eq.getSlot();
			}
		}
		catch (RuntimeException ignored) { }
		return labelSlot(label);
	}

	private int labelSlot(String label)
	{
		String l = label == null ? "" : label.toLowerCase();
		if (l.contains("helm") || l.contains("head"))                                      return 0;
		if (l.contains("cape") || l.contains("ava") || l.contains("quiver"))              return 1;
		if (l.contains("amulet") || l.contains("torture") || l.contains("anguish")
				|| l.contains("fury") || l.contains("occult") || l.contains("neck"))      return 2;
		if (l.contains("ammo") || l.contains("arrow") || l.contains("bolt")
				|| l.contains("blessing") || l.contains("rada"))                          return 13;
		if (l.contains("weapon") || l.contains("scythe") || l.contains("bow")
				|| l.contains("blowpipe") || l.contains("staff") || l.contains("blade")
				|| l.contains("sceptre") || l.contains("tentacle") || l.contains("eye"))  return 3;
		if (l.contains("body") || l.contains("top") || l.contains("chest")
				|| l.contains("torso"))                                                    return 4;
		if (l.contains("shield") || l.contains("defender") || l.contains("ward")
				|| l.contains("book"))                                                     return 5;
		if (l.contains("leg") || l.contains("robe") || l.contains("tasset"))              return 7;
		if (l.contains("glove") || l.contains("hand") || l.contains("vambrace"))          return 9;
		if (l.contains("boot") || l.contains("feet") || l.contains("tread"))              return 10;
		if (l.contains("ring"))                                                            return 12;
		return -1;
	}

	private int readSpellbook()
	{
		return client.getVarbitValue(Varbits.SPELLBOOK);
	}

	private String spellbookName(int id)
	{
		switch (id)
		{
			case 0: return "Standard";
			case 1: return "Ancient";
			case 2: return "Lunar";
			case 3: return "Arceuus";
			default: return "Unknown(" + id + ")";
		}
	}

	private String itemName(int id)
	{
		try
		{
			String n = itemManager.getItemComposition(id).getName();
			return (n == null || n.isEmpty()) ? "item#" + id : n;
		}
		catch (RuntimeException e) { return "item#" + id; }
	}

	private void copyToClipboard(List<String> lines)
	{
		try
		{
			StringSelection sel = new StringSelection(String.join("\n", lines));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		}
		catch (RuntimeException ignored) { }
	}

	private java.awt.image.BufferedImage createIcon()
	{
		java.awt.image.BufferedImage icon =
				new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = icon.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
				java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new java.awt.Color(0x9B30C0));
		g.fillRoundRect(1, 3, 14, 11, 4, 4);
		g.setColor(new java.awt.Color(0xF7CD45));
		g.fillRect(3, 6, 10, 7);
		g.setColor(new java.awt.Color(0x5A3A22));
		g.fillRect(3, 8, 10, 2);
		g.setColor(new java.awt.Color(0xF7CD45));
		g.fillRect(7, 8, 2, 3);
		g.dispose();
		return icon;
	}

	private void message(String text)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[Learner ToB] " + text, null);
	}
}