package com.github.sanctum.clans.construct.api;

/**
 * An interface for visualizing relations between two objects.
 * <p>
 * The string result can be anything but is normally that of a color code.
 *
 * @param <T> The object to relate.
 */
public interface Relatable<T> {

	String relate(T t);

}
