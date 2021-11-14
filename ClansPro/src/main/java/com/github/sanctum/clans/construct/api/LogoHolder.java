package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SpecialCarrierAdapter;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;

/**
 * Symbolises an entity that has its own logo and logo carriers.
 * A logo is just a collection of strings at base that when graphed together form a visual art piece.
 * A logo carrier is essentially a hologram within itself, its a container for individual armor stand
 * entities or otherwise called {@link Carrier.Line}'s
 *
 * @see Clan
 * @see Clan.Associate
 * @see InvasiveEntity
 */
public interface LogoHolder extends Savable {

	static Carrier getCarrier(Location location) {
		for (Clan c : ClansAPI.getInstance().getClanManager().getClans()) {
			for (Carrier l : c.getCarriers()) {
				for (Carrier.Line line : l.getLines()) {
					for (int i = 1; i < 2; i++) {
						Block up = location.getBlock().getRelative(BlockFace.UP, i);
						if (line.getStand().getLocation().distanceSquared(up.getLocation().add(0.5, 0, 0.5)) <= 1) {
							return l;
						}
					}
				}
			}
		}
		return InoperableSpecialMemory.ADAPTERS.stream().filter(adapter -> adapter.accept(location) != null).map(adapter -> adapter.accept(location)).findFirst().orElse(null);
	}

	static Deployable<Void> newAdapter(@NotNull("Carrier extension's cannot be null!") SpecialCarrierAdapter adapter) {
		return Deployable.of(null, unused -> InoperableSpecialMemory.ADAPTERS.add(adapter));
	}

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

	List<Carrier> getCarriers();

	List<Carrier> getCarriers(Chunk chunk);

	Carrier newCarrier(Location location);

	/**
	 * Save all carrier information to it's backing location.
	 */
	@Note("This method may be useless for some implementations.")
	@Override
	void save();

	void remove(Carrier carrier);

	/**
	 * Remove all carrier information from file.
	 */
	@Override
	void remove();

	default boolean isInvasive() {
		return this instanceof InvasiveEntity;
	}

	default boolean isPersistent() {
		return this instanceof PersistentEntity;
	}

	/**
	 * Im the object that keeps track of a singular "hologram" post.
	 */
	interface Carrier {

		void add(@Note("Armor stands go here.") Attributable attributable) throws IllegalArgumentException;

		Location getTop();

		Chunk getChunk();

		LogoHolder getHolder();

		String getId();

		Set<Line> getLines();

		default Location getBottom() {
			return getLines().toArray(new Line[0])[getLines().size()].getStand().getLocation();
		}

		default void remove() {
			getLines().forEach(l -> l.getStand().remove());
		}

		/**
		 * I keep track of individual logo lines. A "logo line" is a singular string from a collection.
		 */
		interface Line {

			default String getText() {
				return getStand().getCustomName();
			}

			default void setText(String text) {
				getStand().setCustomName(text);
			}

			HUID getId();

			ArmorStand getStand();

			void destroy();

		}

	}

}
