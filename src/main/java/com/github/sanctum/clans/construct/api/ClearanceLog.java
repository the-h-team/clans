package com.github.sanctum.clans.construct.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

public class ClearanceLog implements Iterable<Map.Entry<Clearance, Integer>>, Serializable {

	private static final long serialVersionUID = -1949270600946338973L;
	private final Map<Clearance, Integer> MAP = new HashMap<>();

	public ClearanceLog() {
		for (Clearance p : Clearance.values()) {
			get(p);
		}
	}

	public void set(Clearance clearance, @MagicConstant(valuesFromClass = Clearance.Level.class) int level) {
		if (level == Clearance.Level.EMPTY) {
			if (Clearance.valueOf(clearance.getName()) != null) return;
			MAP.remove(clearance);
			return;
		}
		Clearance temp = null;
		for (Map.Entry<Clearance, Integer> entry: this) {
			if (entry.getKey().equals(clearance)) {
				temp = entry.getKey();
				break;
			}
		}
		if (temp != null) {
			MAP.put(temp, level);
		} else {
			MAP.put(clearance, level);
		}
	}

	public int get(Clearance clearance) {
		int level = 0;
		Clearance temp = null;
		for (Map.Entry<Clearance, Integer> entry: this) {
			if (entry.getKey().equals(clearance)) {
				temp = entry.getKey();
				level = entry.getValue();
				break;
			}
		}
		if (temp == null) {
			MAP.put(clearance, clearance.getDefault());
			return clearance.getDefault();
		}
		return level;
	}

	@NotNull
	@Override
	public Iterator<Map.Entry<Clearance, Integer>> iterator() {
		return MAP.entrySet().iterator();
	}

	@Override
	public void forEach(Consumer<? super Map.Entry<Clearance, Integer>> action) {
		MAP.entrySet().forEach(action);
	}

	@Override
	public Spliterator<Map.Entry<Clearance, Integer>> spliterator() {
		return MAP.entrySet().spliterator();
	}

	public Stream<Map.Entry<Clearance, Integer>> stream() {
		return Collections.unmodifiableMap(MAP).entrySet().stream();
	}
}
