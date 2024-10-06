package com.github.sanctum.clans.model;

import com.github.sanctum.clans.impl.DefaultClaim;
import com.github.sanctum.clans.model.backend.ClaimFileBackend;
import com.github.sanctum.labyrinth.library.LabyrinthEncoded;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.Check;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulated data for a clan owned <strong>chunk</strong> of land.
 */
@Node.Pointer(value = "Claim", type = DefaultClaim.class)
@DelegateDeserialization(DefaultClaim.class)
public interface Claim extends Savable, Iterable<Block>, ConfigurationSerializable, JsonAdapter<Claim> {


	ClaimFileBackend ACTION = new ClaimFileBackend();


	/**
	 * Attempt converting a given player to a claim resident.
	 * <p>
	 * This will return null if the given player isn't within a claim.
	 *
	 * @param p The player to search for
	 * @return A resident object or null
	 */
	static Resident getResident(Player p) {
		return ClansAPI.getInstance().getClaimManager().getResidentManager().getResident(p);
	}

	/**
	 * Get the entity holder for this claim, if default implementation this method will indefinitely return the owning clan object.
	 *
	 * @return Gets the inherent owner of the claim if provided.
	 */
	EntityHolder getHolder();

	/**
	 * Get the id for this claim.
	 *
	 * @return Gets the claimID from the claim object
	 */
	String getId();

	/**
	 * Get who owns this claim.
	 * The average entity type for a claim owner is a clan but in special cases in the future
	 * could return a clan associate.
	 *
	 * @return Gets the owner of the claim object.
	 */
	InvasiveEntity getOwner();

	/**
	 * Get the exact chunk location for this claim.
	 *
	 * @return Gets the specific chunk of the claim location.
	 */
	Chunk getChunk();

	/**
	 * Get a flag registered to this claim.
	 *
	 * @param id The id of the flag to get
	 * @return the flag or null if not present.
	 */
	Flag getFlag(String id);

	/**
	 * Get an array of flags for this claim.
	 *
	 * @return the array of registered flags for this claim
	 */
	Flag[] getFlags();

	/**
	 * Get the claim's position list
	 * <p>
	 * [0] = chunkX, [1] = chunkZ
	 *
	 * @return The claims position list
	 */
	int[] getPos();

	/**
	 * Get the claim's id list
	 * <p>
	 * [0] = clanID , [1] = claimID, [2] = worldName
	 *
	 * @return The claims id list
	 */
	String[] getKey();

	/**
	 * Get the claims activity status.
	 *
	 * @return true if the claim is visible
	 */
	boolean isActive();

	/**
	 * @return true if this claim is owned by an associate directly instead of a clan.
	 */
	default boolean isPlayerOwned() {
		return getOwner().isAssociate();
	}

	/**
	 * Sets the claim's activity. Making this false will make the claim appear invisible.
	 *
	 * @param active new status for the claim
	 */
	void setActive(boolean active);

	/**
	 * Register flag(s) properties to this claim.
	 *
	 * @param flags The flag(s) to register.
	 */
	void register(Flag... flags);

	/**
	 * Remove a flag from this claim.
	 *
	 * @param flag The flag to remove.
	 */
	void remove(Flag flag);

	/**
	 * Get the chunk centered location for this claim.
	 *
	 * @return Gets the centered location of the claim objects chunk.
	 */
	Location getLocation();

	/**
	 * Get all residents for this claim.
	 *
	 * @return Gets a list of all known online residents within the claim.
	 */
	List<Resident> getResidents();

	/**
	 * Get a claim resident by player-name
	 *
	 * @param name The player name to search for
	 * @return A resident object for the given claim
	 */
	Resident getResident(String name);

	/**
	 * Completely delete/un-claim this clan land.
	 */
	@Override
	void remove();

	/**
	 * Save the claims information to its backing location.
	 */
	@Override
	void save();

	@Override
	default JsonElement write(Claim claim) {
		JsonObject o = new JsonObject();
		o.addProperty("id", claim.getId());
		o.addProperty("owner", claim.getOwner().getTag().getId());
		o.addProperty("x", claim.getChunk().getX());
		o.addProperty("z", claim.getChunk().getZ());
		o.addProperty("world", claim.getChunk().getWorld().getName());
		o.addProperty("active", claim.isActive());
		JsonObject flags = new JsonObject();
		for (Flag f : claim.getFlags()) {
			flags.addProperty(f.getId(), f.isEnabled());
		}
		o.add("flags", flags);
		return o;
	}

