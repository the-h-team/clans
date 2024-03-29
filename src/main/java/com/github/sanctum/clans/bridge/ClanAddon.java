package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.container.KeyedServiceManager;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.util.Check;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.HUID;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClanAddon {

	private static int PLACEMENT = 0;
	private final HUID id = HUID.randomID();
	private final ClanAddonLogger logger;
	private final ClanAddonContext context;
	private final ClanAddonLoader loader;
	private final ClassLoader classLoader;

	protected ClanAddon() {
		ClassLoader loader = this.getClass().getClassLoader();
		if (!(loader instanceof ClanAddonClassLoader) && !ClansAPI.class.getClassLoader().equals(loader))
			throw new InvalidAddonStateException("Addon not provided by " + ClanAddonClassLoader.class);
		this.classLoader = loader;
		this.logger = new ClanAddonLogger() {
			private final Logger LOG = Logger.getLogger("Minecraft");
			private final String addon;

			{
				this.addon = "Clan:" + getName();
			}

			public void log(Level level, String info) {
				this.LOG.log(level, "[" + addon + "]: " + info);
			}

			public void info(Supplier<String> info) {
				log(Level.INFO, info.get());
			}

			public void warn(Supplier<String> info) {
				log(Level.WARNING, info.get());
			}

			public void error(Supplier<String> info) {
				log(Level.SEVERE, info.get());
			}

			public void info(String info) {
				log(Level.INFO, info);
			}

			public void warn(String info) {
				log(Level.WARNING, info);
			}

			public void error(String info) {
				log(Level.SEVERE, info);
			}
		};
		this.loader = new ClanAddonLoader() {

			@Override
			public ClanAddon loadAddon(File jar) throws IOException, InvalidAddonException {
				if (!jar.isFile()) throw new InvalidAddonException("File " + jar.getName() + " not valid for addon processing.");
				return new ClanAddonClassLoader(jar, ClanAddon.this).getMainClass();
			}

			@Override
			public Deployable<Void> enableAddon(ClanAddon addon) {
				return Deployable.of(() -> {
					if (addon.classLoader.getParent().equals(getClassLoader())) {
						if (ClanAddonQueue.getInstance().get(addon.getName()) == null) {
							ClanAddonQueue.getInstance().register(addon);
							return;
						} else
							throw new ClanAddonRegistrationException("Addon " + addon + " is already registered and running!");
					}
					throw new InvalidAddonStateException("The provided addon doesn't belong to this loader's " + ClanAddonClassLoader.class);
				}, 0);
			}

			@Override
			public Deployable<Void> disableAddon(ClanAddon addon) {
				return Deployable.of(() -> {
					if (addon.classLoader.getParent().equals(getClassLoader())) {
						if (ClanAddonQueue.getInstance().get(addon.getName()) != null) {
							ClanAddonQueue.getInstance().remove(addon);
							return;
						} else throw new ClanAddonRegistrationException("Addon " + addon + " isn't registered!");
					}
					throw new InvalidAddonStateException("The provided addon doesn't belong to this loader's " + ClanAddonClassLoader.class);
				}, 0);
			}


		};
		this.context = new ClanAddonContext() {

			private final List<String> depend = new ArrayList<>();
			private final List<String> loadBefore = new ArrayList<>();
			private final List<String> loadAfter = new ArrayList<>();
			private final Collection<Listener> listeners = new HashSet<>();
			private final Collection<ClanSubCommand> commands = new HashSet<>();
			private boolean active = false;
			private int level;

			{
				this.level = PLACEMENT + 1;
				InputStream resource = getResource(getName() + ".yml");
				if (resource != null) {
					FileManager temp = getFile(getName());
					FileList.copy(resource, temp.getRoot().getParent());
					temp.getRoot().reload();
					depend.addAll(temp.read(f -> f.getStringList("depend")));
					loadBefore.addAll(temp.read(f -> f.getStringList("load-before")));
					loadAfter.addAll(temp.read(f -> f.getStringList("load-after")));
					temp.getRoot().delete();
				} else {
					getLogger().info("- No dependencies provided.");
				}
			}

			@Override
			public Listener[] getListeners() {
				return listeners.toArray(new Listener[0]);
			}

			@Override
			public ClanSubCommand[] getCommands() {
				return commands.toArray(new ClanSubCommand[0]);
			}

			@Override
			public String[] getDependencies() {
				return depend.toArray(new String[0]);
			}

			@Override
			public String[] getLoadBefore() {
				return loadBefore.toArray(new String[0]);
			}

			@Override
			public String[] getLoadAfter() {
				return loadAfter.toArray(new String[0]);
			}

			@Override
			public int getLevel() {
				return this.level;
			}

			@Override
			public boolean isActive() {
				return this.active;
			}

			@Override
			public void setActive(boolean active) {
				this.active = active;
			}

			@Override
			public void setLevel(int importance) {
				this.level = importance;
			}

			@Override
			public void stage(ClanSubCommand command) {
				commands.add(command);
			}

			@Override
			public void stage(Listener listener) {
				listeners.add(listener);
			}
		};
	}

	protected ClanAddon(ClanAddonContext context) {
		ClassLoader loader = this.getClass().getClassLoader();
		if (!(loader instanceof ClanAddonClassLoader) && !ClansAPI.class.getClassLoader().equals(loader))
			throw new InvalidAddonStateException("Addon not provided by " + ClanAddonClassLoader.class);
		this.classLoader = loader;
		this.logger = new ClanAddonLogger() {
			private final Logger LOG = Logger.getLogger("Minecraft");
			private final String addon;

			{
				this.addon = "Clan:" + getName();
			}

			public void log(Level level, String info) {
				this.LOG.log(level, "[" + addon + "]: " + info);
			}

			public void info(Supplier<String> info) {
				log(Level.INFO, info.get());
			}

			public void warn(Supplier<String> info) {
				log(Level.WARNING, info.get());
			}

			public void error(Supplier<String> info) {
				log(Level.SEVERE, info.get());
			}

			public void info(String info) {
				log(Level.INFO, info);
			}

			public void warn(String info) {
				log(Level.WARNING, info);
			}

			public void error(String info) {
				log(Level.SEVERE, info);
			}
		};
		this.loader = new ClanAddonLoader() {

			@Override
			public ClanAddon loadAddon(File jar) throws IOException, InvalidAddonException {
				if (!jar.isFile()) throw new InvalidAddonException("File " + jar.getName() + " not valid for addon processing.");
				return new ClanAddonClassLoader(jar, ClanAddon.this).getMainClass();
			}

			@Override
			public Deployable<Void> enableAddon(ClanAddon addon) {
				return Deployable.of(() -> {
					if (addon.classLoader.getParent().equals(getClassLoader())) {
						if (ClanAddonQueue.getInstance().get(addon.getName()) == null) {
							ClanAddonQueue.getInstance().register(addon);
							return;
						} else
							throw new ClanAddonRegistrationException("Addon " + addon + " is already registered and running!");
					}
					throw new InvalidAddonStateException("The provided addon doesn't belong to this loader's " + ClanAddonClassLoader.class);
				}, 0);
			}

			@Override
			public Deployable<Void> disableAddon(ClanAddon addon) {
				return Deployable.of(() -> {
					if (addon.classLoader.getParent().equals(getClassLoader())) {
						if (ClanAddonQueue.getInstance().get(addon.getName()) != null) {
							ClanAddonQueue.getInstance().remove(addon);
							return;
						} else throw new ClanAddonRegistrationException("Addon " + addon + " isn't registered!");
					}
					throw new InvalidAddonStateException("The provided addon doesn't belong to this loader's " + ClanAddonClassLoader.class);
				}, 0);
			}


		};
		this.context = context;
	}

	/**
	 * Get and manage services using clan addon objects instead of plugin as the key.
	 *
	 * @return A keyed service manager using clan addons for keys.
	 */
	public static @NotNull KeyedServiceManager<ClanAddon> getServicesManager() {
		return ClansAPI.getInstance().getServiceManager();
	}

	public static @NotNull ClanAddon getProvidingAddon(Class<?> c) throws InvalidAddonStateException {
		Class<?> clazz = Check.forNull(c, "Null classes cannot be attached to an addon");
		final ClassLoader cl = clazz.getClassLoader();
		if (!(cl instanceof ClanAddonClassLoader) && !cl.equals(ClansAPI.class.getClassLoader())) {
			throw new InvalidAddonStateException(clazz + " is not provided by " + ClanAddonClassLoader.class);
		}
		if (cl instanceof ClanAddonClassLoader) {
			ClanAddon addon = ((ClanAddonClassLoader) cl).getMainClass();
			if (addon == null) {
				throw new InvalidAddonStateException("Cannot get addon for " + clazz + " from a static initializer");
			}
			return addon;
		}
		throw new InvalidAddonStateException("Plugin provided addon detected, invalid retrieval.");
	}

	public static <T extends ClanAddon> @Nullable T getAddon(@NotNull Class<T> c) {
		for (ClanAddon addon : ClanAddonQueue.getInstance().get()) {
			if (c.isAssignableFrom(addon.getClass())) {
				return c.cast(addon);
			}
		}
		return null;
	}

	public abstract void onLoad();

	public abstract void onEnable();

	public abstract void onDisable();

	/**
	 * Get the unique id for this addon.
	 *
	 * @return The id for this addon.
	 */
	public @NotNull HUID getId() {
		return this.id;
	}

	/**
	 * Get the name of this addon.
	 *
	 * @return The name of this addon.
	 */
	public abstract @NotNull String getName();

	/**
	 * Get the description for this addon.
	 *
	 * @return The addon description.
	 */
	public abstract @NotNull String getDescription();

	/**
	 * Get the version of the addon.
	 *
	 * @return The addon version.
	 */
	public abstract @NotNull String getVersion();

	/**
	 * Get the addon authors.
	 *
	 * @return The addon authors.
	 */
	public abstract @NotNull String[] getAuthors();

	/**
	 * @return true if the plugin should persist on enable.
	 */
	public boolean isPersistent() {
		return true;
	}

	/**
	 * Translate placeholders for this given addon.
	 *
	 * @param player The player to use.
	 * @param param  The string to parse.
	 * @return The placeholder converted string.
	 */
	public String onPlaceholder(OfflinePlayer player, String param) {
		return param.equals(getName()) ? getName() + " " + getVersion() : null;
	}

	/**
	 * Locate and modify an existing file or create a new one.
	 *
	 * @param name      The name of the file.
	 * @param directory The directory the file lies within.
	 * @return A cached file manager.
	 */
	public final @NotNull FileManager getFile(String name, String... directory) {
		if (directory == null) {
			return getFile(Configurable.Type.YAML, name);
		} else {
			return getFile(Configurable.Type.YAML, name, directory);
		}
	}

	/**
	 * Locate and modify an existing file or create a new one.
	 *
	 * @param name      The name of the file.
	 * @param extension The file extension to use. Ex. [{@linkplain com.github.sanctum.panther.file.Configurable.Type#JSON}, {@linkplain com.github.sanctum.panther.file.Configurable.Type#YAML}]
	 * @param directory The directory the file lies within.
	 * @return A cached file manager.
	 */
	public final @NotNull FileManager getFile(Configurable.Extension extension, String name, String... directory) {
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
		return getClassLoader().getResourceAsStream(resource);
	}

	protected final @NotNull ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public final @NotNull ClanAddonContext getContext() {
		return context;
	}

	public final @NotNull ClanAddonLoader getLoader() {
		return loader;
	}

	/**
	 * Get the console logger for this addon.
	 *
	 * @return A console logger for this addon.
	 */
	public final @NotNull ClanAddonLogger getLogger() {
		return this.logger;
	}

	public final @NotNull Plugin getPlugin() {
		return getApi().getPlugin();
	}

	public final @NotNull Mailer getMailer() {
		return Mailer.empty(getPlugin());
	}

	/**
	 * @return The clans api instance.
	 */
	protected final @NotNull ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

	final void register() {
		PLACEMENT += 1;
		ClanAddonQueue.getInstance().ADDONS.add(this);
	}

	@Ordinal(121)
	final void remove() {
		PLACEMENT -= 1;
		if (getContext().isActive()) {
			ClansAPI.getInstance().getPlugin().getLogger().info("- Disabling addon " + '"' + getName() + '"' + " v" + getVersion());
			for (RegisteredListener l : HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin())) {
				if (Arrays.asList(getContext().getListeners()).contains(l.getListener())) {
					if (AnnotationDiscovery.of(Subscribe.class, l.getListener()).isPresent()) {
						LabyrinthProvider.getInstance().getEventMap().unsubscribe(l.getListener());
					} else {
						HandlerList.unregisterAll(l.getListener());
					}
				}
			}
			onDisable();
			getContext().setActive(false);
		}
		TaskScheduler.of(() -> ClanAddonQueue.getInstance().ADDONS.removeIf(c -> c.getName().equals(getName()))).scheduleLater(1);
	}

}
