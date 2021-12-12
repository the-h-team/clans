package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.impl.AnimalAssociate;
import com.github.sanctum.clans.construct.impl.ServerAssociate;
import com.github.sanctum.labyrinth.annotation.Experimental;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>An object describing an entity that has an ability to control chunks of land.
 * The use of this interface insinuates that the underlying "entity" has free will and
 * therefore requires relations due to spontaneous involvements.</p>
 *
 * @see Clan
 * @see Clan.Associate
 */
public interface InvasiveEntity extends Nameable, LogoHolder, Comparable<InvasiveEntity> {

	@Experimental(dueTo = "Utility method for retaining cached entity references.")
	static @NotNull InvasiveEntity wrapNonAssociated(@NotNull ServerOperator entity) {
		if (entity instanceof OfflinePlayer) {
			if (ClansAPI.getInstance().getAssociate((OfflinePlayer) entity).isPresent()) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Unusual retrieval of associate object. Expected ClansAPI#getAssociate(OfflinePlayer)");
				return ClansAPI.getInstance().getAssociate((OfflinePlayer) entity).get();
			}
		}
		String id = null;
		String n = null;
		if (entity instanceof OfflinePlayer) {
			id = ((OfflinePlayer)entity).getUniqueId().toString();
			n = ((OfflinePlayer)entity).getName();
		}
		if (entity instanceof Entity) {
			id = ((Entity)entity).getUniqueId().toString();
			n = ((Entity)entity).getName();
		}
		if (entity instanceof CommandSender) {
			if (id == null) {
				id = ClansAPI.getInstance().getSessionId().toString();
				n = Bukkit.getName();
			}
		}
		String finalN = n;
		return InoperableSpecialMemory.ENTITY_MAP.computeIfAbsent(id, i -> new InvasiveEntity() {

			@Override
			public List<String> getLogo() {
				return null;
			}

			@Override
			public List<Carrier> getCarriers() {
				return null;
			}

			@Override
			public List<Carrier> getCarriers(Chunk chunk) {
				return null;
			}

			@Override
			public Carrier newCarrier(Location location) {
				return null;
			}

			@Override
			public void save() {

			}

			@Override
			public void remove() {
				InvasiveEntity.removeNonAssociated(this, true);
			}

			@Override
			public void remove(Carrier carrier) {

			}

			private final Tag tag;
			private final String name;
			private final Relation relations;

			{
				this.relations = new Relation() {

					private final Alliance s = new Alliance() {
						@Override
						public @NotNull List<InvasiveEntity> get() {
							return null;
						}

						@Override
						public @NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl) {
							return null;
						}

						@Override
						public void request(InvasiveEntity target) {

						}

						@Override
						public void request(InvasiveEntity target, String message) {

						}

						@Override
						public @NotNull List<InvasiveEntity> getRequests() {
							return null;
						}

						@Override
						public @NotNull <T extends InvasiveEntity> List<T> getRequests(Class<T> cl) {
							return null;
						}

						@Override
						public Teleport getTeleport(InvasiveEntity entity) {
							return null;
						}

						@Override
						public boolean isEmpty() {
							return false;
						}

						@Override
						public InvasiveEntity[] toArray() {
							return new InvasiveEntity[0];
						}

						@Override
						public <T extends InvasiveEntity> T[] toArray(T[] a) {
							return null;
						}

						@Override
						public <T extends InvasiveEntity> boolean has(T o) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
							return false;
						}

						@Override
						public boolean add(InvasiveEntity entity) {
							return false;
						}

						@Override
						public boolean remove(InvasiveEntity entity) {
							return false;
						}

						@Override
						public int size() {
							return 0;
						}

						@Override
						public void clear() {

						}
					};
					private final Rivalry r = new Rivalry() {
						@Override
						public @NotNull List<InvasiveEntity> get() {
							return null;
						}

						@Override
						public @NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl) {
							return null;
						}

						@Override
						public Teleport getTeleport(InvasiveEntity entity) {
							return null;
						}

						@Override
						public boolean isEmpty() {
							return false;
						}

						@Override
						public InvasiveEntity[] toArray() {
							return new InvasiveEntity[0];
						}

						@Override
						public <T extends InvasiveEntity> T[] toArray(T[] a) {
							return null;
						}

						@Override
						public <T extends InvasiveEntity> boolean has(T o) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
							return false;
						}

						@Override
						public boolean add(InvasiveEntity entity) {
							return false;
						}

						@Override
						public boolean remove(InvasiveEntity entity) {
							return false;
						}

						@Override
						public int size() {
							return 0;
						}

						@Override
						public void clear() {

						}
					};

