package cc.dyspore.plex.mods.autothank;

import net.minecraft.client.gui.GuiButton;
import cc.dyspore.plex.core.PlexCore;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUIModMenu;
import cc.dyspore.plex.ui.widget.PlexUISlider;

public class PlexAutoThankUI extends PlexUIBase {
	@Override
	public String getTitle() {
		return "AutoThank";
	}

	@Override
	public void initScreen(PlexUIModMenu ui) {
		int top = ui.startingYPos(41);
		int paneSize = ui.centeredPaneSize(1, 20, 160);
		int pane1Pos = ui.centeredPanePos(0, 1, 20, 160);
		ui.addElement(new GuiButton(5, pane1Pos + 5, top + 0, paneSize - 10, 20, buttonDisplayString("AutoThank", PlexCore.modInstance(PlexAutoThankMod.class).modEnabled)));
		ui.addElement(new GuiButton(6, pane1Pos + 5, top + 23, paneSize - 10, 20, buttonDisplayString("Compact Messages", PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled)));

	}
	
	public String buttonDisplayString(String prefix, Boolean enabled) {
		return prefix + ": " + (enabled ? "Enabled" : "Disabled");
	}
	
	@Override
	public void onSliderInteract(PlexUISlider slider) {
	}
	
	@Override
	public int pageForegroundColour() {
		return 0xff00ffc3;
	}

	@Override
	public void onButtonInteract(GuiButton button) {
		if (button.id == 5) {
			PlexCore.modInstance(PlexAutoThankMod.class).modEnabled = !PlexCore.modInstance(PlexAutoThankMod.class).modEnabled;
			button.displayString = buttonDisplayString("AutoThank", PlexCore.modInstance(PlexAutoThankMod.class).modEnabled);
		}
		if (button.id == 6) {
			PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled = !PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled;
			button.displayString = buttonDisplayString("Compact Messages", PlexCore.modInstance(PlexAutoThankMod.class).compactMessagesEnabled);
		}
	}
}
