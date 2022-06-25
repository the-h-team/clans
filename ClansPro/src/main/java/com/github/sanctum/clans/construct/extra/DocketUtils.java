package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.labyrinth.data.container.LabyrinthEntryMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthMap;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;

public class DocketUtils {

	static final LabyrinthMap<String, MemoryDocket<?>> dockets = new LabyrinthEntryMap<>();

	public static <V> MemoryDocket<V> get(String id) {
		Object o = dockets.get(id);
		if (o != null) return (MemoryDocket<V>) o;
		return null;
	}

	public static void load(String id, MemoryDocket<?> docket) {
		dockets.put(id, docket);
	}

}
