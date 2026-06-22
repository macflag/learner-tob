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

	// --- General ---
	@ConfigItem(keyName = "enableGearCheck", name = "Enable gear check",
			description = "Turn the gear check on or off.",
			position = 0, section = generalSection)
	default boolean enableGearCheck() { return true; }

	// --- Popup ---
	@ConfigItem(keyName = "overlayDismiss", name = "Dismiss mode",
			description = "Click to close, or auto-dismiss after a set time.",
			position = 0, section = popupSection)
	default OverlayDismiss overlayDismiss() { return OverlayDismiss.CLICK; }

	@ConfigItem(keyName = "overlayFontSize", name = "Font size",
			description = "Small, Medium, or Large text in the popup.",
			position = 1, section = popupSection)
	default OverlayFontSize overlayFontSize() { return OverlayFontSize.MEDIUM; }

	@ConfigItem(keyName = "overlayPosition", name = "Position",
			description = "Where the popup appears on screen.",
			position = 2, section = popupSection)
	default OverlayPosition overlayPosition() { return OverlayPosition.TOP_CENTER; }

	@ConfigItem(keyName = "overlayOpacity", name = "Opacity",
			description = "Background opacity of the popup (10-100).",
			position = 3, section = popupSection)
	@Range(min = 10, max = 100)
	default int overlayOpacity() { return 85; }

	@ConfigItem(keyName = "passColor", name = "Pass colour",
			description = "Colour of the popup header when check passes.",
			position = 4, section = popupSection)
	default Color passColor() { return new Color(0, 160, 0); }

	@ConfigItem(keyName = "failColor", name = "Fail colour",
			description = "Colour of the popup header when issues are found.",
			position = 5, section = popupSection)
	default Color failColor() { return new Color(180, 120, 0); }

	@ConfigItem(keyName = "lotsColor", name = "LOTS colour",
			description = "Colour of the popup header when 4+ issues are found.",
			position = 6, section = popupSection)
	default Color lotsColor() { return new Color(180, 50, 0); }

	// Hidden — role is now set via the sidebar panel dropdown
	@ConfigItem(keyName = "role", name = "", description = "", hidden = true)
	default Role role() { return Role.MELEE; }
}