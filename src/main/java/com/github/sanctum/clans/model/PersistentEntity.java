package com.github.sanctum.clans.model;

import com.github.sanctum.panther.file.MemorySpace;
import com.github.sanctum.panther.file.Node;
import java.util.Map;
import java.util.Set;

/**
 * <p>An object describing an entity with a persistent background,
 * custom values can be keyed directly to this memory space.</p>
 *
 * @see com.github.sanctum.clans.model.Clan
 * @see com.github.sanctum.clans.model.Clan.Associate
 */
public interface PersistentEntity extends MemorySpace {

	/**
	 * Check if a specific key belongs to another memory space.
	 * If no memory space belongs to the target path and something like an primitive value lies
	 * their this will return false.
	 *
	 * @param key The key to use
	 * @return true if the key is an existing memory space false if its not or if its a common value
	 */
	@Override
	boolean isNode(String key);

	/**
	 * Get a node from this memory space, default implementaions imply that if
	 * a node is not valid the object can be any way for future creation use. Therefore
	 * increasing the fluidity of your workflow when using this api.
	 *
	 * A node is in simple terms just another memory space containing keyed values.
	 *
	 * @param key The key to use
	 * @return an existing node if found or a new one.
	 */
	@Override
	Node getNode(String key);

	/**
	 * Get all the keys known to this persistent memory space.
	 * If deep is selected true this method will search through multiple
	 * memory spaces.
	 *
	 * @param deep how deep to search
	 * @return a set of keys.
	 */
	@Override
	Set<String> getKeys(boolean deep);

	/**
	 * Get all the values known to this persistent memory space.
	 * If deep is selected true this method will search through multiple
	 * memory spaces.
	 *
	 * @param deep how deep to search
	 * @return a map of keyed values.
	 */
	@Override
	Map<String, Object> getValues(boolean deep);

	/**
	 * @return the key path for this memory space in configuration.
	 */
	@Override
	String getPath();

}
