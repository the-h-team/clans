package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.plugin.Plugin;

final class ClanAddonClassLoader extends URLClassLoader {

	private static final Field PLUGIN_CLASS_MAP;

	static {
		try {
			PLUGIN_CLASS_MAP = Class.forName("org.bukkit.plugin.java.PluginClassLoader").getDeclaredField("classes");
			PLUGIN_CLASS_MAP.setAccessible(true);
		} catch (NoSuchFieldException | ClassNotFoundException e) {
			throw new IllegalStateException("Unable to reach class map", e);
		}
	}

	private final Plugin plugin;
	final ClanAddon addon;

	ClanAddonClassLoader(File file) throws IOException, InvalidAddonException {
		super(new URL[]{file.toURI().toURL()}, ClansAPI.class.getClassLoader());
		final List<Class<?>> loadedClasses = new ArrayList<>();
		this.plugin = ClansAPI.getInstance().getPlugin();
		if (!file.isFile()) throw new InvalidAddonException("The provided file is not a jar file!");
		new JarFile(file).stream()
				.map(ZipEntry::getName)
				.filter(entryName -> entryName.contains(".class") && !entryName.contains("$"))
				.map(classPath -> classPath.replace('/', '.'))
				.map(className -> className.substring(0, className.length() - 6))
				.forEach(s -> {
					final Class<?> resolvedClass;
					try {
						resolvedClass = loadClass(s, true);
					} catch (ClassNotFoundException e) {
						plugin.getLogger().warning(() -> "Unable to inject '" + s + "'");
						plugin.getLogger().warning(e::getMessage);
						return;
					}
					getClassMap(plugin).put(s, resolvedClass);
					plugin.getLogger().finest(() -> "Loaded '" + s + "' successfully.");
					loadedClasses.add(resolvedClass);
				});
		try {
			Class<? extends ClanAddon> addonClass = loadedClasses.stream().filter(ClanAddon.class::isAssignableFrom).findFirst().map(aClass -> (Class<? extends ClanAddon>) aClass).get();
			this.addon = addonClass.getDeclaredConstructor().newInstance();
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
			throw new InvalidAddonException("No public constructor", ex);
		} catch (InstantiationException ex) {
			throw new InvalidAddonException("Unusual constructor args detected.", ex);
		}
	}

	ClanAddonClassLoader(File file, ClanAddon parent) throws IOException, InvalidAddonException {
		super(new URL[]{file.toURI().toURL()}, parent.getClassLoader());
		final List<Class<?>> loadedClasses = new ArrayList<>();
		this.plugin = ClansAPI.getInstance().getPlugin();
		if (!file.isFile()) throw new InvalidAddonException("The provided file is not a jar file!");
		new JarFile(file).stream()
				.map(ZipEntry::getName)
				.filter(entryName -> entryName.contains(".class") && !entryName.contains("$"))
				.map(classPath -> classPath.replace('/', '.'))
				.map(className -> className.substring(0, className.length() - 6))
				.forEach(s -> {
					final Class<?> resolvedClass;
					try {
						resolvedClass = loadClass(s, true);
					} catch (ClassNotFoundException e) {
						plugin.getLogger().warning(() -> "Unable to inject '" + s + "'");
						plugin.getLogger().warning(e::getMessage);
						return;
					}
					getClassMap(plugin).put(s, resolvedClass);
					plugin.getLogger().finest(() -> "Loaded '" + s + "' successfully.");
					loadedClasses.add(resolvedClass);
				});
		try {
			Class<? extends ClanAddon> addonClass = loadedClasses.stream().filter(ClanAddon.class::isAssignableFrom).findFirst().map(aClass -> (Class<? extends ClanAddon>) aClass).get();
			this.addon = addonClass.getDeclaredConstructor().newInstance();
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
			throw new InvalidAddonException("No public constructor", ex);
		} catch (InstantiationException ex) {
			throw new InvalidAddonException("Unusual constructor args detected.", ex);
		}
	}

	private static Map<String, Class<?>> getClassMap(Plugin javaPlugin) throws IllegalStateException {
		try {
			//noinspection unchecked
			return (Map<String, Class<?>>) PLUGIN_CLASS_MAP.get(javaPlugin.getClass().getClassLoader());
		} catch (ClassCastException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "ClanAddonClassLoader{" +
				"addon=" + addon +
				'}';
	}
}
