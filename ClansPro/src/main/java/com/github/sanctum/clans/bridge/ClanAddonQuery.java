package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.service.AnnotationDiscovery;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class ClanAddonQuery {

	private static final Set<ClanAddon> CLAN_ADDONS = new HashSet<>();

	private static final List<String> DATA_LOG = new ArrayList<>();

	public static Set<ClanAddon> getRegisteredAddons() {
		return CLAN_ADDONS;
	}

	public static void unregisterAll(ClanAddon e) {
		e.setActive(false);
		e.onDisable();
		DATA_LOG.clear();
		ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		DATA_LOG.add("Clans [Pro] - Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> e.getAdditions().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			DATA_LOG.add(" - Unregistering cycle from handler-list.");
			for (Listener l : a) {
				HandlerList.unregisterAll(l);
				LabyrinthProvider.getInstance().getEventMap().unregister(l);
				count++;
			}
			if (count > 0) {
				ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Handler(s) successfully un-registered");
				DATA_LOG.add(" - (+" + count + ") Handler(s) found and un-registered");
			}
		} else {
			ClansAPI.getInstance().getPlugin().getLogger().info("- Failed to un-register events. No cycles currently running.");
			DATA_LOG.add(" - Failed to un-register events. No cycles currently running.");
		}
	}

	public static void registerAll(ClanAddon e) {
		e.setActive(true);
		e.onEnable();
		DATA_LOG.clear();
		ClansAPI.getInstance().getPlugin().getLogger().info("- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		DATA_LOG.add("Clans [Pro] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansAPI.getInstance().getPlugin()).stream().sequential().filter(r -> e.getAdditions().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		for (Listener add : e.getAdditions()) {
			if (a.contains(add)) {
				ClansAPI.getInstance().getPlugin().getLogger().info("- (+1) Handler failed to register. Already registered and skipping.");
				DATA_LOG.add(" - (+1) Handler failed to register. Already registered and skipping.");
			} else {
				AnnotationDiscovery<Subscribe, Object> discovery = AnnotationDiscovery.of(Subscribe.class, add);
				if (discovery.filter(m -> m.getParameters().length == 1 && m.getParameters()[0].getType().isAssignableFrom(Vent.class) && m.isAnnotationPresent(Subscribe.class)).count() > 0) {
					Vent.register(ClansAPI.getInstance().getPlugin(), add);
				}
				Bukkit.getPluginManager().registerEvents(add, ClansAPI.getInstance().getPlugin());
				count++;
			}
		}
		if (count > 0) {
			ClansAPI.getInstance().getPlugin().getLogger().info("- (+" + count + ") Listener(s) successfully re-registered");
			DATA_LOG.add(" - (+" + count + ") Listener(s) found and re-registered");
		}
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

	public static List<String> getRegisteredNames() {
		return CompletableFuture.supplyAsync(() -> getRegisteredAddons().stream().sequential().map(ClanAddon::getName).collect(Collectors.toList())).join();
	}

	public static void pickupCycle(Class<? extends ClanAddon> cycle) {
		try {
			ClanAddon c = cycle.newInstance();
			try {
				c.onLoad();
				c.register();
			} catch (NoClassDefFoundError e) {
				LabyrinthProvider.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + c.getName() + " will not work.");
				LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
			}
		} catch (InstantiationException | IllegalAccessException e) {
			ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to cast EventCycle to the class " + cycle.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
			e.printStackTrace();
		}
	}

	public static void pickupCycles(Plugin plugin, String packageName) {
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
				try {
					plugin.getLogger().info("- loading addon " + cycle.getName() + " version " + cycle.getVersion());
					cycle.onLoad();
					cycle.register();
				} catch (NoClassDefFoundError e) {
					LabyrinthProvider.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + cycle.getName() + " will not work.");
					LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
				}
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				ClansAPI.getInstance().getPlugin().getLogger().severe("- Unable to cast EventCycle to the class " + aClass.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
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
