package com.github.sanctum.clans.model;

import com.github.sanctum.labyrinth.data.service.Constant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * An object containing either zero or multiple value types in response to a processed inquiry.
 *
 * @see Consultant#sendMessage(Supplier)
 */
public final class Ticket {

	private final Map<Integer, Object> map = new HashMap<>();

	/**
	 * Add an object to this ticket response.
	 *
	 * @param key The type of object.
	 * @param o The object.
	 */
	public void setType(@MagicConstant(valuesFromClass = Field.class) int key, @NotNull Object o) {
		validate(key);
		map.put(key, o);
	}

	/**
	 * Get an object from this response by its value type.
	 *
	 * @param key The type of object.
	 * @return The object or null.
	 */
	public Object get(@MagicConstant(valuesFromClass = Field.class) int key) {
		validate(key);
		return map.get(key);
	}

	/**
	 * Get an object from this response by its value type and cast it.
	 *
	 * @param key The type of object.
	 * @param cl The class to use for casting.
	 * @param <T> The raw object type.
	 * @return The casted object or null.
	 */
	public <T> T get(@MagicConstant(valuesFromClass = Field.class) int key, Class<T> cl) {
		validate(key);
		return cl.cast(map.get(key));
	}

	/**
	 * Check if this response contains a value type.
	 *
	 * @param key The type of object.
	 * @return The casted object or null.
	 */
	public boolean has(@MagicConstant(valuesFromClass = Field.class) int key) {
		return get(key) != null;
	}

	/**
	 * Check if this response contains a value type by class.
	 *
	 * @param key The type of object.
	 * @param <T> The raw object type.
	 * @param cl The class to use for casting.
	 * @return The casted object or null.
	 */
	public <T> boolean has(@MagicConstant(valuesFromClass = Field.class) int key, Class<T> cl) {
		return has(key) && cl.isAssignableFrom(get(key).getClass());
	}

	void validate(int num) {
		if (!Constant.values(Field.class, Integer.class).contains(num)) throw new IllegalArgumentException("No memory space exists for unknown object key " + num);
	}

	/**
	 * @return true if this ticket response is empty and contains no information.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	public static final class Field {


		/**
		 * Represents a string value
		 */
		public static final int STRING = 1;
		/**
		 * Represents a number value
		 */
		public static final int NUMBER = 2;
		/**
		 * Represents a boolean value
		 */
		public static final int BOOLEAN = 3;
		/**
		 * Is a custom object needing parsed.
		 */
		public static final int CUSTOM = 4;
	}

}
