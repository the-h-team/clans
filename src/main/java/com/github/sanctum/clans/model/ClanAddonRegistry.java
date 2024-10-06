package com.github.sanctum.clans.model;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.task.TaskMonitor;
import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.util.Check;
import com.github.sanctum.panther.util.Task;
import com.github.sanctum.panther.util.TaskChain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

public final class ClanAddonRegistry {
	static ClanAddonRegistry instance;
	final Set<Clan.Addon> ADDONS = new HashSet<>();

	ClanAddonRegistry() {

	}

	public int bump(@NotNull Clan.Addon addon) {
		int gain = 0;
		for (String load : addon.getContext().getLoadBefore()) {
			Clan.Addon a = get(load);
			if (a != null) {
				if (a.getContext().getLevel() <= addon.getContext().getLevel()) {
					a.getContext().setLevel(addon.getContext().getLevel() + 1);
					gain++;
				}
			}
		}
		return gain;
	}

	public @NotNull ClanAddonLoadResult register(@NotNull Clan.Addon addon) {
		ClanException.call(ClanAddonRegistrationException::new).check(get(addon.getName())).run("Addon's can only be registered one time!", true);
		if (addon.getContext().isActive()) return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return false;
			}

			@Override
			public String[] read() {
				return new String[]{"Addon already active!"};
			}

