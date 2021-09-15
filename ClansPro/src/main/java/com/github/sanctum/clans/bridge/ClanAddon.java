package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileExtension;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.Nullable;

public abstract class ClanAddon {

	private boolean ACTIVE;
	private int importance;
	private final ClanAddonLogger LOGGER;
	private final Collection<String> DEPEND;
	private final Collection<String> LOADBEFORE;
	private final Collection<Listener> LISTENERS;
	private final Collection<ClanSubCommand> COMMANDS;

	public ClanAddon() {
		this.LISTENERS = new HashSet<>();
		this.COMMANDS = new HashSet<>();
		this.DEPEND = new HashSet<>();
		this.LOADBEFORE = new HashSet<>();
		this.LOGGER = new ClanAddonLogger(getName());
		this.ACTIVE = true;
		this.importance = ClanAddonQuery.COUNT + 1;
	}

	protected final void loadDepends() {
		if (DEPEND.isEmpty()) {
			InputStream resource = getResource(getName().toLowerCase() + ".yml");
			if (resource != null) {
				FileManager temp = getFile(getName());
				FileList.copy(resource, temp.getRoot().getParent());
				temp.getRoot().reload();
				DEPEND.addAll(temp.read(f -> f.getStringList("depend")));
				LOADBEFORE.addAll(temp.read(f -> f.getStringList("loadbefore")));
				temp.getRoot().delete();
			} else {
				getLogger().warn("Addon dependency list not found!");
			}
		}
	}

	protected final void setLevel(int importance) {
		this.importance = importance;
	}

	/**
	 * @return true if the addon is enabled.
	 */
	public final boolean isActive() {
		return this.ACTIVE;
	}

	/**
	 * @return true if the plugin should persist on enable.
	 */
	public abstract boolean persist();

	/**
	 * Get the unique id for this addon.
	 *
	 * @return The id for this addon.
	 */
	public HUID getId() {
		return HUID.randomID();
	}

	/**
	 * Get the name of this addon.
	 *
	 * @return The name of this addon.
	 */
	public abstract String getName();

	/**
	 * Get the description for this addon.
	 *
	 * @return The addon description.
	 */
	public abstract String getDescription();

	/**
	 * Get the version of the addon.
	 *
	 * @return The addon version.
	 */
	public abstract String getVersion();

	/**
	 * Get the addon authors.
	 *
	 * @return The addon authors.
	 */
	public abstract String[] getAuthors();

	public abstract void onLoad();

	public abstract void onEnable();

	public abstract void onDisable();

	/**
	 * Translate placeholders for this given addon.
	 *
	 * @param player The player to use.
	 * @param param  The string to parse.
	 * @return The placeholder converted string.
	 */
	public String onPlaceholder(Player player, String param) {
		if (param.equals(getName())) return getName() + " " + getVersion();
		return "";
	}

	/**
	 * Get all addon dependencies for this addon.
	 *
	 * @return An array of addon names.
	 */
	public String[] getDependencies() {
		return DEPEND.toArray(new String[0]);
	}

	/**
	 * Get all addons to be loaded before this addon.
	 *
	 * @return An array of addon names.
	 */
	public String[] getLoadBefore() {
		return LOADBEFORE.toArray(new String[0]);
	}

	/**
	 * Get all registered listeners to this addon.
	 *
	 * @return A collection of listeners.
	 */
	public Collection<Listener> getAdditions() {
		return this.LISTENERS;
	}

	/**
	 * Get all clans sub commands.
	 *
	 * @return A collection of registered sub commands.
	 */
	public Collection<ClanSubCommand> getCommands() {
		return this.COMMANDS;
	}

	public final Plugin getPlugin() {
		return getApi().getPlugin();
	}

	public final int getLevel() {
		return importance;
	}

