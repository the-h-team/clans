package com.github.sanctum.clans.construct.api;

import java.util.Comparator;

/**
 * An interface for visualizing relations between two objects.
 * <p>
 * The string result can be anything but is normally that of a color code.
 * This object doesn't inherit from {@link Comparable} but has the same properties to reduce child inheritance problems.
 *
 * @param <T> The object to relate.
 */
public interface Relatable<T> {

	String relate(T t);

	default int relateTo(T t) {
		String test = relate(t);
		if (test.contains("&c") || test.contains("&4")) {
			return 1;
		}
		if (test.contains("&a")) {
			return 2;
		}
		return 0;
	}

	/**
	 * Compare relatable object's to each-other by their relationship.
	 *
	 * @param <V> The relation type.
	 * @return A comparator for relations.
	 */
	static <V extends Relatable<V>> Comparator<V> comparingByRelation() {
		return Relatable::relateTo;
	}

}
