package com.github.sanctum.clans.model;

/**
 * An object that has a savable state, underlying information gets written to whatever objective it may have.
 */
public interface Savable {

	/**
	 * Saves the information under this object space.
	 */
	void save();

	/**
	 * Removes the information under this object space.
	 */
	void remove();

}