					@Override
					public @NotNull Alliance getAlliance() {
						return s;
					}

					@Override
					public @NotNull Rivalry getRivalry() {
						return r;
					}

					@Override
					public @NotNull InvasiveEntity getEntity() {
						return getEnt();
					}

					@Override
					public boolean isNeutral(InvasiveEntity target) {
						return false;
					}
				};
				this.name = finalN;
				this.tag = () -> i;
			}

			InvasiveEntity getEnt() {
				return this;
			}

			@Override
			public @NotNull Tag getTag() {
				return tag;
			}

			@Override
			public @NotNull Claim[] getClaims() {
				return new Claim[0];
			}

			@Override
			public @Nullable Claim newClaim(Chunk c) {
				return null;
			}

			@Override
			public @NotNull Relation getRelation() {
				return relations;
			}

			@Override
			public @Nullable Teleport getTeleport() {
				return null;
			}

			@Override
			public int getClaimLimit() {
				return 0;
			}

			@Override
			public boolean isValid() {
				return false;
			}

			@Override
			public @NotNull String getName() {
				return name;
			}
		});
	}

	@Experimental(dueTo = "Utility method for retaining cached entity references.")
	static @NotNull InvasiveEntity wrapNonAssociated(@NotNull ServerOperator entity, String displayName) {
		if (entity instanceof OfflinePlayer) {
			if (ClansAPI.getInstance().getAssociate((OfflinePlayer) entity).isPresent()) {
				ClansAPI.getInstance().getPlugin().getLogger().warning("- Unusual retrieval of associate object. Expected ClansAPI#getAssociate(OfflinePlayer)");
				return ClansAPI.getInstance().getAssociate((OfflinePlayer) entity).get();
			}
		}
		String id = null;
		if (entity instanceof OfflinePlayer) {
			id = ((OfflinePlayer)entity).getUniqueId().toString();
		}
		if (entity instanceof Entity) {
			id = ((Entity)entity).getUniqueId().toString();
		}
		if (entity instanceof CommandSender) {
			if (id == null) {
				id = ClansAPI.getInstance().getSessionId().toString();
			}
		}
		return InoperableSpecialMemory.ENTITY_MAP.computeIfAbsent(id, i -> new InvasiveEntity() {

			@Override
			public List<String> getLogo() {
				return null;
			}

			@Override
			public List<Carrier> getCarriers() {
				return null;
			}

			@Override
			public List<Carrier> getCarriers(Chunk chunk) {
				return null;
			}

			@Override
			public Carrier newCarrier(Location location) {
				return null;
			}

			@Override
			public void save() {

			}

			@Override
			public void remove() {
				InvasiveEntity.removeNonAssociated(this, true);
			}

			@Override
			public void remove(Carrier carrier) {

			}

			private final Tag tag;
			private final String name;
			private final Relation relations;

			{
				this.relations = new Relation() {

					private final Alliance s = new Alliance() {
						@Override
						public @NotNull List<InvasiveEntity> get() {
							return null;
						}

						@Override
						public @NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl) {
							return null;
						}

						@Override
						public void request(InvasiveEntity target) {

						}

						@Override
						public void request(InvasiveEntity target, String message) {

						}

						@Override
						public @NotNull List<InvasiveEntity> getRequests() {
							return null;
						}

						@Override
						public @NotNull <T extends InvasiveEntity> List<T> getRequests(Class<T> cl) {
							return null;
						}

						@Override
						public Teleport getTeleport(InvasiveEntity entity) {
							return null;
						}

						@Override
						public boolean isEmpty() {
							return false;
						}

						@Override
						public InvasiveEntity[] toArray() {
							return new InvasiveEntity[0];
						}

						@Override
						public <T extends InvasiveEntity> T[] toArray(T[] a) {
							return null;
						}

						@Override
						public <T extends InvasiveEntity> boolean has(T o) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
							return false;
						}

						@Override
						public boolean add(InvasiveEntity entity) {
							return false;
						}

						@Override
						public boolean remove(InvasiveEntity entity) {
							return false;
						}

						@Override
						public int size() {
							return 0;
						}

						@Override
						public void clear() {

						}
					};
					private final Rivalry r = new Rivalry() {
						@Override
						public @NotNull List<InvasiveEntity> get() {
							return null;
						}

						@Override
						public @NotNull <T extends InvasiveEntity> List<T> get(Class<T> cl) {
							return null;
						}

						@Override
						public Teleport getTeleport(InvasiveEntity entity) {
							return null;
						}

						@Override
						public boolean isEmpty() {
							return false;
						}

						@Override
						public InvasiveEntity[] toArray() {
							return new InvasiveEntity[0];
						}

						@Override
						public <T extends InvasiveEntity> T[] toArray(T[] a) {
							return null;
						}

						@Override
						public <T extends InvasiveEntity> boolean has(T o) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean hasAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean addAll(Collection<T> c) {
							return false;
						}

						@Override
						public <T extends InvasiveEntity> boolean removeAll(Collection<T> c) {
							return false;
						}

						@Override
						public boolean add(InvasiveEntity entity) {
							return false;
						}

						@Override
						public boolean remove(InvasiveEntity entity) {
							return false;
						}

						@Override
						public int size() {
							return 0;
						}

						@Override
						public void clear() {

						}
					};

					@Override
					public @NotNull Alliance getAlliance() {
						return s;
					}

					@Override
					public @NotNull Rivalry getRivalry() {
						return r;
					}

					@Override
					public @NotNull InvasiveEntity getEntity() {
						return getEnt();
					}

					@Override
					public boolean isNeutral(InvasiveEntity target) {
						return false;
					}
				};
				this.name = displayName;
				this.tag = () -> i;
			}

			InvasiveEntity getEnt() {
				return this;
			}

			@Override
			public @NotNull Tag getTag() {
				return tag;
			}

			@Override
			public @NotNull Claim[] getClaims() {
				return new Claim[0];
			}

			@Override
			public @Nullable Claim newClaim(Chunk c) {
				return null;
			}

			@Override
			public @NotNull Relation getRelation() {
				return relations;
			}

			@Override
			public @Nullable Teleport getTeleport() {
				return null;
			}

			@Override
			public int getClaimLimit() {
				return 0;
			}

			@Override
			public boolean isValid() {
				return false;
			}

			@Override
			public @NotNull String getName() {
				return name;
			}
		});
	}

	static <T extends InvasiveEntity> void registerNonAssociated(T entity) {
		InoperableSpecialMemory.ENTITY_MAP.put(entity.getTag().getId(), entity);
	}

	static <T extends InvasiveEntity> void removeNonAssociated(T entity, boolean kill) {
		if (kill) {
			if (entity.isEntity()) {
				if (entity.getAsEntity().isValid()) {
					entity.getAsEntity().remove();
				}
			}
		}
		InoperableSpecialMemory.ENTITY_MAP.remove(entity.getTag().getId());
	}

	/**
	 * Compare entities by normal in house circumstances.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	static <V extends InvasiveEntity> Comparator<V> comparingByEntity() {
		return InvasiveEntity::compareTo;
	}

	/**
	 * Compare entities by their clan power.
	 * <p>
	 * Works as if both entities are a clan, both are an associate or one is an associate and the other is a clan.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	static <V extends InvasiveEntity> Comparator<V> comparingByPower() {
		return (o1, o2) -> {
			if (o1.isClan()) {
				if (o2.isClan()) {
					return Double.compare(o1.getAsClan().getPower(), o2.getAsClan().getPower());
				}
				return Double.compare(o1.getAsClan().getPower(), o2.getAsAssociate().getClan().getPower());
			}
			return Double.compare(o1.getAsAssociate().getClan().getPower(), o2.getAsAssociate().getClan().getPower());
		};
	}

	/**
	 * Compare entities by their clan banks.
	 * <p>
	 * Works as if both entities are a clan, both are an associate or one is an associate and the other is a clan.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	static <V extends InvasiveEntity> Comparator<V> comparingByMoney() {
		return (o1, o2) -> {
			if (o1.isClan()) {
				if (o2.isClan()) {
					return Double.compare(o1.getAsClan().getBalanceDouble(), o2.getAsClan().getBalanceDouble());
				}
				return Double.compare(o1.getAsClan().getBalanceDouble(), o2.getAsAssociate().getClan().getBalanceDouble());
			}
			if (EconomyProvision.getInstance().isValid()) {
				return Double.compare(EconomyProvision.getInstance().balance(o1.getAsAssociate().getUser().toBukkit()).orElse(0.0), EconomyProvision.getInstance().balance(o2.getAsAssociate().getUser().toBukkit()).orElse(0.0));
			} else
			return Double.compare(o1.getAsAssociate().getClan().getBalanceDouble(), o2.getAsAssociate().getClan().getBalanceDouble());
		};
	}

	/**
	 * Compare entities by their relationship.
	 * <p>
	 * Works as if both entities are a clan, both are an associate or one is an associate and the other is a clan.
	 *
	 * @param <V> The entity type.
	 * @return A comparator for invasive entities.
	 */
	static <V extends InvasiveEntity> Comparator<V> comparingByRelation() {
		return (o1, o2) -> {
			if (o1.isClan()) {
				if (o2.isClan()) {
					return o1.getAsClan().getRelation().compareTo(o2.getAsClan().getRelation());
				}
				return o1.getAsClan().getRelation().compareTo(o2.getAsAssociate().getRelation());
			}
			return o1.getAsAssociate().getRelation().compareTo(o2.getAsAssociate().getRelation());
		};
	}

	/**
	 * Get the tag for this entity.
	 *
	 * @return this entity's unique tag.
	 */
	@NotNull Tag getTag();


	/**
	 * Get the full list of owned claims for this entity.
	 *
	 * @return An array of claims
	 */
	@NotNull Claim[] getClaims();

	/**
	 * Claim the target chunk for this entity if possible.
	 *
	 * @param c The target chunk
	 * @return The newly claimed chunk or null if unable to claim.
	 */
	@Nullable Claim newClaim(Chunk c);

	/**
	 * Get the alliance & rivalry information regarding this entities public status.
	 *
	 * @return The relationships this entity holds.
	 */
	@NotNull Relation getRelation();

	/**
	 * Get the teleportation for this entity.
	 *
	 * @return The teleportation object or null
	 */
	@Nullable Teleport getTeleport();

	/**
	 * Get the optional memory space for this entity's persistent data.
	 *
	 * @return An optional memory space for this entity.
	 */
	@NotNull default Optional<MemorySpace> getMemorySpace() {
		return Optional.empty();
	}

	/**
	 * Create a brand-new teleportation for this entity.
	 *
	 * @param location The location to teleport [player, location]
	 * @return A new teleportation or null
	 */
	default @Nullable Teleport newTeleport(Object location) {
		if (getTeleport() != null) return null;
		if (isAssociate()) {
			return getAsAssociate().getClan().newTeleport(this, location);
		}
		if (isClan()) {
			return getAsClan().newTeleport(this, location);
		}
		return null;
	}

	/**
	 * @return The max amount of chunks this entity can own.
	 */
	int getClaimLimit();

	/**
	 * @return true if this entity is valid and can control land.
	 */
	boolean isValid();

	/**
	 * Claim the target location for this entity if possible.
	 *
	 * @param location The target location
	 * @return The newly claimed chunk or null if unable to claim.
	 */
	@Nullable default Claim newClaim(Location location) {
		return newClaim(location.getChunk());
	}

	@Override
	default int compareTo(@NotNull InvasiveEntity o) {
		if (isEntity()) {
			if (o.isEntity()) {
				return String.CASE_INSENSITIVE_ORDER.compare(getName(), o.getName());
			}
			return 0;
		}
		if (this instanceof Clan) {
			if (o instanceof Clan) {
				return Double.compare(((Clan) this).getPower(), ((Clan) o).getPower());
			}
			if (o instanceof Clan.Associate) {
				return Integer.compare(getClaimLimit(), o.getClaimLimit());
			}
			return 0;
		}
		if (this instanceof Clan.Associate) {
			if (o instanceof Clan.Associate) {
				if (((Clan.Associate)this).getPriority().toLevel() > ((Clan.Associate)o).getPriority().toLevel() || EconomyProvision.getInstance().isValid() && (EconomyProvision.getInstance().balance(((Clan.Associate)this).getUser().toBukkit()).get() > EconomyProvision.getInstance().balance(((Clan.Associate)o).getUser().toBukkit()).get())) {
					return 1;
				}
			}
			return 0;
		}
		return -1;
	}

	/**
	 * <p>Get this entity object as a clan, if this object instance isn't
	 * that of a clan then an exception will be thrown. It is mandatory
	 * that before using this method you check first using {@link InvasiveEntity#isClan()}
	 * </p>
	 *
	 * @throws ClassCastException If this entity isn't a clan.
	 * @see InvasiveEntity#isClan()
	 * @return This entity instance as a clan.
	 */
	default Clan getAsClan() throws ClassCastException {
		return (Clan)this;
	}

	/**
	 * <p>Get this entity object as a clan associate, if this object instance isn't
	 * that of a clan associate then an exception will be thrown. It is mandatory
	 * that before using this method you check first using {@link InvasiveEntity#isAssociate()}
	 * </p>
	 *
	 * @throws ClassCastException If this entity isn't a clan associate.
	 * @see InvasiveEntity#isAssociate()
	 * @return This entity instance as a clan associate.
	 */
	default Clan.Associate getAsAssociate() {
		return this instanceof Clan.Associate ? (Clan.Associate)this : ClansAPI.getInstance().getAssociate(UUID.fromString(getTag().getId())).orElse(null);
	}

	/**
	 * <p>Get this entity object as a player, if this object instance isn't
	 * that of a player then an exception will be thrown. It is mandatory
	 * that before using this method you check first using {@link InvasiveEntity#isPlayer()}
	 * </p>
	 *
	 * @throws ClassCastException If this entity isn't a player.
	 * @see InvasiveEntity#isPlayer()
	 * @return This entity instance as an offline player.
	 */
	default OfflinePlayer getAsPlayer() {
		if (isClan()) return null;
		return getTag().getPlayer();
	}

	/**
	 * <p>Get this entity object as an entity directly, if this object instance isn't
	 * that of a entity but is instead an offline player or another entity type then an exception will be thrown.
	 * It is mandatory that before using this method you check first using {@link InvasiveEntity#isEntity()}
	 * </p>
	 *
	 * @throws ClassCastException If this entity isn't a non player entity.
	 * @see InvasiveEntity#isEntity()
	 * @see Tag#getEntity()
	 * @return This entity instance as an entity.
	 */
	default Entity getAsEntity() {
		if (isPlayer() && getAsPlayer().isOnline()) return getAsPlayer().getPlayer();
		return getTag().getEntity();
	}

	/**
	 * @return true if this entity is a clan and <strong>ONLY</strong> a clan.
	 */
	default boolean isClan() {
		return this instanceof Clan || getTag().isClan();
	}

	/**
	 * @return true if this entity is an associate and <strong>ONLY</strong> an associate.
	 */
	default boolean isAssociate() {
		try {
			return (getTag().isPlayer() || getTag().isEntity() || getTag().isServer()) && ClansAPI.getInstance().getAssociate(UUID.fromString(getTag().getId())).isPresent() || this instanceof Clan.Associate;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * @return true if this entity is a player and <strong>ONLY</strong> a player.
	 */
	default boolean isPlayer() {
		return getTag().isPlayer() && getTag().getPlayer() != null && !ClansAPI.getInstance().getAssociate(getTag().getPlayer()).isPresent();
	}

	/**
	 * @return true if this entity is a non player entity and <strong>ONLY</strong> a non player entity.
	 */
	default boolean isEntity() {
		return getTag().isEntity();
	}

	/**
	 * @return true if this entity is a tamable entity and <strong>ONLY</strong> a tamable entity.
	 */
	default boolean isTamable() {
		return isEntity() && getAsEntity() instanceof Tameable || this instanceof AnimalAssociate;
	}

	/**
	 * @return true if this entity is a server entity and <strong>ONLY</strong> a server entity.
	 */
	default boolean isServer() {
		return getName().equals(Bukkit.getName()) || this instanceof ServerAssociate;
	}

	/**
	 * An object that describes the base of any entity. It's sole purpose is to be a container for any type of string id.
	 */
	@FunctionalInterface
	interface Tag extends Serializable {

		/**
		 * Get the id for this tag.
		 *
		 * @return the id.
		 */
		@NotNull String getId();

		/**
		 * @return true if this tag's id represents a clan object.
		 */
		default boolean isClan() {
			return getClan() != null;
		}

		/**
		 * @return true if this tag's id represents a player.
		 */
		default boolean isPlayer() {
			try {
				UUID.fromString(getId());
			} catch (Exception e) {
				return false;
			}
			return getPlayer() != null && getPlayer().getName() != null;
		}

		/**
		 * @return true if this tag's id represents a non player entity.
		 */
		default boolean isEntity() {
			return (!isPlayer() && !isClan());
		}

		/**
		 * @return true if this tag's id represents a server entity ({@link CommandSender}).
		 */
		default boolean isServer() {
			return getId().equals(ClansAPI.getInstance().getSessionId().toString());
		}

		/**
		 * @return true if this tag's id represents either the server or a player, entity or clan.
		 */
		default boolean isValid() {
			return isClan() || isPlayer() || isEntity() || isServer();
		}

		/**
		 * Get the player that this id belongs to, a prior {@link Tag#isPlayer()} check is required.
		 *
		 * @return the player this id belongs to or null.
		 */
		default OfflinePlayer getPlayer() {
			return Bukkit.getOfflinePlayer(UUID.fromString(getId()));
		}

		/**
		 * Get the entity that this id belongs to, a prior {@link Tag#isEntity()} check is required.
		 *
		 * @apiNote Alternatively this method can <strong>also</strong> be used to retrieve a player object if this
		 * tag belongs to one and they are <strong>online</strong>.
		 * @return the entity this id belongs to or null.
		 */
		default Entity getEntity() {
			if (isPlayer() && getPlayer() != null && getPlayer().isOnline()) return getPlayer().getPlayer();
			return Bukkit.getEntity(UUID.fromString(getId()));
		}

		/**
		 * Get the clan that this id belongs to, a prior {@link Tag#isClan()} check is required.
		 *
		 * @return the clan this id belongs to or null.
		 */
		default Clan getClan() {
			return ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(getId()));
		}

	}

}
