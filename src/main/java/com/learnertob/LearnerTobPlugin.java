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
import net.runelite.api.GroundObject;
import net.runelite.api.JagexColor;
import net.runelite.api.InventoryID;
import net.runelite.api.Scene;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.ItemContainer;
import net.runelite.api.Renderable;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Prayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PreMapLoad;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PluginDescriptor(
		name = "Learner ToB",
		description = "Bank-driven gear checks for learning Theatre of Blood",
		tags = {"tob", "theatre", "blood", "raid", "learner", "pvm"},
		configName = "learnertob"
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
	@Inject private Hooks hooks;
	@Inject private LearnerTobConfig config;
	@Inject private ConfigManager configManager;
	@Inject private ItemManager itemManager;
	@Inject private OverlayManager overlayManager;
	@Inject private MouseManager mouseManager;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ClientThread clientThread;
	@Inject private GearCheckOverlay overlay;
	@Inject private TileMarkerOverlay tileOverlay;
	@Inject private LearnerTobPanel panel;

	private NavigationButton navButton;
	private boolean lastScytheSetup = true;
	private boolean lastOathWhip = false;
	private Item[] bankSnapshot = new Item[0];

	private static final Set<Integer> BLOAT_IDS = new HashSet<>(java.util.Arrays.asList(
			10812,  // Entry
			10813,  // Hard
			8359)); // Normal
	private static final Set<Integer> BLOAT_FLOOR_IDS = new HashSet<>(java.util.Arrays.asList(
			32941, 32942, 32943, 32944, 32945, 32946, 32947, 32948));
	// Maiden phases by NPC id (she transforms at 70/50/30%). Normal + Story mode.
	private static final Set<Integer> MAIDEN_IDS = new HashSet<>(java.util.Arrays.asList(
			8360, 8361, 8362, 8363, 8364, 8365,        // Normal (6 phases; 8360-8363 wiki-confirmed, 8364/8365 by observed pattern — verify in-game)
			10814, 10815, 10816, 10817, 10818, 10819,  // Entry (all 6 confirmed via in-game NPC dump)
			10822));                                    // Hard base (confirmed; later Hard phases need in-game dump to confirm)
	// Nylocas Matomenos (the nylos freezers target): Maiden room, all modes.
	private static final Set<Integer> NYLO_IDS = new HashSet<>(java.util.Arrays.asList(
			8366,    // Normal Maiden
			10820,   // Entry Maiden
			10828)); // Hard Maiden
	// Blood spawns (all roles): Entry + Normal.
	private static final Set<Integer> BLOOD_IDS = new HashSet<>(java.util.Arrays.asList(8367, 10821, 10829));
	private boolean maiden75, maiden55, maiden35, maiden0;

	private boolean tileRecording = false;
	private final Set<WorldPoint> recordedTiles = new HashSet<>();

	// Role-specific "stand here" boxes in the Maiden room: {minX, maxX, minY, maxY}, plane 0.
	private static final int[] BOX_MDPS_NOSCY = {3166, 3169, 4452, 4455};
	private static final int[] BOX_RDPS_NOSCY = {3166, 3169, 4437, 4440};
	private static final int[] BOX_NFRZ       = {3168, 3171, 4452, 4455};
	private static final int[] BOX_SFRZ       = {3169, 3172, 4438, 4441};

	// Reusable proximity zones. The raid-start door is the first; Phase 3
	// rooms each add their own ZoneTrigger here.
	private final List<ZoneTrigger> zones = new ArrayList<>();
	private ZoneTrigger maidenZone;
	private boolean maidenPromptActive = false;
	private boolean maidenInBox = false;        // in the setup box last tick
	private boolean maidenSetupHandled = false; // shown + resolved this raid
	private ZoneTrigger maidenPrayerZone;
	private boolean maidenPrayerHandled = false; // prayer prompt shown/resolved this raid
	private boolean maidenPrayerActive = false;  // comply prayer popup showing
	private boolean maidenInPrayerBox = false;
	private boolean maidenPrayerArmed = false; // stay-mode: armed on entry, holds until correct

	// Bloat per-raid state
	private ZoneTrigger bloatSetupZone;
	private ZoneTrigger bloatPrayerZone;
	private boolean bloatSetupHandled = false;
	private boolean bloatSetupPromptActive = false;
	private boolean bloatSetupArmed = false;
	private boolean bloatPrayerHandled = false;
	private boolean bloatPrayerArmed = false;
	private boolean bloatPrayerActive = false;
	private boolean bloatPostHandled = false;
	private boolean bloatPostArmed = false;
	private boolean bloatPostPromptActive = false;
	private boolean bloatNpcPresent = false;
	private NPC bloatNpc = null;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	@Provides
	LearnerTobConfig provideConfig(ConfigManager cm)
	{
		return cm.getConfig(LearnerTobConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(tileOverlay);
		mouseManager.registerMouseListener(this);
		hooks.registerRenderableDrawListener(drawListener);

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
		// Raid-start door lobby box (inclusive bounds), plane 0. Fires only on a
		// walk-in from the WEST (left) edge, so leaving the raid (a teleport back
		// into the lobby) doesn't re-trigger it.
		zones.add(new ZoneTrigger("Raid entry", 3666, 3671, 3215, 3223, 0,
				this::raidEntryProblems, ZoneTrigger.EntrySide.WEST));

		// Maiden start box (region 12613). Handled separately from the click
		// zones above: it shows a COMPLY prompt that stays until the player
		// drops Salve and equips their maul/hammer.
		maidenZone = new ZoneTrigger("Maiden \u2014 setup", 3186, 3194, 4443, 4448, 0,
				this::maidenSetupSteps);

		// Maiden prayer box (just west of the setup box). Single flashing prompt
		// on entry telling you which prayers to flick.
		maidenPrayerZone = new ZoneTrigger("Maiden \u2014 prayers", 3181, 3184, 4443, 4450, 0,
				java.util.Collections::emptyList);

		// Bloat setup box: equip Crystal Halberd and Salve.
		bloatSetupZone = new ZoneTrigger("Bloat \u2014 setup", 3175, 3180, 4422, 4432, 0,
				java.util.Collections::emptyList);
		// Bloat prayer box: arm Ranged + Piety prayers.
		bloatPrayerZone = new ZoneTrigger("Bloat \u2014 prayers", 3305, 3305, 4446, 4449, 0,
				java.util.Collections::emptyList);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(tileOverlay);
		mouseManager.unregisterMouseListener(this);
		hooks.unregisterRenderableDrawListener(drawListener);
		clientToolbar.removeNavigation(navButton);
		overlay.dismiss();
	}

	// ------------------------------------------------------------------
	//  Container helpers
	// ------------------------------------------------------------------

	/**
	 * Testing helper: item IDs the plugin should pretend you do NOT own, so you
	 * can walk other roles/setups without banking gear. Built from the Testing
	 * config section. Empty unless a hide toggle (or the ID field) is set.
	 */
	private Set<Integer> hiddenIds()
	{
		Set<Integer> hidden = new HashSet<>();
		if (config.testHideScythe())   hidden.addAll(Presets.SCYTHE_ANY);
		if (config.testHideOathplate())
		{
			hidden.addAll(Presets.HELM_OATH);
			hidden.addAll(Presets.OATH_CHEST);
			hidden.addAll(Presets.OATH_LEGS);
		}
		if (config.testHideTentacle()) hidden.add(Presets.ABYSSAL_TENTACLE);
		for (String tok : config.testHideIds().split(","))
		{
			tok = tok.trim();
			if (tok.isEmpty()) continue;
			try { hidden.add(Integer.parseInt(tok)); }
			catch (NumberFormatException ignored) { }
		}
		return hidden;
	}

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
		Item[] items;
		if (which == InventoryID.BANK)
			items = bankSnapshot;
		else
		{
			ItemContainer c = client.getItemContainer(which);
			if (c == null) return;
			items = c.getItems();
		}
		Set<Integer> hidden = hiddenIds();
		for (Item i : items)
			if (i.getId() > 0 && !hidden.contains(i.getId())) ids.add(i.getId());
	}

	private Map<Integer, Integer> containerCounts(InventoryID which)
	{
		Map<Integer, Integer> map = new LinkedHashMap<>();
		ItemContainer c = client.getItemContainer(which);
		if (c == null) return map;
		Set<Integer> hidden = hiddenIds();
		for (Item i : c.getItems())
			if (i.getId() > 0 && !hidden.contains(i.getId()))
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
				// Arceuus: Fire, Aether, Blood, Death
				runes.put(554,   "Fire");   // Fire rune
				runes.put(30843, "Aether"); // Aether rune
				runes.put(565,   "Blood");  // Blood rune
				runes.put(560,   "Death");  // Death rune
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
		Role role = config.role();
		if (!owned.isEmpty())
		{
			lastScytheSetup = Presets.hasScythe(owned);
			lastOathWhip    = computeOathWhip(role, lastScytheSetup, owned);
			String label = setupLabel(lastScytheSetup, lastOathWhip);
			if (!hiddenIds().isEmpty()) label += "  [TEST]";
			panel.setSetupLabel(label);
		}

		Set<Integer> ready = readyIds();
		Set<Integer> bank  = bankOnlyIds();
		Map<Integer, Integer> pouchContents = runePouchContents();

		boolean scythe   = lastScytheSetup;
		boolean oathWhip = lastOathWhip;

		int expectedBook = Presets.expectedSpellbook(role);
		boolean bookOk   = expectedBook == readSpellbook();
		String  bookName = spellbookName(expectedBook);

		List<SlotReq> reqs = Presets.requirements(role, scythe, oathWhip);
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
		if (id == InventoryID.BANK.getId())
			bankSnapshot = event.getItemContainer().getItems().clone();
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
		if (overlay.isVisible() && overlay.clickCloses() && overlay.containsPoint(e.getPoint()))
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
		Player local = client.getLocalPlayer();
		if (local == null)
			return;

		// Per-raid state clears only when you're out of the Theatre instance
		// (in the lobby), NOT on every LOADING — rooms transition mid-raid.
		if (!client.isInInstancedRegion())
		{
			maidenSetupHandled = false;
			maidenPrayerHandled = false;
			maidenInPrayerBox = false;
			maidenPrayerArmed = false;
			maiden75 = maiden55 = maiden35 = maiden0 = false;

			bloatSetupHandled = false;
			bloatSetupArmed = false;
			bloatPrayerHandled = false;
			bloatPrayerArmed = false;
			bloatPostHandled = false;
			bloatPostArmed = false;
			bloatNpcPresent = false;
			bloatNpc = null;
		}

		WorldPoint wp = playerWorldPoint(local);
		if (wp == null)
			return;

		if (tileRecording)
			recordedTiles.add(wp);

		// Raid-start door — push-style, click-to-close.
		if (config.raidEntryCheck())
		{
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

		// Maiden prayers — single flashing prompt on entry.
		updateMaidenPrayer(wp);

		// Maiden setup — comply-style, stays up until done.
		updateMaidenPrompt(wp);

		// Maiden HP call-outs (75 / 55 / 35%).
		updateMaidenHp();

		// Role-specific standing box on the floor.
		updateMaidenTiles();

		// Bloat room prompts and markers.
		updateBloat(wp);
	}

	/**
	 * Pushes the standing-box for the current role/setup to the tile overlay,
	 * shown only while Maiden is in the room. Scythe setups melee her and get
	 * no box; the no-scythe (ranged) setups and freezers each get their spot.
	 */
	private void updateMaidenTiles()
	{
		if (!config.maidenTileMarkers())
		{
			tileOverlay.set(null, null, null);
			return;
		}

		Role role = config.role();
		boolean freeze = role == Role.NORTH_FREEZE || role == Role.SOUTH_FREEZE;

		NPC maiden = null;
		List<TileMarkerOverlay.Mark> nylos = new ArrayList<>();
		List<TileMarkerOverlay.Mark> bloods = new ArrayList<>();

		// Single pass over the NPCs: find Maiden (the room gate), and collect the
		// true tiles for nylos (freeze roles only) and blood spawns (everyone).
		for (NPC npc : client.getNpcs())
		{
			if (npc == null)
				continue;
			int id = npc.getId();
			if (MAIDEN_IDS.contains(id))
				maiden = npc;

			WorldPoint wl = npc.getWorldLocation(); // server (true) SW tile
			if (wl == null)
				continue;
			NPCComposition comp = npc.getTransformedComposition();
			int size = comp != null ? comp.getSize() : 1;
			if (freeze && NYLO_IDS.contains(id))
				nylos.add(new TileMarkerOverlay.Mark(wl, size));
			if (BLOOD_IDS.contains(id))
				bloods.add(new TileMarkerOverlay.Mark(wl, size));
		}

		// Everything only shows while Maiden is in the room.
		int[] box = null;
		if (maiden != null)
		{
			switch (role)
			{
				case MELEE:        if (!lastScytheSetup) box = BOX_MDPS_NOSCY; break;
				case RANGED:       if (!lastScytheSetup) box = BOX_RDPS_NOSCY; break;
				case NORTH_FREEZE: box = BOX_NFRZ; break;
				case SOUTH_FREEZE: box = BOX_SFRZ; break;
				default: break;
			}
		}
		else
		{
			nylos.clear();
			bloods.clear();
		}

		tileOverlay.set(box, nylos, bloods);
	}

	// ------------------------------------------------------------------
	//  Bloat room engine
	// ------------------------------------------------------------------

	private void updateBloat(WorldPoint wp)
	{
		NPC bloat = findBloat();
		bloatNpcPresent = (bloat != null);

		updateBloatTiles(bloat);
		updateBloatSetup(wp);
		updateBloatPrayer(wp);
		updateBloatPost(wp);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (BLOAT_IDS.contains(event.getNpc().getId()))
			bloatPostArmed = true;
	}

	private NPC findBloat()
	{
		for (NPC npc : client.getNpcs())
		{
			if (npc != null && BLOAT_IDS.contains(npc.getId()))
			{
				bloatNpc = npc;
				return npc;
			}
		}
		bloatNpc = null;
		return null;
	}

	public NPC getBloatNpc() { return bloatNpc; }

	private void updateBloatTiles(NPC bloat)
	{
		tileOverlay.setBloat(config.bloatTileMarker() ? bloat : null);
	}

	/**
	 * Recolors Bloat danger floor tiles before the scene is uploaded to GPU.
	 * PreMapLoad fires before SceneUploader runs, so SceneTilePaint edits
	 * render immediately on the next frame without needing setSceneId(0).
	 *
	 * Two passes per matching tile:
	 *   1) SceneTilePaint corner colors — paints the terrain itself.
	 *   2) setGroundObject(null) — removes the blood-splatter decals (IDs 32941-32948).
	 *
	 * Area: outer box 3288-3303 x 4440-4455, minus inner chamber 3293-3298 x 4445-4450.
	 * Config change triggers a LOADING state reload so the new color takes effect.
	 */
	@Subscribe
	public void onPreMapLoad(PreMapLoad event)
	{
		applyBloatFloorRecolor(event.getScene());
	}

	private void applyBloatFloorRecolor(Scene scene)
	{
		if (!config.bloatRecolorFloor()) return;
		int hsl = getSafeHsl(config.bloatFloorColor());
		Tile[][] plane0 = scene.getTiles()[0];
		for (Tile[] row : plane0)
		{
			for (Tile tile : row)
			{
				if (tile == null) continue;
				WorldPoint wp = WorldPoint.fromLocalInstance(scene, tile.getLocalLocation(), tile.getPlane());
				if (wp == null) continue;
				boolean inOuter = wp.getX() >= 3288 && wp.getX() <= 3303
						&& wp.getY() >= 4440 && wp.getY() <= 4455;
				if (!inOuter) continue;
				boolean inInner = wp.getX() >= 3293 && wp.getX() <= 3298
						&& wp.getY() >= 4445 && wp.getY() <= 4450;
				if (inInner) continue;

				SceneTilePaint paint = tile.getSceneTilePaint();
				if (paint != null)
				{
					paint.setNwColor(hsl);
					paint.setNeColor(hsl);
					paint.setSwColor(hsl);
					paint.setSeColor(hsl);
					tile.setSceneTilePaint(paint);
				}

				GroundObject obj = tile.getGroundObject();
				if (obj != null && BLOAT_FLOOR_IDS.contains(obj.getId()))
					tile.setGroundObject(null);
			}
		}
	}

	/** Reloads the scene when the floor recolor toggle or color is changed. */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!LearnerTobConfig.GROUP.equals(event.getGroup())) return;
		String key = event.getKey();
		if ("bloatRecolorFloor".equals(key) || "bloatFloorColor".equals(key))
		{
			clientThread.invokeLater(() -> {
				if (client.getGameState() == GameState.LOGGED_IN)
					client.setGameState(GameState.LOADING);
			});
		}
	}

	/**
	 * Converts an AWT Color to a Jagex HSL int safe for SceneTilePaint.
	 * Some colors (pure blue, magenta, black) convert to HSL=0 which renders
	 * black; this clamps lightness to at least 1 and falls back to mid-grey
	 * if the result is still zero.
	 */
	private int getSafeHsl(Color color)
	{
		int rgb = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
		int hsl = JagexColor.rgbToHSL(rgb, 1.0d);
		if (hsl > 0) return hsl;
		int hue   = (hsl >> 10) & 0x3F;
		int sat   = (hsl >> 7)  & 0x07;
		int light = Math.max(1, hsl & 0x7F);
		hsl = (hue << 10) | (sat << 7) | light;
		if (hsl <= 0) hsl = JagexColor.rgbToHSL(0x707070, 1.0d);
		return hsl;
	}

	/**
	 * Entry setup prompt: armed on entering the setup box, stays until Crystal
	 * Halberd and Salve (e) are both present. Once per raid.
	 *
	 * Uses the same armed pattern as updateBloatPrayer. When both are active,
	 * prayer overwrites setup each tick (prayer is called after setup in updateBloat)
	 * and takes visual precedence — once prayers are done, setup shows alone.
	 */
	private void updateBloatSetup(WorldPoint wp)
	{
		if (!config.bloatSetupCheck()) return;
		if (bloatSetupHandled) return;
		boolean inBox = bloatSetupZone.contains(wp);
		if (inBox) bloatSetupArmed = true;
		if (!bloatSetupArmed) return;

		List<String> steps = bloatSetupSteps();
		if (steps.isEmpty())
		{
			if (bloatSetupPromptActive) { overlay.dismiss(); bloatSetupPromptActive = false; }
			bloatSetupHandled = true;
			return;
		}

		overlay.showResult("Bloat — setup", steps, GearCheckOverlay.DismissMode.COMPLY, 0, true);
		bloatSetupPromptActive = true;
	}

	private List<String> bloatSetupSteps()
	{
		List<String> steps = new ArrayList<>();
		Map<Integer, Integer> worn = containerCounts(InventoryID.EQUIPMENT);
		if (!worn.containsKey(Presets.CRYSTAL_HALBERD))
			steps.add("Equip Crystal Halberd");
		if (!worn.containsKey(Presets.SALVE_AMULET_E))
			steps.add("Equip Salve (e)");
		return steps;
	}

	/**
	 * Prayer prompt: armed on entering the prayer box, stays until both
	 * Protect from Missiles and Piety are active. Once per raid.
	 */
	private void updateBloatPrayer(WorldPoint wp)
	{
		if (!config.bloatPrayerPrompt()) return;
		if (bloatPrayerHandled) return;

		boolean inBox = bloatPrayerZone.contains(wp);
		if (inBox) bloatPrayerArmed = true;
		if (!bloatPrayerArmed) return;

		List<String> steps = bloatPrayerSteps();
		if (steps.isEmpty())
		{
			if (bloatPrayerActive) { overlay.dismiss(); bloatPrayerActive = false; }
			bloatPrayerHandled = true;
			return;
		}

		overlay.showResult("Bloat — prayers", steps, GearCheckOverlay.DismissMode.COMPLY, 0, true);
		bloatPrayerActive = true;
	}

	private List<String> bloatPrayerSteps()
	{
		List<String> steps = new ArrayList<>();
		if (!client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
			steps.add("Pray Ranged");
		if (!client.isPrayerActive(Prayer.PIETY))
			steps.add("Pray Piety");
		return steps;
	}

	/**
	 * Post-Bloat reminders: armed when the player enters the post box with Bloat
	 * already dead. Stays until Salve is dropped and a Stamina potion is in inventory.
	 * Once per raid.
	 */
	private void updateBloatPost(WorldPoint wp)
	{
		if (!config.bloatPostRoomReminders()) return;
		if (bloatPostHandled) return;

		if (!bloatPostArmed) return;

		// Cancel if the player has left the Bloat room region (entered Nylocas or beyond).
		boolean inBloatRoom = wp.getX() >= 3269 && wp.getX() <= 3322
				&& wp.getY() >= 4441 && wp.getY() <= 4462;
		if (!inBloatRoom)
		{
			if (bloatPostPromptActive) { overlay.dismiss(); bloatPostPromptActive = false; }
			bloatPostHandled = true;
			return;
		}

		List<String> steps = bloatPostSteps();
		if (steps.isEmpty())
		{
			if (bloatPostPromptActive) { overlay.dismiss(); bloatPostPromptActive = false; }
			bloatPostHandled = true;
			return;
		}

		overlay.showResult("Bloat — after room", steps, GearCheckOverlay.DismissMode.COMPLY, 0, true);
		bloatPostPromptActive = true;
	}

	private List<String> bloatPostSteps()
	{
		List<String> steps = new ArrayList<>();
		Map<Integer, Integer> worn = containerCounts(InventoryID.EQUIPMENT);
		Map<Integer, Integer> inv  = containerCounts(InventoryID.INVENTORY);
		if (worn.containsKey(Presets.SALVE_AMULET_E) || inv.containsKey(Presets.SALVE_AMULET_E))
			steps.add("Drop Salve");
		if (!inv.containsKey(Presets.STAMINA_POTION_4))
			steps.add("Buy Stamina (4)");
		return steps;
	}

	/**
	 * Returns false for other players while Bloat is present and bloatHidePlayers is on.
	 * All other renderables pass through (return true = draw normally).
	 */
	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (config.bloatHidePlayers() && bloatNpcPresent && renderable instanceof Player)
		{
			Player p = (Player) renderable;
			return p == client.getLocalPlayer();
		}
		return true;
	}

	/**
	 * Player world point that works inside instances. In an instanced region
	 * getWorldLocation() returns dynamic coords; the room boxes are defined in
	 * template coords, so translate via the instance template chunks (same as
	 * RuneLite's own dev-tools location overlay).
	 */
	private WorldPoint playerWorldPoint(Player local)
	{
		LocalPoint lp = local.getLocalLocation();
		if (client.isInInstancedRegion() && lp != null)
			return WorldPoint.fromLocalInstance(client, lp);
		return local.getWorldLocation();
	}

	/** The Maiden NPC currently in the scene, or null. */
	private NPC findMaiden()
	{
		for (NPC npc : client.getNpcs())
			if (npc != null && MAIDEN_IDS.contains(npc.getId()))
				return npc;
		return null;
	}

	/**
	 * Maiden HP call-outs. Health comes as a ratio (0..scale); the server never
	 * sends real HP, so we work in percent. Fires the role-specific prompt once
	 * as she crosses 75 / 55 / 35% (the early warnings before each nylo spawn).
	 */
	private void updateMaidenHp()
	{
		if (!config.maidenHpPrompts())
			return;

		NPC maiden = findMaiden();
		if (maiden == null)
			return; // flags reset on region change (new fight)

		int ratio = maiden.getHealthRatio();
		int scale = maiden.getHealthScale();
		if (ratio < 0 || scale <= 0)
			return; // no health bar yet (not engaged)

		double pct = 100.0 * ratio / scale;
		Role role  = config.role();

		if (!maiden75 && pct <= 75) { maiden75 = true; fireMaidenHp(role, 75); }
		if (!maiden55 && pct <= 55) { maiden55 = true; fireMaidenHp(role, 55); }
		if (!maiden35 && pct <= 35) { maiden35 = true; fireMaidenHp(role, 35); }
		if (!maiden0  && pct <= 0)  { maiden0  = true; fireMaidenHp(role,  0); }
	}

	private void fireMaidenHp(Role role, int threshold)
	{
		overlay.showResult("Maiden \u2014 " + threshold + "%",
				maidenHpText(role, threshold),
				GearCheckOverlay.DismissMode.TIMED, threshold == 0 ? 2 : 3, true);
	}

	/** Role-specific Maiden call-out text for a threshold. */
	private List<String> maidenHpText(Role role, int t)
	{
		if (t == 0)
			return java.util.Collections.singletonList("Drink Divine Super Combat");
		switch (role)
		{
			case NORTH_FREEZE:
			case SOUTH_FREEZE:
				return java.util.Collections.singletonList("Get ready to freeze");
			case RANGED:
				return java.util.Collections.singletonList(
						t == 35 ? "Stay on Maiden" : "Kill S1, then chin the clump");
			case MELEE:
			default:
				return java.util.Collections.singletonList(
						t == 35 ? "Stay on Maiden" : "Kill N1 and N2");
		}
	}

	/**
	 * Maiden prayer prompt: a single flashing reminder, fired once per raid the
	 * first time you stand in the prayer box, telling you which prayers to flick.
	 */
	private void updateMaidenPrayer(WorldPoint wp)
	{
		if (!config.maidenPrayerPrompt())
			return;

		boolean inBox   = maidenPrayerZone.contains(wp);
		boolean correct = prayersCorrect(config.role(), lastScytheSetup);

		if (config.maidenPrayerStay())
		{
			// Stay-until-correct (Option A): arm on entering the prayer box, then
			// hold the prompt until the prayers are actually right \u2014 even after
			// you've walked out of the box. Clears only when correct (or new raid).
			if (maidenPrayerHandled)
				return;
			if (inBox)
				maidenPrayerArmed = true;
			if (!maidenPrayerArmed)
				return;
			if (correct)
			{
				if (maidenPrayerActive) { overlay.dismiss(); maidenPrayerActive = false; }
				maidenPrayerHandled = true;
				return;
			}
			overlay.showResult("Maiden \u2014 prayers",
					prayerText(config.role(), lastScytheSetup),
					GearCheckOverlay.DismissMode.COMPLY, 0, true);
			maidenPrayerActive = true;
		}
		else
		{
			// Flash-once: single timed flash on entry, skipped if already correct.
			if (maidenPrayerHandled || !inBox)
				return;
			if (!correct)
				overlay.showResult("Maiden \u2014 prayers",
						prayerText(config.role(), lastScytheSetup),
						GearCheckOverlay.DismissMode.TIMED, 3, true);
			maidenPrayerHandled = true;
		}
	}

	/** The offensive prayer for the current style. */
	private Prayer offensivePrayer(Role role, boolean scythe)
	{
		switch (role)
		{
			case NORTH_FREEZE:
			case SOUTH_FREEZE:
				return Prayer.AUGURY;
			case MELEE:
			case RANGED:
			default:
				// Only the scythe setup melees Maiden (Piety); every other setup,
				// including MDPS Oathplate-Whip, ranges her (Rigour).
				return scythe ? Prayer.PIETY : Prayer.RIGOUR;
		}
	}

	/** True if Protect from Magic AND the correct offensive prayer are both on. */
	private boolean prayersCorrect(Role role, boolean scythe)
	{
		return client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)
				&& client.isPrayerActive(offensivePrayer(role, scythe));
	}

	/** Protect Magic plus the offensive prayer for the current style. */
	private List<String> prayerText(Role role, boolean scythe)
	{
		Prayer off  = offensivePrayer(role, scythe);
		String name = off == Prayer.AUGURY ? "Augury" : off == Prayer.PIETY ? "Piety" : "Rigour";
		return java.util.Collections.singletonList("Pray Magic and " + name);
	}

	/**
	 * Maiden entry prompt: while standing in the Maiden start box, show the
	 * remaining setup steps (drop Salve, equip maul/hammer) and clear the popup
	 * the moment they're all done. Re-evaluated every tick so it tracks progress.
	 */
	private void updateMaidenPrompt(WorldPoint wp)
	{
		// Prayers take precedence: while that comply prompt is up, hold the setup
		// prompt so the two don't fight over the popup.
		if (maidenPrayerActive)
			return;

		boolean inBox = config.maidenSetupCheck() && maidenZone.contains(wp);

		if (!inBox)
		{
			if (maidenPromptActive)
			{
				overlay.dismiss();
				maidenPromptActive = false;
			}
			// Leaving the box (after having been in it) ends the setup window, so
			// walking back out the same way later won't re-trigger the prompt.
			if (maidenInBox)
				maidenSetupHandled = true;
			maidenInBox = false;
			return;
		}

		maidenInBox = true;
		if (maidenSetupHandled)
			return; // already shown + resolved this raid

		List<String> steps = maidenSetupSteps();
		if (steps.isEmpty())
		{
			if (maidenPromptActive)
			{
				overlay.dismiss();
				maidenPromptActive = false;
			}
			maidenSetupHandled = true; // complied
			return;
		}

		overlay.showResult("Maiden \u2014 setup", steps,
				GearCheckOverlay.DismissMode.COMPLY, 0, true);
		maidenPromptActive = true;
	}

	/** Remaining Maiden setup steps; empty = complied. */
	private List<String> maidenSetupSteps()
	{
		List<String> steps = new ArrayList<>();
		Map<Integer, Integer> worn = containerCounts(InventoryID.EQUIPMENT);
		Map<Integer, Integer> inv  = containerCounts(InventoryID.INVENTORY);

		boolean salveGone = !worn.containsKey(Presets.SALVE_AMULET_E)
				&& !inv.containsKey(Presets.SALVE_AMULET_E);
		if (!salveGone)
			steps.add("Drop Salve");

		boolean maulEquipped = false;
		for (int id : Presets.DEF_SPEC)
			if (worn.containsKey(id)) { maulEquipped = true; break; }
		if (!maulEquipped)
			steps.add("Equip " + defSpecName());

		return steps;
	}

	/** Names the player's owned defence-reduction spec weapon for the prompt. */
	private String defSpecName()
	{
		Set<Integer> owned = ownedIds();
		if (owned.contains(Presets.ELDER_MAUL))      return "Elder Maul";
		if (owned.contains(Presets.DRAGON_WARHAMMER)) return "Dragon Warhammer";
		return "Maul/Hammer";
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
			overlay.dismiss();
			maidenPromptActive = false;
			maidenInBox = false;
			maidenPrayerActive = false;
			bloatSetupPromptActive = false;
			bloatPrayerActive = false;
			bloatPostPromptActive = false;
		}
	}

	/**
	 * Raid-start door check — runs the SAME full check as the manual gear check
	 * (gear/consumables, runes, spellbook, retaliate, boost matrix, HP overheal),
	 * so nothing is missed at the door. Push-style: silent unless there's a problem.
	 */
	private List<String> raidEntryProblems()
	{
		return collectProblems(config.role(), lastScytheSetup, lastOathWhip);
	}

	/** MDPS-only third setup: no scythe, a full Oathplate set owned, and a tentacle. */
	private boolean computeOathWhip(Role role, boolean scythe, Set<Integer> owned)
	{
		return role == Role.MELEE && !scythe
				&& Presets.hasFullOathplate(owned)
				&& owned.contains(Presets.ABYSSAL_TENTACLE);
	}

	/** Panel/title setup label for the current state. */
	private String setupLabel(boolean scythe, boolean oathWhip)
	{
		if (scythe) return "Scythe";
		return oathWhip ? "Oathplate Whip" : "No Scythe";
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
			case "tobcheck":  runCheck();            break;
			case "tobdump":   runDump();             break;
			case "tobrecord": toggleTileRecording(); break;
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
		Role role        = config.role();
		boolean scythe   = lastScytheSetup;
		boolean oathWhip = lastOathWhip;

		List<String> problems = collectProblems(role, scythe, oathWhip);

		String title = problems.isEmpty()
				? "Gear check \u2014 no issues"
				: role + " " + setupLabel(scythe, oathWhip) + " \u2014 " + problems.size() + " issue(s)";

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
	private List<String> collectProblems(Role role, boolean scythe, boolean oathWhip)
	{
		Map<Integer, Integer> wornCounts = containerCounts(InventoryID.EQUIPMENT);
		Map<Integer, Integer> invCounts  = containerCounts(InventoryID.INVENTORY);

		List<String> problems = new ArrayList<>();

		// Gear + consumables (equipped vs. carried).
		for (SlotReq req : Presets.requirements(role, scythe, oathWhip))
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

		// Uncharged weapon warnings — passes the gear check above but can't hit.
		{
			Set<Integer> worn = new HashSet<>(wornCounts.keySet());
			worn.addAll(invCounts.keySet());
			boolean hasMage    = worn.stream().anyMatch(Presets.MAGE_ANY::contains);
			boolean hasCharged = worn.stream().anyMatch(Presets.MAGE_CHARGED::contains);
			if (hasMage && !hasCharged)
				problems.add("Charge your mage weapon before the raid");
			boolean hasScythe        = worn.stream().anyMatch(Presets.SCYTHE_ANY::contains);
			boolean hasChargedScythe = worn.stream().anyMatch(Presets.SCYTHE_CHARGED::contains);
			if (hasScythe && !hasChargedScythe)
				problems.add("Charge your scythe before the raid");
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
		addPriorityWarning(problems, "DPS spec",     Presets.DPS_SPEC_PRIORITY,  wornInv, bankOnly);
		addPriorityWarning(problems, "defence spec", Presets.DEF_SPEC_PRIORITY,  wornInv, bankOnly);
		addPriorityWarning(problems, "boots",        Presets.BOOTS_PRIORITY,     wornInv, bankOnly);
		addPriorityWarning(problems, "blessing",     Presets.BLESSING_PRIORITY,  wornInv, bankOnly);
		if (!scythe)
			addPriorityWarning(problems, "defender", Presets.DEFENDER_PRIORITY, wornInv, bankOnly);
		if (role == Role.NORTH_FREEZE || role == Role.SOUTH_FREEZE)
			addPriorityWarning(problems, "freeze staff", Presets.FREEZE_STAFF_PRIORITY, wornInv, bankOnly);

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

	private void toggleTileRecording()
	{
		if (!tileRecording)
		{
			tileRecording = true;
			recordedTiles.clear();
			message("Tile recording started \u2014 type ::tobrecord again to stop.");
			return;
		}

		tileRecording = false;

		if (recordedTiles.isEmpty())
		{
			message("Tile recording stopped \u2014 no tiles recorded.");
			return;
		}

		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int plane = 0;
		for (WorldPoint p : recordedTiles)
		{
			if (p.getX() < minX) minX = p.getX();
			if (p.getX() > maxX) maxX = p.getX();
			if (p.getY() < minY) minY = p.getY();
			if (p.getY() > maxY) maxY = p.getY();
			plane = p.getPlane();
		}

		String result = "Box: minX=" + minX + " maxX=" + maxX
				+ " minY=" + minY + " maxY=" + maxY
				+ " plane=" + plane + " | Tiles: " + recordedTiles.size();
		message(result);
		try
		{
			StringSelection sel = new StringSelection(result);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
		}
		catch (RuntimeException ignored) { }
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