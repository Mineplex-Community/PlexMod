package cc.dyspore.plex.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cc.dyspore.plex.ui.PlexUIModMenu;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import cc.dyspore.plex.Plex;
import cc.dyspore.plex.ui.PlexUIBase;

/**
 * The module core class for PlexMod. Use the static methods in this class to
 * register the various aspects of your add-on mod.
 * 
 * @since 1.0
 */
public class PlexCore {
	private static Map<Class<? extends PlexModBase>, PlexModBase> modules = new ConcurrentHashMap<>();
	private static List<PlexUITab> uiTabList = new ArrayList<>();

	private static PlexCoreEventLoop eventLoopMods = PlexCoreEventLoop.create("modLoop").setClock(50);
	private static PlexCoreEventLoop eventLoopInternal = PlexCoreEventLoop.create("internalLoop").setClock(25);

	private PlexCore() {
	}

	/**
	 * Registers the mod task loop
	 *
	 * @return The task loop, {@link PlexCoreEventLoop}
	 */
	public static PlexCoreEventLoop getModLoop() {
		return eventLoopMods;
	}

	/**
	 * Registers the internal task loop
	 *
	 * @return The task loop, {@link PlexCoreEventLoop}
	 */
	public static PlexCoreEventLoop getInternalLoop() {
		return eventLoopInternal;
	}

	/**
	 * Registers a mod
	 * 
	 * @param mod The mod to register
	 */
	public static void register(final PlexModBase mod) {
		modules.put(mod.getClass(), mod);
		mod.modInit();
		long loopDelay = mod.getLoopDelay();
		if (loopDelay > 0) {
			getModLoop().addTask(mod::doModLoop, loopDelay);
		}
		else {
			getModLoop().addTask(mod::doModLoop);
		}
		PlexCore.saveConfiguration();
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
		if (modules.containsKey(modClass)) {
			try {
				return modClass.cast(modules.get(modClass));
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
	 * @param name  Title of the UI tab
	 * @param clazz Class of the UI tab
	 */
	public static PlexUITab registerMenuTab(String name, Class<? extends PlexUIBase> clazz) {
		PlexUITab tab = new PlexUITab(clazz, name);
		uiTabList.add(tab);
		return tab;
	}

	/**
	 * Returns the UI tab list
	 * 
	 * @return the UI tab list
	 * @see PlexUIBase
	 */
	public static List<PlexUITab> getMenuTabs() {
		return uiTabList;
	}

	/**
	 * Save all mod config files
	 */
	public static void saveConfiguration() {
		for (PlexModBase mod : modules.values()) {
			mod.saveConfig();
		}
		Plex.config.save();
	}

	/**
	 * Call the joinedMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void dispatchJoin() {
		for (final PlexModBase mod : modules.values()) {
			mod.onJoin();
		}
	}

	/**
	 * Call the leftMineplex() method in all registered mods
	 * 
	 * @see PlexModBase
	 */
	public static void dispatchLeave() {
		for (final PlexModBase mod : modules.values()) {
			mod.onLeave();
		}
	}

	/**
	 * Dispatches to all mods that the lobby has been updated
	 *
	 * @param lobbyType       Type of the lobby or LobbyType.E_LOBBY_SWITCH for indiciating exactly when a change occurs.
	 */
	public static void dispatchLobbyChanged(PlexMP.LobbyType lobbyType) {
		for (PlexModBase mod : modules.values()) {
			mod.onLobbyUpdate(lobbyType);
		}
	}

	/**
	 * Displays a UI screen
	 * 
	 * @param screen Screen to display
	 */
	public static void displayMenu(PlexUIBase screen) {
		if (screen != null) {
			Plex.listeners.setTargetGuiScreen(new PlexUIModMenu(screen));
		}
		else {
			Plex.listeners.setTargetGuiScreen(null);
		}
	}

	/**
	 * Gets the current player's username
	 * 
	 * @return The player's IGN, or null if not in-game
	 */
	public static String getPlayerName() {
		return Plex.minecraft.getSession().getUsername();
	}

	/**
	 * Gets a list of players' usernames in the world. Equivalent to
	 * getPlayerIGNList(false).
	 * 
	 * @return List of players' usernames in the current world
	 */
	public static List<String> getLoadedPlayerNames() {
		return getLoadedPlayerNames(false);
	}

	/**
	 * Gets the list of players in the world, optionally lowercasing all names
	 * 
	 * @param lowercase Make all player names lowercase
	 * @return The list of names, or null if not in a world
	 */
	public static List<String> getLoadedPlayerNames(boolean lowercase) {
		try {
			List<String> result = new ArrayList<>();
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
	public static List<String> getTablistPlayerNames() {
		return getTablistPlayerNames(false);
	}

	/**
	 * Gets the list of players in the tablist, optionally lowercasing all names
	 *
	 * @param lowercase Make all player names lowercase
	 * @return The list of names, or null if not in a world
	 */
	public static List<String> getTablistPlayerNames(boolean lowercase) {
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


	public static class PlexUITab {
		private Class<? extends PlexUIBase> guiClass;
		private String label;
		private int id;

		public PlexUITab(Class<? extends PlexUIBase> uiClass, String label) {
			this.guiClass = uiClass;
			this.label = label;
		}

		public Class<? extends PlexUIBase> getGuiClass() {
			return this.guiClass;
		}

		public String getLabel() {
			return this.label;
		}

		public int getID() {
			return this.id;
		}

		public PlexUITab setID(int id) {
			this.id = id;
			return this;
		}

		public PlexUITab getShallowCopy() {
			return new PlexUITab(this.guiClass, this.label);
		}
	}
}