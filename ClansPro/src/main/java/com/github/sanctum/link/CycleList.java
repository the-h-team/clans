package com.github.sanctum.link;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.labyrinth.Labyrinth;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
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

public class CycleList {

	private static final Collection<EventCycle> registeredCycles = new HashSet<>();

	private static final List<String> dataLog = new ArrayList<>();

	public static Collection<EventCycle> getRegisteredCycles() {
		return CompletableFuture.supplyAsync(() -> registeredCycles).join();
	}

	public static void unregisterAll(EventCycle e) {
		e.setActive(false);
		e.onDisable();
		dataLog.clear();
		ClansPro.getInstance().getLogger().info("- Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		dataLog.add("Clans [Pro] - Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansPro.getInstance()).stream().sequential().filter(r -> e.getAdditions().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			dataLog.add(" - Unregistering cycle from handler-list.");
			for (Listener l : a) {
				HandlerList.unregisterAll(l);
				count++;
			}
			if (count > 0) {
				ClansPro.getInstance().getLogger().info("- (+" + count + ") Handler(s) successfully un-registered");
				dataLog.add(" - (+" + count + ") Handler(s) found and un-registered");
			}
		} else {
			ClansPro.getInstance().getLogger().info("- Failed to un-register events. No cycles currently running.");
			dataLog.add(" - Failed to un-register events. No cycles currently running.");
		}
	}

	public static void registerAll(EventCycle e) {
		e.setActive(true);
		e.onEnable();
		dataLog.clear();
		ClansPro.getInstance().getLogger().info("- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		dataLog.add("Clans [Pro] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(ClansPro.getInstance()).stream().sequential().filter(r -> e.getAdditions().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		for (Listener add : e.getAdditions()) {
			if (a.contains(add)) {
				ClansPro.getInstance().getLogger().info("- (+1) Handler failed to register. Already registered and skipping.");
				dataLog.add(" - (+1) Handler failed to register. Already registered and skipping.");
			} else {
				Bukkit.getPluginManager().registerEvents(add, ClansPro.getInstance());
				count++;
			}
		}
		if (count > 0) {
			ClansPro.getInstance().getLogger().info("- (+" + count + ") Listener(s) successfully re-registered");
			dataLog.add(" - (+" + count + ") Listener(s) found and re-registered");
		}
	}

	public static EventCycle getAddon(String name) {
		return getRegisteredCycles().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	public static List<String> getUnusedAddons() {
		List<String> array = new ArrayList<>();
		for (EventCycle e : registeredCycles) {
			if (!e.isActive()) {
				array.add(e.getName());
			}
		}
		return array;
	}

	public static List<String> getUsedAddons() {
		List<String> array = new ArrayList<>();
		for (EventCycle e : registeredCycles) {
			if (e.isActive()) {
				array.add(e.getName());
			}
		}
		return array;
	}

	public static List<String> getRegisteredAddons() {
		return CompletableFuture.supplyAsync(() -> getRegisteredCycles().stream().sequential().map(EventCycle::getName).collect(Collectors.toList())).join();
	}

	public static void pickupCycle(Class<? extends EventCycle> cycle) {
		try {
			EventCycle c = cycle.newInstance();
			try {
				c.onLoad();
				c.register();
				c.onEnable();
			} catch (NoClassDefFoundError e) {
				Labyrinth.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + c.getName() + " will not work.");
				Labyrinth.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
			}
		} catch (InstantiationException | IllegalAccessException e) {
			ClansPro.getInstance().getLogger().severe("- Unable to cast EventCycle to the class " + cycle.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
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
					ClansPro.getInstance().getLogger().severe("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
					break;
				}
				if (EventCycle.class.isAssignableFrom(clazz)) {
					classes.add(clazz);
				}
			}
		}
		for (Class<?> aClass : classes) {
			try {
				EventCycle cycle = (EventCycle) aClass.getDeclaredConstructor().newInstance();
				try {
					cycle.onLoad();
					cycle.register();
					cycle.onEnable();
				} catch (NoClassDefFoundError e) {
					Labyrinth.getInstance().getLogger().warning("- Your Labyrinth core is out-dated. Additions for addon " + cycle.getName() + " will not work.");
					Labyrinth.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
				}
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				ClansPro.getInstance().getLogger().severe("- Unable to cast EventCycle to the class " + aClass.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
				e.printStackTrace();
				break;
			}
		}
	}

	public static String[] getDataLog() {
		return dataLog.toArray(new String[0]);
	}

}
