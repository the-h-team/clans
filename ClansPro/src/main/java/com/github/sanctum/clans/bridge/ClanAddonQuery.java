package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClanException;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.service.AnnotationDiscovery;
import com.github.sanctum.labyrinth.event.custom.LabeledAs;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class ClanAddonQuery {
	protected static int COUNT = 0;
	private static final Set<ClanAddon> CLAN_ADDONS = new HashSet<>();

	private static final List<String> DATA_LOG = new ArrayList<>();

	public static Set<ClanAddon> getRegisteredAddons() {
		return CLAN_ADDONS;
	}

	public static boolean disable(ClanAddon e) {
		if (!e.isActive()) return false;
		e.setActive(false);
		e.onDisable();
		DATA_LOG.clear();
		ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		DATA_LOG.add("Clans [Pro] - Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> e.getAdditions().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			DATA_LOG.add(" - Unregistering addon from cache.");
			for (Listener l : a) {
				HandlerList.unregisterAll(l);
				LabeledAs an = l.getClass().getAnnotation(LabeledAs.class);
				LabyrinthProvider.getInstance().getEventMap().unregister(e.getPlugin(), an != null ? an.value() : null, l);
				count++;
			}
			if (count > 0) {
				ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Listener(s) successfully un-registered");
				DATA_LOG.add(" - (+" + count + ") Listener(s) found and un-registered");
			}
		} else {
			ClansAPI.getInstance().getPlugin().getLogger().info("- Failed to un-register listeners. None currently running.");
			DATA_LOG.add(" - Failed to un-register listeners. None currently running.");
		}
		return true;
	}

	public static boolean enable(ClanAddon e) {
		if (e.isActive()) return false;
		for (String precursor : e.getDependencies()) {
			ClanAddon addon = getAddon(precursor);
			ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + e.getName() + ". Please install the missing dependency for this addon.");
		}

		e.setActive(true);
		e.onEnable();
		DATA_LOG.clear();
		ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		DATA_LOG.add("Clans [Pro] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> e.getAdditions().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		for (Listener add : e.getAdditions()) {
			if (a.contains(add)) {
				ClansAPI.getInstance().getPlugin().getLogger().info("- (+1) Listener failed to register. Already registered and skipping.");
				DATA_LOG.add(" - (+1) Listener failed to register. Already registered and skipping.");
			} else {
				AnnotationDiscovery<Subscribe, Object> discovery = AnnotationDiscovery.of(Subscribe.class, add);
				if (discovery.filter(m -> m.getParameters().length == 1 && m.getParameters()[0].getType().isAssignableFrom(Vent.class) && m.isAnnotationPresent(Subscribe.class)).count() > 0) {
					LabyrinthProvider.getService(Service.VENT).register(ClansAPI.getInstance().getPlugin(), add);
				} else {
					Bukkit.getPluginManager().registerEvents(add, ClansAPI.getInstance().getPlugin());
				}
				count++;
			}
		}
		if (count > 0) {
			ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Listener(s) successfully re-registered");
			DATA_LOG.add(" - (+" + count + ") Listener(s) found and re-registered");
		}
		return true;
	}

	public static ClanAddon getAddon(String name) {
		return getRegisteredAddons().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	public static List<String> getUnusedNames() {
		List<String> array = new ArrayList<>();
		for (ClanAddon e : CLAN_ADDONS) {
			if (!e.isActive()) {
				array.add(e.getName());
			}
		}
		return array;
	}

	public static List<String> getUsedNames() {
		List<String> array = new ArrayList<>();
		for (ClanAddon e : CLAN_ADDONS) {
			if (e.isActive()) {
				array.add(e.getName());
			}
		}
		return array;
	}

	public static void adjust(ClanAddon addon) {
		for (String load : addon.getLoadBefore()) {
			ClanAddon a = ClanAddonQuery.getAddon(load);
			if (a != null) {
				if (a.getLevel() <= addon.getLevel()) {
					a.setLevel(addon.getLevel() + 1);
				}
			}
		}
	}

	public static void register(Class<? extends ClanAddon> cycle) {

		ClanException.call(ClanAddonRegistrationException::new).check(getAddon(cycle.getName())).run("Addon's can only be registered one time!", true);

		Plugin PRO = ClansAPI.getInstance().getPlugin();
		Logger l = PRO.getLogger();
		try {
			ClanAddon c = cycle.newInstance();
			c.loadDepends();
			c.onLoad();
			c.register();
			adjust(c);
			for (String precursor : c.getDependencies()) {
				ClanAddon addon = ClanAddonQuery.getAddon(precursor);
				ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + c.getName() + ". Please install the missing dependency for this addon.");
			}
			c.onEnable();
			if (c.persist()) {

				l.info(" ");
				l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				l.info("- Addon: " + c.getName());
				l.info("- Description: " + c.getDescription());
				l.info("- Persistent: (" + c.persist() + ")");
				l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				l.info(" ");
				l.info("- Listeners: (" + c.getAdditions().size() + ")");
				for (Listener addition : c.getAdditions()) {
					String format = addition.getClass().getSimpleName().isEmpty() ? "{REDACTED}" : addition.getClass().getSimpleName();
					boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						l.info("- [" + c.getName() + "] (+1) Listener " + format + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, PRO);
					} else {
						l.info("- [" + c.getName() + "] (-1) Listener " + format + " already loaded. Skipping.");
					}
				}
			} else {
				l.info(" ");
				l.info("- Addon: " + c.getName());
				l.info("- Description: " + c.getDescription());
				l.info("- Persistent: (" + c.persist() + ")");
				c.remove();
				l.info(" ");
				l.info("- Listeners: (" + c.getAdditions().size() + ")");
				for (Listener addition : c.getAdditions()) {
					l.info("- [" + c.getName() + "] (+1) Addon failed to load due to no persistence.");
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			l.severe("- Unable to cast Addon to the class " + cycle.getName() + ". This likely means you are not implementing the Addon interface for your event class properly.");
			e.printStackTrace();
		}
	}

	public static void register(Plugin plugin, String packageName) {
		Plugin PRO = ClansAPI.getInstance().getPlugin();
		Logger l = PRO.getLogger();
		List<ClanAddon> data = new Registry<>(ClanAddon.class)
				.source(plugin)
				.pick(packageName)
				.operate(e -> {
					ClanException.call(ClanAddonRegistrationException::new).check(e).run("Addon's can only be registered one time!", true);
					e.loadDepends();
					e.onLoad();
					e.register();
				}).getData();

		l.info("- Found (" + data.size() + ") clan addon(s)");

		data.forEach(ClanAddonQuery::adjust);

		for (ClanAddon e : data.stream().sorted(Comparator.comparingInt(ClanAddon::getLevel)).collect(Collectors.toCollection(LinkedHashSet::new))) {
			for (String precursor : e.getDependencies()) {
				ClanAddon addon = ClanAddonQuery.getAddon(precursor);
				ClanException.call(ClanAddonDependencyException::new).check(addon).run("Missing dependency " + precursor + " for addon " + e.getName() + ". Please install the missing dependency for this addon.");
			}
			e.onEnable();
			if (e.persist()) {

				l.info(" ");
				l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				l.info("- Addon: " + e.getName());
				l.info("- Version: " + e.getVersion());
				l.info("- Author(s): " + Arrays.toString(e.getAuthors()));
				l.info("- Description: " + e.getDescription());
				l.info("- Persistent: (" + e.persist() + ")");
				l.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				l.info(" ");
				l.info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					String format = addition.getClass().getSimpleName().isEmpty() ? "{REDACTED}" : addition.getClass().getSimpleName();
					boolean registered = HandlerList.getRegisteredListeners(PRO).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						l.info("- [" + e.getName() + "] (+1) Class " + format + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, PRO);
					} else {
						l.info("- [" + e.getName() + "] (-1) Class " + format + " already loaded. Skipping.");
					}
				}
			} else {
				l.info(" ");
				l.info("- Addon: " + e.getName());
				l.info("- Description: " + e.getDescription());
				l.info("- Persistent: (" + e.persist() + ")");
				e.remove();
				l.info(" ");
				l.info("- Listeners: (" + e.getAdditions().size() + ")");
				for (Listener addition : e.getAdditions()) {
					l.info("- [" + e.getName() + "] (+1) Addon failed to load due to no persistence.");
				}
			}
		}
	}

	public static void load(ClanAddon addon) {
		ClanException.call(ClanAddonRegistrationException::new).check(getAddon(addon.getName())).run("Addon's can only be loaded one time!", true);
		try {
			addon.loadDepends();
			addon.onLoad();
			addon.register();
		} catch (NoClassDefFoundError e) {
			LabyrinthProvider.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + addon.getName() + " will not work.");
			LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
		}
	}

	public static void load(Class<? extends ClanAddon> addon) {
		try {
			ClanAddon c = addon.newInstance();
			ClanException.call(ClanAddonRegistrationException::new).check(getAddon(c.getName())).run("Addon's can only be loaded one time!", true);
			try {
				c.loadDepends();
				c.onLoad();
				c.register();
			} catch (NoClassDefFoundError e) {
				LabyrinthProvider.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + c.getName() + " will not work.");
				LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
			}
		} catch (InstantiationException | IllegalAccessException e) {
			ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to cast ClanAddon to the class " + addon.getName() + ". This likely means you are not extending the ClanAddon abstraction for your addon class properly.");
			e.printStackTrace();
		}
	}

	public static void load(Plugin plugin, String packageName) {
		Set<Class<?>> classes = Sets.newHashSet();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(URLDecoder.decode(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
					break;
				}
				if (ClanAddon.class.isAssignableFrom(clazz)) {
					classes.add(clazz);
				}
			}
		}
		plugin.getLogger().info("/▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ Loading ]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		for (Class<?> aClass : classes) {
			try {
				ClanAddon cycle = (ClanAddon) aClass.getDeclaredConstructor().newInstance();
				ClanException.call(ClanAddonRegistrationException::new).check(getAddon(cycle.getName())).run("Addon's can only be loaded one time!", true);
				try {
					plugin.getLogger().info("- loading addon " + cycle.getName() + " version " + cycle.getVersion());
					cycle.loadDepends();
					cycle.onLoad();
					cycle.register();
				} catch (NoClassDefFoundError e) {
					LabyrinthProvider.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + cycle.getName() + " will not work.");
					LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
				}
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to cast ClanAddon to the class " + aClass.getName() + ". This likely means you are not extending the ClanAddon abstraction for your addon class properly.");
				e.printStackTrace();
				break;
			}
		}
		plugin.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ Loading ]▬▬▬▬▬▬▬▬▬▬▬▬▬▬/");
	}

	public static String[] getDataLog() {
		return DATA_LOG.toArray(new String[0]);
	}

}
