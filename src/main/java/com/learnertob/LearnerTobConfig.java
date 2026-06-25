/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import java.awt.Color;

@ConfigGroup(LearnerTobConfig.GROUP)
public interface LearnerTobConfig extends Config
{
	String GROUP = "learnertob";

	@ConfigSection(name = "General", description = "Gear check toggle",            position = 0)
	String generalSection = "general";

	@ConfigSection(name = "Raid checklist", description = "Pre-raid entry checks", position = 1)
	String checklistSection = "checklist";

	@ConfigSection(name = "Maiden", description = "Maiden room prompts and markers", position = 2)
	String maidenSection = "maiden";

	@ConfigSection(name = "Popup",   description = "Gear check popup appearance",  position = 3)
	String popupSection = "popup";

	@ConfigSection(name = "Testing (dev)", closedByDefault = true,
			description = "Hide items from the plugin so you can test other roles/setups without banking gear",
			position = 4)
	String testingSection = "testing";

	// --- General ---
	@ConfigItem(keyName = "enableGearCheck", name = "Enable gear check",
			description = "Turn the gear check on or off.",
			position = 0, section = generalSection)
	default boolean enableGearCheck() { return true; }

	// --- Popup ---
	@ConfigItem(keyName = "overlayFontSize", name = "Font size",
			description = "Small, Medium, or Large text in the popup.",
			position = 0, section = popupSection)
	default OverlayFontSize overlayFontSize() { return OverlayFontSize.MEDIUM; }

	@ConfigItem(keyName = "overlayPosition", name = "Position",
			description = "Where the popup appears on screen.",
			position = 1, section = popupSection)
	default OverlayPosition overlayPosition() { return OverlayPosition.TOP_CENTER; }

	@ConfigItem(keyName = "overlayOpacity", name = "Opacity",
			description = "Background opacity of the popup (10-100).",
			position = 2, section = popupSection)
	@Range(min = 10, max = 100)
	default int overlayOpacity() { return 85; }

	@ConfigItem(keyName = "failColor", name = "Fail colour",
			description = "Colour of the popup header when issues are found.",
			position = 3, section = popupSection)
	default Color failColor() { return new Color(180, 120, 0); }

	@ConfigItem(keyName = "lotsColor", name = "LOTS colour",
			description = "Colour of the popup header when 4+ issues are found.",
			position = 4, section = popupSection)
	default Color lotsColor() { return new Color(180, 50, 0); }

	@ConfigItem(keyName = "flashingAlerts", name = "Flashing alerts",
			description = "Allow urgent popups to pulse to draw attention. Turn off for a steady popup.",
			position = 5, section = popupSection)
	default boolean flashingAlerts() { return true; }

	// --- Raid checklist ---
	@ConfigItem(keyName = "enableChecklist", name = "Enable raid checklist",
			description = "Master toggle for the pre-raid entry checks.",
			position = 0, section = checklistSection)
	default boolean enableChecklist() { return true; }

	@ConfigItem(keyName = "checkSpellbook", name = "Correct spellbook",
			description = "Warn if you're not on the spellbook this role expects.",
			position = 1, section = checklistSection)
	default boolean checkSpellbook() { return true; }

	@ConfigItem(keyName = "checkAutoRetaliate", name = "Auto-retaliate off",
			description = "Warn if auto-retaliate is still ON.",
			position = 2, section = checklistSection)
	default boolean checkAutoRetaliate() { return true; }

	@ConfigItem(keyName = "checkPrePot", name = "Pre-pot boost",
			description = "Warn if your main combat stat isn't boosted (no pre-pot).",
			position = 3, section = checklistSection)
	default boolean checkPrePot() { return true; }

	@ConfigItem(keyName = "checkHpOverheal", name = "HP overheal",
			description = "Warn if hitpoints are below maximum at entry.",
			position = 4, section = checklistSection)
	default boolean checkHpOverheal() { return true; }

	@ConfigItem(keyName = "raidEntryCheck", name = "Raid-entry door check",
			description = "Auto-run the entry checklist at the raid-start door (silent unless there's an issue).",
			position = 5, section = checklistSection)
	default boolean raidEntryCheck() { return true; }

	@ConfigItem(keyName = "maidenSetupCheck", name = "Maiden setup prompt",
			description = "On entering Maiden, prompt to drop Salve and equip your maul/hammer; clears once done.",
			position = 0, section = maidenSection)
	default boolean maidenSetupCheck() { return true; }

	@ConfigItem(keyName = "maidenHpPrompts", name = "Maiden HP prompts",
			description = "Role-specific call-outs as Maiden drops past 75 / 55 / 35% HP.",
			position = 1, section = maidenSection)
	default boolean maidenHpPrompts() { return true; }

	@ConfigItem(keyName = "maidenPrayerPrompt", name = "Maiden prayer prompt",
			description = "On entering Maiden, flash which prayers to flick (Magic + Piety/Rigour/Augury).",
			position = 2, section = maidenSection)
	default boolean maidenPrayerPrompt() { return true; }

	@ConfigItem(keyName = "maidenPrayerStay", name = "Prayer prompt: stay until correct",
			description = "On: the prayer prompt stays until you have the right prayers active. "
					+ "Off: a single flash on entry, skipped if you're already praying correctly.",
			position = 3, section = maidenSection)
	default boolean maidenPrayerStay() { return false; }

	@ConfigItem(keyName = "maidenTileMarkers", name = "Maiden tile markers",
			description = "Draw your role's 'stand here' box on the floor during Maiden.",
			position = 4, section = maidenSection)
	default boolean maidenTileMarkers() { return true; }

	// --- Testing (dev): pretend you don't own certain items, to test other setups ---
	@ConfigItem(keyName = "testHideScythe", name = "Hide Scythe",
			description = "Pretend you own no scythe (forces the No-Scythe / Oathplate-Whip setups).",
			position = 0, section = testingSection)
	default boolean testHideScythe() { return false; }

	@ConfigItem(keyName = "testHideOathplate", name = "Hide Oathplate",
			description = "Pretend you own no Oathplate (helm/chest/legs, any variant).",
			position = 1, section = testingSection)
	default boolean testHideOathplate() { return false; }

	@ConfigItem(keyName = "testHideTentacle", name = "Hide Tentacle",
			description = "Pretend you own no Abyssal tentacle (blocks the MDPS Oathplate-Whip setup).",
			position = 2, section = testingSection)
	default boolean testHideTentacle() { return false; }

	@ConfigItem(keyName = "testHideIds", name = "Hide item IDs",
			description = "Comma-separated item IDs to also hide from the plugin (e.g. 22325,11832).",
			position = 3, section = testingSection)
	default String testHideIds() { return ""; }

	// Hidden — role is now set via the sidebar panel dropdown
	@ConfigItem(keyName = "role", name = "", description = "", hidden = true)
	default Role role() { return Role.MELEE; }
}