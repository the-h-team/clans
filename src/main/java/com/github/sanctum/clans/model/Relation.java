package com.github.sanctum.clans.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * A relational object, holding information for both allies and enemies as-well as incoming requests for alliances.
 * This object is commonly used as a delegate for relationships between entities.
 *
 * @see Clan
 * @see Clan.Associate
 */
public interface Relation extends Iterable<InvasiveEntity>, Relatable<Relation>, Comparable<Relation> {

	/**
	 * Get the allied entities of {@link Relation#getEntity()}
	 *
	 * @return this entities alliance information object.
	 */
	@NotNull Alliance getAlliance();

	/**
	 * Get the rivaled entities of {@link Relation#getEntity()}
	 *
	 * @return this entities enemy information object.
	 */
	@NotNull Rivalry getRivalry();

	/**
	 * @see Clan
	 * @see Clan.Associate
	 * @return the entity this relation belongs to.
	 */
	@NotNull InvasiveEntity getEntity();

	/**
	 * Check if a given entity is neutral standing with {@link Relation#getEntity()}
	 *
	 * @param target The target to check.
	 * @return true if the target entity is neither ally or enemy.
	 */
	boolean isNeutral(InvasiveEntity target);

	/**
	 * Compare this relational object to another, in doing so the benefactor is decided based off of which object contains a larger
	 * alliance.
	 *
	 * @param o The relation to compare.
	 * @return the greater of the two relations.
	 */
	@Override
	default int compareTo(@NotNull Relation o) {
		if ((getEntity().isClan() && o.getEntity().isClan()) || (getEntity().isAssociate() && o.getEntity().isAssociate()))
			return getEntity().compareTo(o.getEntity());
		int count = o.getAlliance().size();
		return Integer.compare(getAlliance().size(), count);
	}

	/**
	 * Relate this relation to that of another.
	 * The regex here follows the same concept as using {@link Clan#relate(Object)} for
	 * both a {@link Clan} & an {@link Clan.Associate}.
	 *
	 * @param relation the relation to compare.
	 * @return A visual relation representation.
	 */
	@Override
	default String relate(Relation relation) {
		if (getEntity().isClan() && relation.getEntity().isClan()) {
			return getEntity().getAsClan().relate(relation.getEntity().getAsClan());
		}
		if (getEntity().isAssociate() && relation.getEntity().isAssociate()) {
			return getEntity().getAsAssociate().getClan().relate(relation.getEntity().getAsAssociate().getClan());
		}
		return "NaN";
	}

	@NotNull
	@Override
	default Iterator<InvasiveEntity> iterator() {
		Set<InvasiveEntity> entities = new HashSet<>(getAlliance().get());
		entities.addAll(getRivalry().get());
		entities.addAll(getAlliance().getRequests());
		return entities.iterator();
	}

	@Override
	default void forEach(Consumer<? super InvasiveEntity> action) {
		Set<InvasiveEntity> entities = new HashSet<>(getAlliance().get());
		entities.addAll(getRivalry().get());
		entities.addAll(getAlliance().getRequests());
		entities.forEach(action);
	}

	@Override
	default Spliterator<InvasiveEntity> spliterator() {
		Set<InvasiveEntity> entities = new HashSet<>(getAlliance().get());
		entities.addAll(getRivalry().get());
		entities.addAll(getAlliance().getRequests());
		return entities.spliterator();
	}

	/**
	 * One side of a two-sided table, an object that acts as a container of rivaled entities.
	 * This object also acts as a collection whilst retaining singularity but also inherits from {@link Iterable}
	 *
	 * @see Relation
	 */
	interface Rivalry extends EntityHolder {

		@NotNull List<InvasiveEntity> get();

		@NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl);

		@NotNull
		@Override
		default Iterator<InvasiveEntity> iterator() {
			return get().iterator();
		}

		@Override
		default void forEach(Consumer<? super InvasiveEntity> action) {
			get().forEach(action);
		}

		@Override
		default Spliterator<InvasiveEntity> spliterator() {
			return get().spliterator();
		}

		@Override
		default Stream<InvasiveEntity> stream() {
			return get().stream();
		}

	}

	/**
	 * One side of a two-sided table, an object that acts as a container of allied entities.
	 * This object also acts as a collection whilst retaining singularity but also inherits from {@link Iterable}
	 * @see Relation
	 */
	interface Alliance extends EntityHolder {

		@NotNull List<InvasiveEntity> get();

		@NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl);

		void request(InvasiveEntity target);

		void request(InvasiveEntity target, String message);

		@NotNull List<InvasiveEntity> getRequests();

		@NotNull <T extends InvasiveEntity> List<T> getRequests(Class<T> cl);

		@NotNull
		@Override
		default Iterator<InvasiveEntity> iterator() {
			return get().iterator();
		}

		@Override
		default void forEach(Consumer<? super InvasiveEntity> action) {
			get().forEach(action);
		}

		@Override
		default Spliterator<InvasiveEntity> spliterator() {
			return get().spliterator();
		}

		@Override
		default Stream<InvasiveEntity> stream() {
			return get().stream();
		}

	}

}
