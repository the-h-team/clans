package com.github.sanctum.clans.model;

import com.github.sanctum.panther.annotation.Note;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Symbolises an entity that has its own logo.
 * A logo is just a collection of strings at base that when graphed together form a visual art piece.
 *
 * @see Clan
 * @see Clan.Associate
 * @see InvasiveEntity
 */
public interface LogoHolder {

	/**
	 * Compare entities by their logo's
	 *
	 * Works as if both entities are a clan.
	 *
	 * @param <V> The logo holder
	 * @return a logo comparison.
	 */
	static <V extends LogoHolder> Comparator<V> comparingByLogo() {
		return (o1, o2) -> {
			if (o1.getLogo() == null) {
				return -1;
			}
			if (o2.getLogo() == null) {
				return 1;
			}
			if (Arrays.deepEquals(o1.getLogo().toArray(new String[0]), o2.getLogo().toArray(new String[0]))) {
				return 0;
			}
			if (o1.getLogo().size() >= o2.getLogo().size()) {
				return 1;
			}
			if (o1.getLogo().size() < o2.getLogo().size()) {
				return -1;
			}
			return 0;
		};
	}

	List<String> getLogo();

	/**
	 * Save all holder information to it's backing location.
	 */
	@Note("This method may be useless for some implementations.")
	void save();

	/**
	 * Remove all holder information from file.
	 */
	void remove();

	default boolean isInvasive() {
		return this instanceof InvasiveEntity;
	}

}
