package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.data.service.Check;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that describes an entity capable of containing nameable entities.
 * Nameable entities can be considered either a player, non player entity or even other clans themselves.
 *
 * @see Clan
 * @see Relation.Alliance
 * @see Relation.Rivalry
 */
public interface EntityHolder extends Iterable<InvasiveEntity> {

	Teleport getTeleport(InvasiveEntity entity);

	boolean isEmpty();

	InvasiveEntity[] toArray();

	<T extends InvasiveEntity> T[] toArray(T[] a);

	<T extends InvasiveEntity> boolean has(T o);

	<T extends InvasiveEntity> boolean hasAll(Collection<T> c);

	<T extends InvasiveEntity> boolean addAll(Collection<T> c);

	<T extends InvasiveEntity> boolean removeAll(Collection<T> c);

	default InvasiveEntity index(int index) {
		return toArray()[Math.min(size(), index)];
	}

	default boolean removeIf(@NotNull Predicate<? super InvasiveEntity> filter) {
		Check.forNull(filter, "Illegal not null bypass; Removal filters cannot be null");
		boolean removed = false;
		final Iterator<? extends InvasiveEntity> each = iterator();
		while (each.hasNext()) {
			InvasiveEntity e = each.next();
			if (filter.test(e)) {
				each.remove();
				removed = true;
			}
		}
		return removed;
	}

	void add(InvasiveEntity entity);

	void remove(InvasiveEntity entity);

	int size();

	void clear();

	@NotNull
	@Override
	Iterator<InvasiveEntity> iterator();

	@Override
	void forEach(Consumer<? super InvasiveEntity> action);

	@Override
	Spliterator<InvasiveEntity> spliterator();
}
