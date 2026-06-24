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

	@ConfigSection(name = "Popup",   description = "Gear check popup appearance",  position = 1)
	String popupSection = "popup";

	@ConfigSection(name = "Raid checklist", description = "Pre-raid entry checks", position = 2)
	String checklistSection = "checklist";

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

	// Hidden — role is now set via the sidebar panel dropdown
	@ConfigItem(keyName = "role", name = "", description = "", hidden = true)
	default Role role() { return Role.MELEE; }
}