	@Override
	default Claim read(Map<String, Object> object) {
		String id = (String) object.get("id");
		String owner = (String) object.get("owner");
		Claim test = ClansAPI.getInstance().getClaimManager().getClaim(id);
		if (test != null) {
			return test;
		}
		Number x = (long) object.get("x");
		Number z = (long) object.get("z");
		String world = (String) object.get("world");
		boolean active = Boolean.valueOf((Boolean) object.get("active"));
		Claim claim = new DefaultClaim(x.intValue(), z.intValue(), owner, id, world, active);
		Map<String, Object> flags = (Map<String, Object>) object.get("flags");
		if (flags != null) {
			flags.forEach((flagid, o) -> {
				boolean b = Boolean.parseBoolean(o.toString());
				Claim.Flag loading = new Claim.Flag(flagid, true) {

					private static final long serialVersionUID = -5092886817202966276L;

					{
						setEnabled(b);
					}

					@Override
					public @NotNull String getId() {
						return id;
					}
				};
				claim.register(loading);
			});
		}
		return claim;
	}

	@Override
	default Class<Claim> getSerializationSignature() {
		return Claim.class;
	}

	@Override
	default @NotNull Map<String, Object> serialize() {
		Map<String, Object> o = new HashMap<>();
		o.put("id", getId());
		o.put("owner", getOwner().getTag().getId());
		o.put("x", getChunk().getX());
		o.put("z", getChunk().getZ());
		o.put("world", getChunk().getWorld().getName());
		o.put("active", isActive());
		Map<String, Object> flags = new HashMap<>();
		for (Flag f : getFlags()) {
			flags.put(f.getId(), f.isEnabled());
		}
		o.put("flags", flags);
		return o;
	}

	static Claim deserialize(Map<String, Object> object) {
		String id = (String) object.get("id");
		String owner = (String) object.get("owner");
		Claim test = ClansAPI.getInstance().getClaimManager().getClaim(id);
		if (test != null) {
			return test;
		}
		int x = (int) object.get("x");
		int z = (int) object.get("z");
		String world = (String) object.get("world");
		boolean active = Boolean.valueOf((Boolean) object.get("active"));
		Claim claim = new DefaultClaim(x, z, owner, id, world, active);
		Map<String, Object> flags = (Map<String, Object>) object.get("flags");
		if (flags != null) {
			flags.forEach((flagid, o) -> {
				boolean b = Boolean.parseBoolean(o.toString());
				Claim.Flag loading = new Claim.Flag(flagid, true) {

					private static final long serialVersionUID = -5092886817202966276L;

					{
						setEnabled(b);
					}

					@Override
					public @NotNull String getId() {
						return id;
					}
				};
				claim.register(loading);
			});
		}
		return claim;
	}

	/**
	 * A wrapping interface for an online player that is currently within a clan claim. Retaining information like time spent, joined, blocks manipulated and more.
	 */
	interface Resident {

		/**
		 * @return The player or resident within a clan land.
		 */
		@NotNull Player getPlayer();

		/**
		 * @return This resident's information regarding time stayed and more.
		 */
		@NotNull ResidentInformation getInfo();

	}

	abstract class Flag implements Comparable<Flag>, Cloneable, Serializable {

		private static final long serialVersionUID = 904348302141876668L;
		protected boolean loading;
		private boolean allowed;
		private String id;

		public Flag() {
		}

		public Flag(Flag otherFlag) {
			this.id = Check.forNull(otherFlag.getId(), "Flag id cannot be null in cloning process!");
			this.allowed = otherFlag.allowed;
			this.loading = otherFlag.loading;
		}

		public Flag(String id, boolean loading) {
			this.loading = loading;
			this.id = id;
		}

		public static Flag deserialize(String context) {
			return new LabyrinthEncoded(context).deserialize(Flag.class);
		}

		@NotNull
		public String getId() {
			return id;
		}

		public boolean isValid() {
			return !loading;
		}

		public boolean isEnabled() {
			return allowed;
		}

		public void setEnabled(boolean allowed) {
			this.allowed = allowed;
		}

		public final Flag updateCustom() {
			this.loading = false;
			return this;
		}

		public final @NotNull String serialize() {
			return new LabyrinthEncoded(this).serialize();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Flag)) return false;
			if (obj == this) return true;
			Flag f = (Flag) obj;
			return f.getId().equals(getId()) && isValid() == f.isValid();
		}

		@Override
		public Flag clone() throws CloneNotSupportedException {
			return new Flag(this) {
				private static final long serialVersionUID = 2214098531471796215L;
			};
		}

		@Override
		public int compareTo(@NotNull Claim.Flag o) {
			if (!isValid() && o.isValid()) {
				return -1;
			}
			return 0;
		}
	}

	interface Action<O> extends Runnable {

		O deploy();

		@Override
		default void run() {
			deploy();
		}

	}

}
