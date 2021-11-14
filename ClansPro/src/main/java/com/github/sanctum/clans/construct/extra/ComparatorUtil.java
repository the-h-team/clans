package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.api.LogoHolder;
import java.util.Comparator;

/**
 * A utility for grouping together all known comparator's in the clans plugin.
 */
public final class ComparatorUtil {

	/**
	 * Compare entities by their logo's
	 * <p>
	 * Works as if both entities are a clan.
	 *
	 * @param <V> The logo holder
	 * @return a logo comparison.
	 */
	public static <V extends LogoHolder> Comparator<V> comparingByLogo() {
		return LogoHolder.comparingByLogo();
	}

	/**
	 * Compare entities by normal in house circumstances.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	public static <V extends InvasiveEntity> Comparator<V> comparingByEntity() {
		return InvasiveEntity.comparingByEntity();
	}

	/**
	 * Compare entities by their clan power.
	 * <p>
	 * Works as if both entities are a clan, both are an associate or one is an associate and the other is a clan.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	public static <V extends InvasiveEntity> Comparator<V> comparingByPower() {
		return InvasiveEntity.comparingByPower();
	}

	/**
	 * Compare entities by their clan banks.
	 * <p>
	 * Works as if both entities are a clan, both are an associate or one is an associate and the other is a clan.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	public static <V extends InvasiveEntity> Comparator<V> comparingByMoney() {
		return InvasiveEntity.comparingByMoney();
	}

	/**
	 * Compare entities by their relationship.
	 * <p>
	 * Works as if both entities are a clan, both are an associate or one is an associate and the other is a clan.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	public static <V extends InvasiveEntity> Comparator<V> comparingByRelation() {
		return InvasiveEntity.comparingByRelation();
	}

}
