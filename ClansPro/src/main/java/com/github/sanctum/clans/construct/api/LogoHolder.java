package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SpecialCarrierAdapter;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
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
		if (DefaultClan.stands.get(location.getChunk().getX() + ";" + location.getChunk().getZ()) != null) {
			for (Carrier l : DefaultClan.stands.get(location.getChunk().getX() + ";" + location.getChunk().getZ())) {
				if (!getStands(l.getTop()).isEmpty()) {
					return l;
				}
			}
		}
		return InoperableSpecialMemory.ADAPTERS.stream().filter(adapter -> adapter.accept(location) != null).map(adapter -> adapter.accept(location)).findFirst().orElse(null);
	}

	static Set<ArmorStand> getStands(Location location) {
		Set<ArmorStand> stands = new HashSet<>();
		stands.addAll(getStands(location, 0.001, 22, 0.001));
		stands.addAll(getStands(location, 0.001, -22, 0.001));
		return stands;
	}

	static Set<ArmorStand> getStands(Location location, double x, double y, double z) {
		Set<ArmorStand> stands = new HashSet<>();
		location.getWorld().getNearbyEntities(location, x, y, z).stream().filter(entity -> entity instanceof ArmorStand).map(entity -> (ArmorStand) entity).forEach(stands::add);
		return stands;
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

		default void removeAndSave() {
			if (getHolder().isPersistent()) {
				Node holo = ((Clan) getHolder()).getNode("hologram");
				holo.set(null);
				Node world = holo.getNode(getChunk().getWorld().getName());
				Node c = world.getNode(getChunk().getX() + ";" + getChunk().getZ());
				Node stat = c.getNode(OrdinalProcedure.select(this, 0).cast(() -> HUID.class).toString());
				for (LogoHolder.Carrier.Line l : getLines()) {
					Node line = stat.getNode(String.valueOf(l.getIndex()));
					line.getNode("location").set(l.getStand().getLocation());
					line.getNode("content").set(l.getStand().getCustomName());
					l.destroy();
				}
				getStands(getTop()).forEach(Entity::remove);
				holo.save();
			}
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

			int getIndex();

			ArmorStand getStand();

			void destroy();

		}

	}

}