			@Override
			public int tries() {
				return 0;
			}
		};
		Plugin PRO = addon.getPlugin();
		ClanAddonLogger l = addon.getLogger();
		load(addon);
		bump(addon);
		for (String precursor : addon.getContext().getDependencies()) {
			Clan.Addon a = get(precursor);
			ClanException.call(ClanAddonDependencyException::new).check(a).run("Missing dependency " + precursor + " for addon " + addon.getName() + ". Please install the missing dependency for this addon.");
		}
		final ClanAddonLoadAttempt attempt = new ClanAddonLoadAttempt() {
			int attempts;
			final PantherCollection<String> response = new PantherList<>();

			@Override
			public int count() {
				return attempts;
			}

			@Override
			public String[] read() {
				return response.toArray(new String[0]);
			}

			@Override
			public void println(String text) {
				response.add(text);
			}

			@Override
			public void println(Number number) {
				response.add(number.toString());
			}

			@Override
			public boolean load(@NotNull Object o) {
				if (o instanceof Clan.Addon) {
					boolean activate = true;
					for (String plugin : ((Clan.Addon) o).getContext().getLoadAfter()) {
						Plugin pl = Bukkit.getPluginManager().getPlugin(plugin);
						if (pl == null) {
							Clan.Addon t = get(plugin);
							if (t == null) activate = false;
						}
					}
					attempts++;
					return activate;
				}
				return false;
			}
		};
		if (!attempt.load(addon)) {
			String key = "Clans::" + addon.getName() + ";enable-attempt";
			if (TaskMonitor.getLocalInstance().get(key) == null) {
				Task t = new Task(key, Task.REPEATABLE, TaskChain.getSynchronous()) {

					private static final long serialVersionUID = -7922499425255510018L;

					@Ordinal
					public void onRun() {
						if (attempt.count() == 3) {
							// loading of addon failed.
							ClansAPI.getInstance().getPlugin().getLogger().info("- Loading of addon " + '"' + addon.getName() + '"' + " failed due to 1 or more missing dependencies.");
							attempt.println("Clans [Pro] - Unable to load addon " + '"' + addon.getName() + '"' + " due to 1 or more missing dependencies.");
							cancel();
						} else {
							if (attempt.load(addon)) {
								if (addon.isPersistent()) {
									addon.getContext().setActive(true);
									addon.onEnable();
									l.info(" ");
									l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
									l.info("- Addon: " + addon.getName());
									l.info("- Description: " + addon.getDescription());
									l.info("- Persistent: (" + addon.isPersistent() + ")");
									l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
									l.info(" ");
									l.info("- Listeners: (" + addon.getContext().getListeners().length + ")");
									for (Listener addition : addon.getContext().getListeners()) {
										String format = addition.getClass().getSimpleName().isEmpty() ? "{REDACTED}" : addition.getClass().getSimpleName();
										boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
										if (!registered) {
											l.info("- [" + addon.getName() + "] (+1) Listener " + format + " loaded");
											VentMap.getInstance().subscribe((Vent.Host) PRO, addition);
										} else {
											l.info("- [" + addon.getName() + "] (-1) Listener " + format + " already loaded. Skipping.");
										}
									}
								} else {
									l.info(" ");
									l.info("- Addon: " + addon.getName());
									l.info("- Description: " + addon.getDescription());
									l.info("- Persistent: (" + addon.isPersistent() + ")");
									addon.remove();
									l.info(" ");
									l.info("- Listeners: (" + addon.getContext().getListeners().length + ")");
									for (Listener addition : addon.getContext().getListeners()) {
										l.info("- [" + addition.getClass().getSimpleName() + "] (+1) Listener failed to load due to no persistence.");
									}
								}
							} else {
								ClansAPI.getInstance().getPlugin().getLogger().info("- Loading of addon " + addon.getName() + " failed attempt:(" + attempt.count() + ") trying again...");
								attempt.println("- Loading of addon " + addon.getName() + " failed attempt:(" + attempt.count() + ") trying again...");
							}
						}
					}

				};
				TaskChain.getSynchronous().repeat(t, 0, 500);
			}
			return new ClanAddonLoadResult() {
				@Override
				public boolean get() {
					return false;
				}

				@Override
				public String[] read() {
					return attempt.read();
				}

				@Override
				public int tries() {
					return attempt.count();
				}
			};
		}
		if (addon.isPersistent()) {
			addon.getContext().setActive(true);
			addon.onEnable();
			l.info(" ");
			l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			l.info("- Addon: " + addon.getName());
			l.info("- Description: " + addon.getDescription());
			l.info("- Persistent: (" + addon.isPersistent() + ")");
			l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			l.info(" ");
			l.info("- Listeners: (" + addon.getContext().getListeners().length + ")");
			for (Listener addition : addon.getContext().getListeners()) {
				String format = addition.getClass().getSimpleName().isEmpty() ? "{REDACTED}" : addition.getClass().getSimpleName();
				boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
				if (!registered) {
					l.info("- [" + addon.getName() + "] (+1) Listener " + format + " loaded");
					VentMap.getInstance().subscribe((Vent.Host) PRO, addition);
				} else {
					l.info("- [" + addon.getName() + "] (-1) Listener " + format + " already loaded. Skipping.");
				}
			}
		} else {
			l.info(" ");
			l.info("- Addon: " + addon.getName());
			l.info("- Description: " + addon.getDescription());
			l.info("- Persistent: (" + addon.isPersistent() + ")");
			addon.remove();
			l.info(" ");
			l.info("- Listeners: (" + addon.getContext().getListeners().length + ")");
			for (Listener addition : addon.getContext().getListeners()) {
				l.info("- [" + addition.getClass().getSimpleName() + "] (+1) Listener failed to load due to no persistence.");
			}
		}
		return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return true;
			}

			@Override
			public String[] read() {
				return new String[]{"Addon loaded successfully."};
			}

			@Override
			public int tries() {
				return 1;
			}
		};
	}

	public @NotNull ClanAddonLoadResult register(@NotNull Class<? extends Clan.Addon> addon) {
		try {
			Clan.Addon c = addon.newInstance();
			return register(c);
		} catch (InstantiationException | IllegalAccessException e) {
			ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to cast Addon to the class " + addon.getName() + ". This likely means you are not implementing the Addon interface for your event class properly.");
			e.printStackTrace();
		}
		return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return false;
			}

			@Override
			public String[] read() {
				return new String[]{"Unable to register addon"};
			}

			@Override
			public int tries() {
				return 1;
			}
		};
	}

	public @NotNull ClanAddonLoadResult[] register(@NotNull Plugin plugin, @NotNull String packageName) {
		Plugin PRO = ClansAPI.getInstance().getPlugin();
		Logger l = PRO.getLogger();
		List<Clan.Addon> data = new Registry<>(Clan.Addon.class)
				.source(plugin)
				.filter(packageName)
				.operate(e -> {
					ClanException.call(ClanAddonRegistrationException::new).check(e).run("Addon's can only be registered one time!", true);
					load(e);
				}).getData();

		l.info("- Found (" + data.size() + ") clan addon(s)");
		List<ClanAddonLoadResult> results = new ArrayList<>();
		data.forEach(addon -> results.add(register(addon)));

		return results.toArray(new ClanAddonLoadResult[0]);
	}

	public boolean remove(@NotNull Clan.Addon addon) {
		if (!ADDONS.contains(addon)) return false;
		if (addon.getContext().isActive())
			throw new InvalidAddonStateException("Addons can only be removed once they have been de-activated fully!");
		addon.remove();
		return true;
	}

	public @NotNull ClanAddonLoadResult load(@NotNull Clan.Addon addon) {
		ClanException.call(ClanAddonRegistrationException::new).check(get(addon.getName())).run("Addon's can only be loaded one time!", true);
		try {
			addon.onLoad();
			addon.register();
			return new ClanAddonLoadResult() {
				@Override
				public boolean get() {
					return true;
				}

				@Override
				public String[] read() {
					return new String[0];
				}

				@Override
				public int tries() {
					return 1;
				}
			};
		} catch (NoClassDefFoundError e) {
			LabyrinthProvider.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + addon.getName() + " will not work.");
			LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
		}
		return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return false;
			}

			@Override
			public String[] read() {
				return new String[0];
			}

			@Override
			public int tries() {
				return 1;
			}
		};
	}

	public @NotNull ClanAddonLoadResult load(@NotNull Class<? extends Clan.Addon> addon) {
		try {
			Clan.Addon c = addon.newInstance();
			return load(c);
		} catch (InstantiationException | IllegalAccessException e) {
			ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to cast ClanAddon to the class " + addon.getName() + ". This likely means you are not extending the ClanAddon abstraction for your addon class properly.");
			e.printStackTrace();
		}
		return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return false;
			}

			@Override
			public String[] read() {
				return new String[]{"Unable to load addon."};
			}

			@Override
			public int tries() {
				return 1;
			}
		};
	}

	public @NotNull ClanAddonLoadResult enable(@NotNull Clan.Addon e) {
		Check.argument(get().contains(e), "To enable addons they must first be properly loaded! Usage of ClanAddonQuery#load(ClanAddon) expected first!");
		if (e.getContext().isActive()) return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return false;
			}

			@Override
			public String[] read() {
				return new String[]{"Addon already active!"};
			}

			@Override
			public int tries() {
				return 0;
			}
		};
		for (String precursor : e.getContext().getDependencies()) {
			Clan.Addon addon = get(precursor);
			ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + e.getName() + ". Please install the missing dependency for this addon.");
		}
		final ClanAddonLoadAttempt attempt = new ClanAddonLoadAttempt() {
			int attempts;
			final PantherCollection<String> response = new PantherList<>();

			@Override
			public int count() {
				return attempts;
			}

			@Override
			public String[] read() {
				return response.toArray(new String[0]);
			}

			@Override
			public void println(String text) {
				response.add(text);
			}

			@Override
			public void println(Number number) {
				response.add(number.toString());
			}

			@Override
			public boolean load(@NotNull Object o) {
				if (o instanceof Clan.Addon) {
					boolean activate = true;
					for (String plugin : ((Clan.Addon) o).getContext().getLoadAfter()) {
						Plugin pl = Bukkit.getPluginManager().getPlugin(plugin);
						if (pl == null) {
							Clan.Addon t = get(plugin);
							if (t == null) activate = false;
						}
					}
					attempts++;
					return activate;
				}
				return false;
			}
		};
		if (!attempt.load(e)) {
			String key = "Clans::" + e.getName() + ";enable-attempt";
			if (TaskMonitor.getLocalInstance().get(key) == null) {
				Task t = new Task(key, Task.REPEATABLE, TaskChain.getSynchronous()) {

					private static final long serialVersionUID = -7922499425255510018L;

					@Ordinal
					public void onRun() {
						if (attempt.count() == 3) {
							// loading of addon failed.
							ClansAPI.getInstance().getPlugin().getLogger().info("- Pickup for " + '"' + e.getName() + '"' + " addon failed.");
							attempt.println("Clans [Pro] - Unable to load addon " + '"' + e.getName() + '"' + " due to 1 or more missing dependencies.");
							cancel();
						} else {
							if (attempt.load(e)) {
								e.getContext().setActive(true);
								e.onEnable();
								ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
								attempt.println("Clans [Pro] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
								List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> Arrays.asList(e.getContext().getListeners()).contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
								int count = 0;
								for (Listener add : e.getContext().getListeners()) {
									if (a.contains(add)) {
										ClansAPI.getInstance().getPlugin().getLogger().info("- (+1) Listener failed to register. Already registered and skipping.");
										attempt.println(" - (+1) Listener failed to register. Already registered and skipping.");
									} else {
										if (AnnotationDiscovery.of(Subscribe.class, add).isPresent()) {
											VentMap.getInstance().subscribe((Vent.Host) ClansAPI.getInstance().getPlugin(), add);
										} else {
											Bukkit.getPluginManager().registerEvents(add, ClansAPI.getInstance().getPlugin());
										}
										count++;
									}
								}
								if (count > 0) {
									ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Listener(s) successfully re-registered");
									attempt.println(" - (+" + count + ") Listener(s) found and re-registered");
								}
							} else {
								ClansAPI.getInstance().getPlugin().getLogger().info("- Loading of addon " + e.getName() + " failed attempt:(" + attempt.count() + ") trying again.");
								attempt.println("- Loading of addon " + e.getName() + " failed attempt:(" + attempt.count() + ") trying again.");
							}
						}
					}

				};
				TaskChain.getSynchronous().repeat(t, 0, 500);
			}
			return new ClanAddonLoadResult() {
				@Override
				public boolean get() {
					return false;
				}

				@Override
				public String[] read() {
					return attempt.read();
				}

				@Override
				public int tries() {
					return attempt.count();
				}
			};
		} else {
			e.getContext().setActive(true);
			e.onEnable();
			ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
			attempt.println("Clans [Pro] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
			List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> Arrays.asList(e.getContext().getListeners()).contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
			int count = 0;
			for (Listener add : e.getContext().getListeners()) {
				if (a.contains(add)) {
					ClansAPI.getInstance().getPlugin().getLogger().info("- (+1) Listener failed to register. Already registered and skipping.");
					attempt.println(" - (+1) Listener failed to register. Already registered and skipping.");
				} else {
					if (AnnotationDiscovery.of(Subscribe.class, add).isPresent()) {
						VentMap.getInstance().subscribe((Vent.Host) ClansAPI.getInstance().getPlugin(), add);
					} else {
						Bukkit.getPluginManager().registerEvents(add, ClansAPI.getInstance().getPlugin());
					}
					count++;
				}
			}
			if (count > 0) {
				ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Listener(s) successfully re-registered");
				attempt.println(" - (+" + count + ") Listener(s) found and re-registered");
			}
			return new ClanAddonLoadResult() {
				@Override
				public boolean get() {
					return true;
				}

				@Override
				public String[] read() {
					return attempt.read();
				}

				@Override
				public int tries() {
					return attempt.count();
				}
			};
		}
	}

	public @NotNull ClanAddonLoadResult disable(@NotNull Clan.Addon e) {
		Check.argument(get().contains(e), "To disable addons they must first be properly loaded!");
		if (!e.getContext().isActive()) return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return false;
			}

			@Override
			public String[] read() {
				return new String[]{"Addon already not active!"};
			}

			@Override
			public int tries() {
				return 0;
			}
		};
		PantherCollection<String> info = new PantherList<>();
		e.getContext().setActive(false);
		e.onDisable();
		ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		info.add("Clans [Pro] - Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> Arrays.asList(e.getContext().getListeners()).contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			info.add(" - Unregistering addon from cache.");
			for (Listener l : a) {
				if (AnnotationDiscovery.of(Subscribe.class, l).isPresent()) {
					LabyrinthProvider.getInstance().getEventMap().unsubscribe(l);
				} else {
					HandlerList.unregisterAll(l);
				}
				count++;
			}
			if (count > 0) {
				ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Listener(s) successfully un-registered");
				info.add(" - (+" + count + ") Listener(s) found and un-registered");
			}
		} else {
			ClansAPI.getInstance().getPlugin().getLogger().info("- Failed to un-register listeners. None currently running.");
			info.add(" - Failed to un-register listeners. None currently running.");
		}
		return new ClanAddonLoadResult() {
			@Override
			public boolean get() {
				return true;
			}

			@Override
			public String[] read() {
				return info.toArray(new String[0]);
			}

			@Override
			public int tries() {
				return 1;
			}
		};
	}

	public @NotNull ClanAddonLoadResult[] load(@NotNull Plugin plugin, @NotNull String packageName) {
		List<Clan.Addon> data = new Registry<>(Clan.Addon.class)
				.source(plugin)
				.filter(packageName)
				.confine()
				.getData();
		List<ClanAddonLoadResult> results = new ArrayList<>();
		data.forEach(addon -> results.add(load(addon)));
		return results.toArray(new ClanAddonLoadResult[0]);
	}

	public Set<Clan.Addon> get() {
		return Collections.unmodifiableSet(ADDONS);
	}

	public Clan.Addon get(String name) {
		return get().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	public List<String> getEnabled() {
		return ADDONS.stream().filter(addon -> addon.getContext().isActive()).map(Clan.Addon::getName).collect(Collectors.toList());
	}

	public List<String> getDisabled() {
		return ADDONS.stream().filter(addon -> !addon.getContext().isActive()).map(Clan.Addon::getName).collect(Collectors.toList());
	}

	public static @NotNull ClanAddonRegistry getInstance() {
		return instance == null ? (instance = new ClanAddonRegistry()) : instance;
	}

}
