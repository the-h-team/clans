package com.github.sanctum.clans.construct.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated To be replaced by {@link ClearanceLog}
 */
@Deprecated
public class PermissionLog implements Iterable<Map.Entry<Permission, Integer>>, Serializable {
	private static final long serialVersionUID = -3379504326801396918L;

	private final Map<Permission, Integer> MAP = new HashMap<>();

	public PermissionLog() {
		for (Permission p : Permission.values()) {
			get(p);
		}
	}

	public void set(Permission perm, int level) {
		MAP.put(perm, level);
	}

	public int get(Permission permission) {
		return MAP.computeIfAbsent(permission, Permission::getDefault);
	}

	public Map<Permission, Integer> getMap() {
		return MAP;
	}

	@NotNull
	@Override
	public Iterator<Map.Entry<Permission, Integer>> iterator() {
		return getMap().entrySet().iterator();
	}

	@Override
	public void forEach(Consumer<? super Map.Entry<Permission, Integer>> action) {
		getMap().entrySet().forEach(action);
	}

	@Override
	public Spliterator<Map.Entry<Permission, Integer>> spliterator() {
		return getMap().entrySet().spliterator();
	}
}
