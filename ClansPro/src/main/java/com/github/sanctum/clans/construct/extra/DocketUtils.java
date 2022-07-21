package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;

public class DocketUtils {

	static final PantherMap<String, MemoryDocket<?>> dockets = new PantherEntryMap<>();

	public static <V> MemoryDocket<V> get(String id) {
		Object o = dockets.get(id);
		if (o != null) return (MemoryDocket<V>) o;
		return null;
	}

	public static void load(String id, MemoryDocket<?> docket) {
		dockets.put(id, docket);
	}

}
