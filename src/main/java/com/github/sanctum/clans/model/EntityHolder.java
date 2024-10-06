package com.github.sanctum.clans.model;

import com.github.sanctum.clans.util.TeleportationTarget;
import com.github.sanctum.panther.util.Check;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that describes an entity capable of containing nameable entities.
 * Nameable entities can be considered either a player, non player entity or even other clans themselves.
 *
 * @see Clan
 * @see Relation.Alliance
 * @see Relation.Rivalry
 */
public interface EntityHolder extends Iterable<InvasiveEntity> {

	/**
	 * Get a teleportation object from this holder by entity.
	 *
	 * @param entity The entity to retrieve.
	 * @return The teleportation object or null.
	 */
	@Nullable Teleport getTeleport(InvasiveEntity entity);

	/**
	 * Create a new teleportation instance for a given entity
	 *
	 * @param entity   the entity to teleport
	 * @param location the location to teleport to [player, location]
	 * @return a new teleportation object or null.
	 */
	default @Nullable Teleport newTeleport(InvasiveEntity entity, Object location) {
		TeleportationTarget target = new TeleportationTarget(location);
		if (target.isPlayer() || target.isLocation()) {
			return new Teleport.Impl(entity, target);
		}
		return null;
	}

	/**
	 * Check if this entity holder contains zero entities.
	 *
	 * @return true if this holder is empty.
	 */
	boolean isEmpty();

	/**
	 * Get all entities in this holder as an array.
	 *
	 * @return An array of all entities in this holder.
	 */
	InvasiveEntity[] toArray();

	/**
	 * Get all entities in this holder as an array of a specific type.
	 *
	 * @param a   The array arguments
	 * @param <T> The extension of InvasiveEntity
	 * @return An array of all entities in this holder.
	 */
	<T extends InvasiveEntity> T[] toArray(T[] a);

	/**
	 * Check if this holder contains a given entity.
	 *
	 * @param o   The entity to check for.
	 * @param <T> The type of entity.
	 * @return true if this holder contains the entity.
	 */
	<T extends InvasiveEntity> boolean has(T o);

	/**
	 * Check if this holder contains every entity from a given collection of invasive entities.
	 *
	 * @param c   The collection to compare
	 * @param <T> The type of entity.
	 * @return true if this holder contains all the entities
	 */
	<T extends InvasiveEntity> boolean hasAll(Collection<T> c);

	/**
	 * Try to add a collection of invasive entities to this holder
	 *
	 * @param c   The collection to add
	 * @param <T> The type of entities being added
	 * @return true if all entities have successfully been added to this holder.
	 */
	<T extends InvasiveEntity> boolean addAll(Collection<T> c);

	/**
	 * Try to remove all associated entities from a given collection from this holder.
	 *
	 * @param c   The collection to remove
	 * @param <T> The type of entities being removed
	 * @return true if all entities have successfully been removed from this holder.
	 */
	<T extends InvasiveEntity> boolean removeAll(Collection<T> c);

	/**
	 * Get an entity from this holder by index.
	 *
	 * @param index The index to pick
	 * @return the indexed entity from this holder or null
	 * @throws IndexOutOfBoundsException if the target index extends beyond or below the size of this holder.
	 */
	default InvasiveEntity index(int index) throws IndexOutOfBoundsException {
		return toArray()[Math.min(size(), index)];
	}

	/**
	 * Remove a given entity from this holder if they meet the required predicate.
	 *
	 * @param filter The predicate to assign
	 * @return true if the entity was removed.
	 */
	default boolean removeIf(@NotNull Predicate<? super InvasiveEntity> filter) {
		Check.forNull(filter, "Illegal not null bypass; Removal filters cannot be null");
		boolean removed = false;
		for (InvasiveEntity e : this) {
			if (filter.test(e)) {
				remove(e);
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Get a stream wrapping this entity collection to manipulate or condense with.
	 *
	 * @return the stream of entities this holder contains.
	 */
	default Stream<InvasiveEntity> stream() {
		return Arrays.stream(toArray());
	}

	/**
	 * Add an entity to this holder if it doesn't already exist.
	 *
	 * @param entity the entity to add
	 * @return true if the entity was added.
	 */
	boolean add(InvasiveEntity entity);

	/**
	 * Remove an entity from this holder if it exists.
	 *
	 * @param entity the entity to remove
	 * @return true if the entity was removed.
	 */
	boolean remove(InvasiveEntity entity);

	/**
	 * @return the size of this entity holder.
	 */
	int size();

	/**
	 * Clear all entities from this holder.
	 */
	void clear();

	@NotNull
	@Override
	Iterator<InvasiveEntity> iterator();

	@Override
	void forEach(Consumer<? super InvasiveEntity> action);

	@Override
	Spliterator<InvasiveEntity> spliterator();
}
