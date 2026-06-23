/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
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
	private static final int LOTS_THRESHOLD = 4;

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
	//  Owned item helpers
	// ------------------------------------------------------------------
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
		if (c == null)
		{
			return;
		}
		for (Item i : c.getItems())
		{
			if (i.getId() > 0)
			{
				ids.add(i.getId());
			}
		}
	}

	private Map<Integer, Integer> containerCounts(InventoryID which)
	{
		Map<Integer, Integer> map = new LinkedHashMap<>();
		ItemContainer c = client.getItemContainer(which);
		if (c == null)
		{
			return map;
		}
		for (Item i : c.getItems())
		{
			if (i.getId() > 0)
			{
				map.merge(i.getId(), Math.max(1, i.getQuantity()), Integer::sum);
			}
		}
		return map;
	}

	// ------------------------------------------------------------------
	//  Refresh — detect scythe vs no-scythe, update panel and highlights
	// ------------------------------------------------------------------
	private void refreshFromOwned()
	{
		Set<Integer> owned = ownedIds();

		// Only update the setup when there are items to read. This stops the
		// equipment-stats screen (which momentarily empties the worn container)
		// from flipping the panel to No-Scythe with everything marked missing.
		if (!owned.isEmpty())
		{
			lastScytheSetup = Presets.hasScythe(owned);
			panel.setScytheSetup(lastScytheSetup);
		}

		panel.setOwnedIds(readyIds(), bankOnlyIds());

		Map<Integer, String> names = new HashMap<>();
		for (int id : owned)
		{
			if (Presets.ALL_RELEVANT_IDS.contains(id))
			{
				names.put(id, itemName(id));
			}
		}
		panel.setOwnedNames(names);

		panel.refresh();
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

	// ------------------------------------------------------------------
	//  Mouse — a click anywhere dismisses the popup in click mode
	// ------------------------------------------------------------------
	@Override
	public MouseEvent mouseClicked(MouseEvent e)
	{
		if (overlay.isVisible() && config.overlayDismiss() == OverlayDismiss.CLICK)
		{
			overlay.dismiss();
		}
		return e;
	}

	@Override
	public MouseEvent mousePressed(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseEntered(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseExited(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent e)
	{
		return e;
	}

	// ------------------------------------------------------------------
	//  Commands
	// ------------------------------------------------------------------
	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		switch (event.getCommand().toLowerCase())
		{
			case "tobcheck":
				runCheck();
				break;
			case "tobdump":
				runDump();
				break;
			default:
				break;
		}
	}

	private void runCheck()
	{
		if (!config.enableGearCheck())
		{
			message("Gear check is turned off.");
			return;
		}
		if (config.role() != Role.MELEE)
		{
			message("No preset for " + config.role() + " yet.");
			return;
		}

		Map<Integer, Integer> wornCounts = containerCounts(InventoryID.EQUIPMENT);
		Map<Integer, Integer> invCounts = containerCounts(InventoryID.INVENTORY);

		boolean scythe = lastScytheSetup;
		List<SlotReq> reqs = Presets.requirements(scythe);

		List<String> problems = new ArrayList<>();
		for (SlotReq req : reqs)
		{
			Map<Integer, Integer> pool = req.equipped ? wornCounts : invCounts;
			int have = 0;
			for (int id : req.validIds)
			{
				have += pool.getOrDefault(id, 0);
			}
			if (have < req.quantity)
			{
				String where = req.equipped ? "Equip" : "Bring";
				if (req.quantity > 1)
				{
					problems.add(where + " " + req.quantity + "x " + req.label + " (have " + have + ")");
				}
				else
				{
					problems.add(where + " " + req.label);
				}
			}
		}

		if (Presets.ARCEUUS != readSpellbook())
		{
			problems.add("Switch to Arceuus spellbook");
		}

		String title = problems.isEmpty()
				? "Gear check PASSED \u2705"
				: (scythe ? "Scythe" : "No-Scythe") + " check \u2014 " + problems.size() + " issue(s)";

		overlay.showResult(title, problems);

		if (problems.size() >= LOTS_THRESHOLD)
		{
			for (String p : problems)
			{
				message("  - " + p);
			}
		}

		List<String> clip = new ArrayList<>();
		clip.add(title);
		for (String p : problems)
		{
			clip.add("  - " + p);
		}
		copyToClipboard(clip);
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
			{
				if (item.getId() > 0)
				{
					sb.append(item.getId()).append("=").append(itemName(item.getId())).append("  ");
				}
			}
			lines.add(sb.toString());
		}

		Map<Integer, Integer> inv = containerCounts(InventoryID.INVENTORY);
		if (!inv.isEmpty())
		{
			StringBuilder sb = new StringBuilder("INV: ");
			for (Map.Entry<Integer, Integer> e : inv.entrySet())
			{
				sb.append(e.getKey()).append("=").append(itemName(e.getKey()))
						.append("x").append(e.getValue()).append("  ");
			}
			lines.add(sb.toString());
		}

		lines.add("SPELLBOOK: " + readSpellbook() + " = " + spellbookName(readSpellbook()));
		lines.add("=== END DUMP ===");

		for (String line : lines)
		{
			message(line);
		}
		copyToClipboard(lines);
		message("Copied to clipboard \u2705");
	}

	// ------------------------------------------------------------------
	//  Helpers
	// ------------------------------------------------------------------
	private int readSpellbook()
	{
		return client.getVarbitValue(Varbits.SPELLBOOK);
	}

	private String spellbookName(int id)
	{
		switch (id)
		{
			case 0:
				return "Standard";
			case 1:
				return "Ancient";
			case 2:
				return "Lunar";
			case 3:
				return "Arceuus";
			default:
				return "Unknown(" + id + ")";
		}
	}

	private String itemName(int id)
	{
		try
		{
			String n = itemManager.getItemComposition(id).getName();
			return (n == null || n.isEmpty()) ? "item#" + id : n;
		}
		catch (RuntimeException e)
		{
			return "item#" + id;
		}
	}

	private void copyToClipboard(List<String> lines)
	{
		try
		{
			StringSelection sel = new StringSelection(String.join("\n", lines));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		}
		catch (RuntimeException ignored)
		{
			// Clipboard may be unavailable on some systems; ignore.
		}
	}

	private java.awt.image.BufferedImage createIcon()
	{
		java.awt.image.BufferedImage icon =
				new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = icon.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
				java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
		// Purple glow background
		g.setColor(new java.awt.Color(0x9B30C0));
		g.fillRoundRect(1, 3, 14, 11, 4, 4);
		// Gold chest body
		g.setColor(new java.awt.Color(0xF7CD45));
		g.fillRect(3, 6, 10, 7);
		// Dark wood band
		g.setColor(new java.awt.Color(0x5A3A22));
		g.fillRect(3, 8, 10, 2);
		// Lock
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