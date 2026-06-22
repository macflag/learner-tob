package com.learnertob;

import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Varbits;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
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
	@Inject private Client client;
	@Inject private LearnerTobConfig config;
	@Inject private ConfigManager configManager;
	@Inject private ItemManager itemManager;
	@Inject private OverlayManager overlayManager;
	@Inject private MouseManager mouseManager;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ClientThread clientThread;
	@Inject private GearCheckOverlay overlay;
	@Inject private BankHighlightOverlay bankOverlay;
	@Inject private LearnerTobPanel panel;

	private NavigationButton navButton;
	private boolean lastScytheSetup = true;

	@Provides
	LearnerTobConfig provideConfig(ConfigManager cm) { return cm.getConfig(LearnerTobConfig.class); }

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(bankOverlay);
		mouseManager.registerMouseListener(this);

		java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = icon.createGraphics();
		g.setColor(new java.awt.Color(0xF7CD45));
		g.fillOval(0, 0, 15, 15);
		g.setColor(new java.awt.Color(0x001DF5));
		g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
		g.drawString("T", 4, 11);
		g.dispose();

		navButton = NavigationButton.builder()
				.tooltip("Learner ToB")
				.icon(icon)
				.priority(8)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);

		panel.setOnFilterToggle(() -> clientThread.invokeLater(this::updateHighlights));
		panel.setRole(config.role());
		panel.setOnRoleChange(role -> {
			configManager.setConfiguration(LearnerTobConfig.GROUP, "role", role);
			clientThread.invokeLater(this::refreshFromOwned);
		});
		clientThread.invokeLater(this::refreshFromOwned);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(bankOverlay);
		mouseManager.unregisterMouseListener(this);
		clientToolbar.removeNavigation(navButton);
		overlay.dismiss();
	}

	// =====================================================================
	//  OWNED ITEMS — bank + equipment + inventory
	// =====================================================================
	private Set<Integer> ownedIds()
	{
		Set<Integer> ids = new HashSet<>();
		addContainer(ids, InventoryID.BANK);
		addContainer(ids, InventoryID.EQUIPMENT);
		addContainer(ids, InventoryID.INVENTORY);
		return ids;
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

	private void addContainer(Set<Integer> ids, InventoryID which)
	{
		ItemContainer c = client.getItemContainer(which);
		if (c == null) return;
		for (Item i : c.getItems()) if (i.getId() > 0) ids.add(i.getId());
	}

	// Quantity-aware owned map (for potion/food counts)
	private Map<Integer, Integer> ownedCounts()
	{
		Map<Integer, Integer> map = new LinkedHashMap<>();
		for (InventoryID which : new InventoryID[]{InventoryID.BANK, InventoryID.EQUIPMENT, InventoryID.INVENTORY})
		{
			ItemContainer c = client.getItemContainer(which);
			if (c == null) continue;
			for (Item i : c.getItems())
				if (i.getId() > 0) map.merge(i.getId(), Math.max(1, i.getQuantity()), Integer::sum);
		}
		return map;
	}

	// =====================================================================
	//  REFRESH — detect scythe vs no-scythe and update panel + highlights
	// =====================================================================
	private void refreshFromOwned()
	{
		Set<Integer> owned = ownedIds();

		// Only update scythe detection when we actually have items to look at.
		// This prevents the equipment-stats screen (which empties the worn
		// container) from flipping the whole panel to No-Scythe + all red.
		if (!owned.isEmpty())
		{
			boolean scythe = Presets.hasScythe(owned);
			lastScytheSetup = scythe;
			panel.setScytheSetup(scythe);
		}

		panel.setOwnedIds(readyIds(), bankOnlyIds());
		panel.refresh();
		updateHighlights();
	}

	private void updateHighlights()
	{
		if (!panel.isFilterEnabled())
		{
			bankOverlay.setRequiredIds(java.util.Collections.emptySet());
			bankOverlay.setPresentIds(java.util.Collections.emptySet());
			return;
		}
		bankOverlay.setRequiredIds(Presets.ALL_RELEVANT_IDS);
		bankOverlay.setPresentIds(ownedIds());
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == InventoryID.BANK.getId() || id == InventoryID.EQUIPMENT.getId() || id == InventoryID.INVENTORY.getId())
			refreshFromOwned();
	}

	// =====================================================================
	//  MOUSE
	// =====================================================================
	@Override
	public MouseEvent mouseClicked(MouseEvent e)
	{
		if (overlay.isVisible() && config.overlayDismiss() == OverlayDismiss.CLICK) overlay.dismiss();
		return e;
	}
	@Override public MouseEvent mousePressed(MouseEvent e)  { return e; }
	@Override public MouseEvent mouseReleased(MouseEvent e) { return e; }
	@Override public MouseEvent mouseEntered(MouseEvent e)  { return e; }
	@Override public MouseEvent mouseExited(MouseEvent e)   { return e; }
	@Override public MouseEvent mouseMoved(MouseEvent e)    { return e; }
	@Override public MouseEvent mouseDragged(MouseEvent e)  { return e; }

	// =====================================================================
	//  COMMANDS
	// =====================================================================
	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		switch (event.getCommand().toLowerCase())
		{
			case "tobcheck": runCheck(); break;
			case "tobdump":  runDump();  break;
		}
	}

	// =====================================================================
	//  ::tobcheck — check WORN + INVENTORY against requirements
	// =====================================================================
	private void runCheck()
	{
		if (!config.enableGearCheck()) { message("Gear check is turned off."); return; }
		if (config.role() != Role.MELEE) { message("No preset for " + config.role() + "."); return; }

		// What's actually equipped/in inventory right now (NOT bank — must be on you)
		Map<Integer, Integer> wornCounts = containerCounts(InventoryID.EQUIPMENT);
		Map<Integer, Integer> invCounts  = containerCounts(InventoryID.INVENTORY);
		Set<Integer> worn = wornCounts.keySet();

		boolean scythe = lastScytheSetup;
		List<SlotReq> reqs = Presets.requirements(scythe);

		List<String> problems = new ArrayList<>();
		for (SlotReq req : reqs)
		{
			Map<Integer, Integer> pool = req.equipped ? wornCounts : invCounts;
			int have = 0;
			for (int id : req.validIds) have += pool.getOrDefault(id, 0);
			if (have < req.quantity)
			{
				String where = req.equipped ? "Equip" : "Bring";
				if (req.quantity > 1) problems.add(where + " " + req.quantity + "x " + req.label + " (have " + have + ")");
				else problems.add(where + " " + req.label);
			}
		}

		if (Presets.ARCEUUS != readSpellbook())
			problems.add("Switch to Arceuus spellbook");

		String title = problems.isEmpty()
				? "Gear check PASSED \u2705"
				: (scythe ? "Scythe" : "No-Scythe") + " check \u2014 " + problems.size() + " issue(s)";

		overlay.showResult(title, problems);

		if (problems.size() >= 4) for (String p : problems) message("  - " + p);

		List<String> clip = new ArrayList<>();
		clip.add(title);
		for (String p : problems) clip.add("  - " + p);
		copyToClipboard(clip);
	}

	private Map<Integer, Integer> containerCounts(InventoryID which)
	{
		Map<Integer, Integer> map = new LinkedHashMap<>();
		ItemContainer c = client.getItemContainer(which);
		if (c == null) return map;
		for (Item i : c.getItems()) if (i.getId() > 0) map.merge(i.getId(), Math.max(1, i.getQuantity()), Integer::sum);
		return map;
	}

	// =====================================================================
	//  ::tobdump
	// =====================================================================
	private void runDump()
	{
		List<String> lines = new ArrayList<>();
		lines.add("=== TOBDUMP ===");
		ItemContainer eq = client.getItemContainer(InventoryID.EQUIPMENT);
		if (eq != null)
		{
			StringBuilder sb = new StringBuilder("EQUIP: ");
			for (Item item : eq.getItems()) if (item.getId() > 0) sb.append(item.getId()).append("=").append(itemName(item.getId())).append("  ");
			lines.add(sb.toString());
		}
		Map<Integer, Integer> inv = containerCounts(InventoryID.INVENTORY);
		if (!inv.isEmpty())
		{
			StringBuilder sb = new StringBuilder("INV: ");
			for (Map.Entry<Integer, Integer> e : inv.entrySet()) sb.append(e.getKey()).append("=").append(itemName(e.getKey())).append("x").append(e.getValue()).append("  ");
			lines.add(sb.toString());
		}
		lines.add("SPELLBOOK: " + readSpellbook() + " = " + spellbookName(readSpellbook()));
		lines.add("=== END DUMP ===");
		for (String line : lines) message(line);
		copyToClipboard(lines);
		message("Copied to clipboard \u2705");
	}

	// =====================================================================
	//  HELPERS
	// =====================================================================
	int readSpellbook() { return client.getVarbitValue(Varbits.SPELLBOOK); }

	private String spellbookName(int id) { switch (id) { case 0: return "Standard"; case 1: return "Ancient"; case 2: return "Lunar"; case 3: return "Arceuus"; default: return "?"; } }

	String itemName(int id)
	{
		try { String n = itemManager.getItemComposition(id).getName(); return (n == null || n.isEmpty()) ? "item#" + id : n; }
		catch (Exception e) { return "item#" + id; }
	}

	private void copyToClipboard(List<String> lines)
	{
		try { StringSelection sel = new StringSelection(String.join("\n", lines)); Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel); }
		catch (Exception ignored) {}
	}

	private void message(String text)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[Learner ToB] " + text, null);
	}
}