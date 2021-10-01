package com.github.sanctum.clans;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.plugin.Plugin;

public final class ClanAddonClassLoader extends URLClassLoader {

	private static final Field PLUGIN_CLASS_MAP;

	static {
		try {
			PLUGIN_CLASS_MAP = Class.forName("org.bukkit.plugin.java.PluginClassLoader").getDeclaredField("classes");
			PLUGIN_CLASS_MAP.setAccessible(true);
		} catch (NoSuchFieldException | ClassNotFoundException e) {
			throw new IllegalStateException("Unable to reach class map", e);
		}
	}

	private final List<Class<?>> loadedClasses = new ArrayList<>();
	private final Plugin plugin;
	final ClanAddon addon;

	ClanAddonClassLoader(File file) throws IOException, InvalidAddonException {
		super(ImmutableMap.of(file.toURI().toURL(), file).keySet().toArray(new URL[0]), ClansAPI.getInstance().getPlugin().getClass().getClassLoader());
		this.plugin = ClansAPI.getInstance().getPlugin();
		if (!file.isFile()) throw new InvalidAddonException("The provided file is not a jar file!");
		injectJar(file);
		try {
			Class<? extends ClanAddon> addonClass = loadedClasses.stream().filter(ClanAddon.class::isAssignableFrom).findFirst().map(aClass -> (Class<? extends ClanAddon>) aClass).get();
			this.addon = addonClass.newInstance();
		} catch (IllegalAccessException ex) {
			throw new InvalidAddonException("No public constructor", ex);
		} catch (InstantiationException ex) {
			throw new InvalidAddonException("Unusual constructor args detected.", ex);
		}

	}

	public final Class<?> findClass(String name) {
		return loadedClasses.stream().filter(c -> c.getName().contains(name)).findFirst().orElse(null);
	}

	final Class<?> resolveClass(String name) throws ClassNotFoundException {
		return loadClass(name, true);
	}

	void injectClass(String className) {
		final Class<?> resolvedClass;
		try {
			resolvedClass = resolveClass(className);
		} catch (ClassNotFoundException e) {
			plugin.getLogger().warning(() -> "Unable to inject '" + className + "'");
			plugin.getLogger().warning(e::getMessage);
			return;
		}
		getClassMap(plugin).put(className, resolvedClass);
		plugin.getLogger().finest(() -> "Loaded '" + className + "' successfully.");
		loadedClasses.add(resolvedClass);
	}

	private void injectJar(File file) throws IOException {
		Map<URL, JarFile> jars = ImmutableMap.of(file.toURI().toURL(), new JarFile(file));
		if (jars.isEmpty()) return;
		jars.values().forEach(jarFile -> jarFile.stream()
				.map(ZipEntry::getName)
				.filter(entryName -> entryName.contains(".class") && !entryName.contains("$"))
				.map(classPath -> classPath.replace('/', '.'))
				.map(className -> className.substring(0, className.length() - 6))
				.forEach(this::injectClass));
	}

	private static Map<String, Class<?>> getClassMap(Plugin javaPlugin) throws IllegalStateException {
		try {
			//noinspection unchecked
			return (Map<String, Class<?>>) PLUGIN_CLASS_MAP.get(javaPlugin.getClass().getClassLoader());
		} catch (ClassCastException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

}
