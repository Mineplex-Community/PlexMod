package cc.dyspore.plex.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cc.dyspore.plex.core.mineplex.PlexLobbyType;
import cc.dyspore.plex.ui.PlexUIModMenuScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.core.loop.PlexCoreEventLoop;
import cc.dyspore.plex.ui.PlexUIBase;
import cc.dyspore.plex.ui.PlexUITabContainer;

/**
 * The core utility class for PlexMod. Use the static methods in this class to
 * register the various aspects of your add-on mod.
 * 
 * @since 1.0
 */
public class PlexCore {
	public static Map<Class<? extends PlexModBase>, PlexModBase> plexMods = new ConcurrentHashMap<>();
	public static Map<PlexModBase, Runnable> modLoops = new HashMap<>();
	public static Map<PlexModBase, Thread> modThreads = new HashMap<>();
	public static List<PlexUITabContainer> uiTabList = new ArrayList<>();

	private PlexCore() {
	}

	/**
	 * Registers the mod task loop
	 *
	 * @return The task loop, {@link PlexCoreEventLoop}
	 */
	public static PlexCoreEventLoop getModLoop() {
		return Plex.eventLoop.get("modLoop");
	}

	/**
	 * Registers the internal task loop
	 *
	 * @return The task loop, {@link PlexCoreEventLoop}
	 */
	public static PlexCoreEventLoop getInternalLoop() {
		return Plex.eventLoop.get("internalTask");
	}

	/**
	 * Registers a mod
	 * 
	 * @param mod The mod to register
	 */
	public static void registerMod(final PlexModBase mod) {
		plexMods.put(mod.getClass(), mod);
		mod.modInit();
		getModLoop().addTask(() -> {
			mod.modLoop(Plex.gameState.isMineplex);
			if (Plex.gameState.isMineplex) {
				mod.onlineModLoop();
			}
		}, mod.getLoopDelay());
		PlexCore.saveAllConfig();
	}

	/**
	 * Returns a mod
	 * 
	 * @param modClass The class of the mod which extends {@link PlexModBase}
	 * @return The mod, a new registered instance if one is not already loaded
	 */
	public static <T extends PlexModBase> T modInstance(Class<T> modClass) {
		return Objects.requireNonNull(modInstanceOrNull(modClass));
	}

	/**
	 * Returns a mod
	 *
	 * @param modClass The class of the mod which extends {@link PlexModBase}
	 * @return The instance of the mod, or null if not loaded
	 */
	public static <T extends PlexModBase> T modInstanceOrNull(Class<T> modClass) {
		if (plexMods.containsKey(modClass)) {
			try {
				return modClass.cast(plexMods.get(modClass));
			}
			catch (Throwable e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Registers a UI tab under the given title
	 * 
	 * @param name   Title of the UI tab
	 * @param class1 Class of the UI tab
	 */
	public static PlexUITabContainer registerUiTab(String name, Class<? extends PlexUIBase> class1) {
		PlexUITabContainer tab = new PlexUITabContainer(class1, name);
		uiTabList.add(tab);
		return tab;
	}

	/**
	 * Gets the UI tab registered under the given title
	 * 
	 * @param label Title of the tab
	 * @return The UI tab
	 */
	public static PlexUITabContainer getUiTab(String label) {
		for (PlexUITabContainer tab : uiTabList) {
			if (tab.getUiClass().equals(label)) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * Gets the title of the UI tab registered at the given index
	 * 
	 * @param pos Index of the tab
	 * @return Title of the tab if it exists, null otherwise
	 * @see PlexUIBase
	 */
	public static PlexUITabContainer getUiTabAt(Integer pos) {
		if (pos >= uiTabList.size()) {
			return null;
		}

		PlexUITabContainer tab = uiTabList.get(pos);
		if (tab != null) {
			return tab;
		} 
		else {
			return null;
		}
	}

	/**
	 * Returns the UI tab list
	 * 
	 * @return the UI tab list
	 * @see PlexUIBase
	 */
	public static List<PlexUITabContainer> getUiTabList() {
		return uiTabList;
	}

	/**
	 * Save all mod config files
	 */
	public static void saveAllConfig() {
		for (PlexModBase mod : plexMods.values()) {
			mod.saveModConfig();
		}
		Plex.config.save();
	}

	/**
	 * Call the joinedMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void joinedMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
			mod.joinedMineplex();
		}
	}

	/**
	 * Call the leftMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void leftMineplex() {
		for (final PlexModBase mod : plexMods.values()) {
			mod.leftMineplex();
		}
	}

	/**
	 * Displays a UI screen
	 * 
	 * @param screen Screen to display
	 */
	public static void displayUIScreen(PlexUIBase screen) {
		if (screen != null) {
			Plex.listeners.setTargetGuiScreen(new PlexUIModMenuScreen(screen));
		}
		Plex.listeners.setTargetGuiScreen(null);
	}

	/**
	 * Gets the current player's username
	 * 
	 * @return The player's IGN, or null if not in-game
	 */
	public static String getPlayerIGN() {
		try {
			return Plex.minecraft.thePlayer.getDisplayNameString();
		} 
		catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Gets a list of players' usernames in the world. Equivalent to
	 * getPlayerIGNList(false).
	 * 
	 * @return List of players' usernames in the current world
	 */
	public static List<String> getPlayerIGNList() {
		return getPlayerIGNList(false);
	}

	/**
	 * Gets the list of players in the world, optionally lowercasing all names
	 * 
	 * @param lowercase Make all player names lowercase
	 * @return The list of names, or null if not in a world
	 */
	public static List<String> getPlayerIGNList(Boolean lowercase) {
		try {
			List<String> result = new ArrayList<String>();
			for (EntityPlayer player : Plex.minecraft.theWorld.playerEntities) {
				result.add(lowercase ? player.getName().toLowerCase() : player.getName());
			}
			return result;
		} 
		catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Gets a list of players' usernames in the tablist. Equivalent to
	 * getPlayerIGNTabList(false).
	 *
	 * @return List of players' usernames in the current world
	 */
	public static List<String> getPlayerIGNTabList() {
		return getPlayerIGNTabList(false);
	}

	/**
	 * Gets the list of players in the tablist, optionally lowercasing all names
	 *
	 * @param lowercase Make all player names lowercase
	 * @return The list of names, or null if not in a world
	 */
	public static List<String> getPlayerIGNTabList(boolean lowercase) {
		List<String> playerNameList = new ArrayList<>();
		for (NetworkPlayerInfo player : Plex.minecraft.thePlayer.sendQueue.getPlayerInfoMap()) {
			String name = player.getGameProfile().getName();
			if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
				continue;
			}
			playerNameList.add(lowercase ? name.toLowerCase() : name);
		}
		return playerNameList;
	}


	/**
	 * Dispatches to all mods that the lobby has been updated
	 * 
	 * @param lobbyType       Type of the lobby or PlexLobbyType.E_LOBBY_SWITCH for indiciating exactly when a change occurs.
	 */
	public static void dispatchLobbyChanged(PlexLobbyType lobbyType) {
		for (PlexModBase mod : plexMods.values()) {
			mod.lobbyUpdated(lobbyType);
		}		
	}
}