	/**
	 * Locate and modify an existing file or create a new one.
	 *
	 * @param name      The name of the file.
	 * @param directory The directory the file lies within.
	 * @return A cached file manager.
	 */
	public final FileManager getFile(String name, String... directory) {
		String dir = null;
		StringBuilder builder = new StringBuilder();
		if (directory.length > 0) {
			for (String d : directory) {
				builder.append(d).append("/");
			}
		}
		if (builder.length() > 0) {
			dir = builder.toString().trim().substring(0, builder.length() - 1);
		}

		if (dir == null) return getApi().getFileList().get(name, "Addons/" + getName() + "/");
		return getApi().getFileList().get(name, "Addons/" + getName() + "/" + dir + "/");
	}

	/**
	 * Locate and modify an existing file or create a new one.
	 *
	 * @param name      The name of the file.
	 * @param extension The file extension to use. Ex. [{@linkplain com.github.sanctum.labyrinth.data.FileType#JSON}, {@linkplain com.github.sanctum.labyrinth.data.FileType#YAML}]
	 * @param directory The directory the file lies within.
	 * @return A cached file manager.
	 */
	public final FileManager getFile(FileExtension extension, String name, String... directory) {
		String dir = null;
		StringBuilder builder = new StringBuilder();
		if (directory.length > 0) {
			for (String d : directory) {
				builder.append(d).append("/");
			}
		}
		if (builder.length() > 0) {
			dir = builder.toString().trim().substring(0, builder.length() - 1);
		}
		if (dir == null) return getApi().getFileList().get(name, "Addons/" + getName() + "/", extension);
		return getApi().getFileList().get(name, "Addons/" + getName() + "/" + dir + "/", extension);
	}

	/**
	 * Get a resource file from this addons specific resource folder.
	 *
	 * @param resource The name of the resource to get.
	 * @return The resource or null if not found.
	 */
	public @Nullable InputStream getResource(String resource) {
		return getClass().getClassLoader().getResourceAsStream(resource);
	}

	/**
	 * @return The default messenger
	 */
	public final Message getMessenger() {
		return Message.loggedFor(getPlugin());
	}

	/**
	 * @return The clans api instance.
	 */
	protected final ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

	/**
	 * Get and manage services using clan addon objects instead of plugin as the key.
	 *
	 * @return A keyed service manager using clan addons for keys.
	 */
	public final KeyedServiceManager<ClanAddon> getServiceManager() {
		return ClansAPI.getInstance().getServiceManager();
	}

	/**
	 * Get the console logger for this addon.
	 *
	 * @return A console logger for this addon.
	 */
	public final ClanAddonLogger getLogger() {
		return this.LOGGER;
	}

	/**
	 * Register a listener within this addons {@linkplain ClanAddon#onLoad()} ()}
	 *
	 * @param listener The listener to register.
	 */
	protected final void register(Listener listener) {
		if (!this.LISTENERS.contains(listener)) {
			this.LISTENERS.add(listener);
		}
	}

	/**
	 * Register a command within this addons {@linkplain ClanAddon#onLoad()} ()}
	 *
	 * @param command The command to register.
	 */
	protected final void register(ClanSubCommand command) {
		if (!this.COMMANDS.contains(command)) {
			this.COMMANDS.add(command);
		}
	}

	protected final void setActive(boolean active) {
		this.ACTIVE = active;
	}

	protected final void register() {
		ClanAddonQuery.COUNT += 1;
		ClanAddonQuery.getRegisteredAddons().add(this);
	}

	public final void remove() {
		ClanAddonQuery.COUNT -= 1;
		ClansAPI.getInstance().getPlugin().getLogger().info("- Disabling addon " + '"' + getName() + '"' + " v" + getVersion());
		for (RegisteredListener l : HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin())) {
			if (getAdditions().contains(l.getListener())) {
				HandlerList.unregisterAll(l.getListener());
			}
		}
		onDisable();
		Schedule.sync(() -> ClanAddonQuery.getRegisteredAddons().removeIf(c -> c.getName().equals(getName()))).wait(1);
	}

}
