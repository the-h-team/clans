package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.util.BukkitColor;
import com.github.sanctum.clans.construct.util.ClanError;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.entity.ServerAssociate;
import com.github.sanctum.clans.construct.util.Reservoir;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.string.CustomColor;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.formatting.string.GradientColor;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.ProgressBar;
import com.github.sanctum.panther.util.TypeAdapter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.melion.rgbchat.chat.TextColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The mother of pearls & diamonds, the glorious invasive clan entity for grouping numerous entities together under a single label.
 */
@Node.Pointer(value = "com.github.sanctum.clans.Clan", type = DefaultClan.class)
@DelegateDeserialization(DefaultClan.class)
public interface Clan extends ConfigurationSerializable, EntityHolder, InvasiveEntity, JsonAdapter<Clan>, Relatable<Clan> {

	ClanActionEngine ACTION = new ClanActionEngine();

	/**
	 * Get the id of the clan stored within the object
	 *
	 * @return clanID stored within the clan object as an HUID
	 */
	@NotNull HUID getId();

	/**
	 * Get the name of the clan
	 *
	 * @return Gets the clan objects clan tag
	 */
	@NotNull String getName();

	/**
	 * Get the display name of the clan.
	 *
	 * @return Get the clan objects custom display name
	 */
	@Nullable String getNickname();

	/**
	 * Get the clans color palette
	 *
	 * @return The clans color palette
	 */
	@NotNull Clan.Color getPalette();

	/**
	 * Get the clans description
	 *
	 * @return The clans description
	 */
	@NotNull String getDescription();

	/**
	 * Get the clan's password
	 *
	 * @return The clan's password otherwise null
	 */
	@Nullable String getPassword();

	/**
	 * Get the user who owns the clan.
	 *
	 * @return Gets the clan owner.
	 */
	@NotNull Clan.Associate getOwner();

	/**
	 * Gets the location of the clan's base.
	 *
	 * @return the clan's base location
	 */
	@Nullable Location getBase();

	/**
	 * @return
	 */
	@NotNull ClearanceOverride getPermissiveHandle();

	/**
	 *
	 */
	void resetPermissions();

	/**
	 * Get the amount of power the clan has
	 *
	 * @return double value
	 */
	double getPower();

	/**
	 * Creates a new associate using the target entity for this clan object.
	 *
	 * @apiNote You will need to manually load the associate into the clan for it to be usable! {@link Clan#add(InvasiveEntity)}
	 *
	 * @param target The target to collect.
	 * @return A valid clan associate or null.
	 */
	@Nullable Clan.Associate newAssociate(UUID target);

	/**
	 * Creates a new associate using the target entity for this clan object.
	 *
	 * @apiNote You will need to manually load the associate into the clan for it to be usable! {@link Clan#add(InvasiveEntity)}
	 *
	 * @param target The target to collect.
	 * @return A valid clan associate or null.
	 */
	@Nullable Clan.Associate newAssociate(InvasiveEntity target);

	/**
	 * Creates a new associate using the target entity for this clan object.
	 *
	 * @apiNote You will need to manually load the associate into the clan for it to be usable! {@link Clan#add(InvasiveEntity)}
	 *
	 * @param target The target to collect.
	 * @return A valid clan associate or null.
	 */
	@Nullable Clan.Associate newAssociate(LabyrinthUser target);

	/**
	 * Get a specified cooldown from cache
	 *
	 * @param action The label to search for
	 * @return The clans cooldown information for the given action
	 */
	@Nullable ClanCooldown getCooldown(String action);

	/**
	 * Retrieves a value of specified type from the clan's persistent data container.
	 *
	 * @param type The type of object to retrieve.
	 * @param key  The key delimiter for the object.
	 * @param <R>  The desired serializable object.
	 * @return The desired serializable object.
	 */
	<R> R getValue(Class<R> type, String key);

	/**
	 * Retrieves a value of specified type from the clan's persistent data container.
	 *
	 * @param flag The type of object to retrieve.
	 * @param key  The key delimiter for the object.
	 * @param <R>  The desired serializable object.
	 * @return The desired serializable object.
	 */
	default <R> R getValue(TypeAdapter<R> flag, String key) {
		return getValue(flag.getType(), key);
	}

	/**
	 * Retrieves a value of specified type from the clan's persistent data container.
	 *
	 * @param key  The key delimiter for the object.
	 * @param <R>  The desired serializable object.
	 * @return The desired serializable object.
	 */
	default <R> R getValue(String key) {
		return getValue(TypeAdapter.get(), key);
	}

	/**
	 * Store a custom serializable object to this clans data container.
	 *
	 * @param key   The key delimiter for the value.
	 * @param value The desired serializable object to be stored.
	 * @param <R>   The type of the value.
	 * @return The same value passed through the parameters.
	 */
	<R> R setValue(String key, R value, boolean temporary);

	/**
	 * @return true if this clan is valid.
	 */
	boolean isValid();

	/**
	 * Check the clans pvp-mode
	 *
	 * @return false if war mode
	 */
	boolean isPeaceful();

	/**
	 * Check if the clan allows friend-fire
	 *
	 * @return true if friendly fire
	 */
	boolean isFriendlyFire();

	/**
	 * Kick a specified member from the clan.
	 *
	 * @param target The specified target to kick.
	 * @return true if the target is a member and got kicked.
	 */
	boolean kick(Associate target);

	/**
	 * Check if this clan owns the provided chunk.
	 *
	 * @param chunk The chunk to call
	 * @return true if the provided chunk is owned by this clan.
	 */
	boolean isOwner(@NotNull Chunk chunk);

	/**
	 * Transfer ownership of the clan to a specified clan member.
	 *
	 * @param target The user to transfer ownership to.
	 * @return true if they are a member of the clan and can be promoted.
	 */
	boolean transferOwnership(Associate target);

	/**
	 * Check if the clan has a cooldown
	 *
	 * @param action The label to search for
	 * @return false if cooldown cache doesn't contain reference
	 */
	boolean isCooldown(String action);

	/**
	 * Remove a persistent value from this clans data container.
	 *
	 * @param key The values key delimiter.
	 */
	void removeValue(String key);

	/**
	 * Change the clans name (No spaces or special chars)
	 *
	 * @param newTag String to change name to.
	 */
	void setName(String newTag);

	/**
	 * Change the clans custom display name (Spaces & special chars)
	 *
	 * @param newTag String to change name to.
	 */
	void setNickname(String newTag);

	/**
	 * Change the clans description
	 */
	void setDescription(String description);

	/**
	 * Change the clan's password
	 *
	 * @param password String to change password to.
	 */
	void setPassword(String password);

	/**
	 * Change the clans color code
	 *
	 * @param newColor Color-code to change the value to.
	 */
	void setColor(String newColor);

	/**
	 * Change the clan's pvp-mode
	 *
	 * @param peaceful The boolean to change the value to
	 */
	void setPeaceful(boolean peaceful);

	/**
	 * Change the friendlyfire status of the clan
	 *
	 * @param friendlyFire The boolean to change the value to
	 */
	void setFriendlyFire(boolean friendlyFire);

	/**
	 * Change the clan's base location
	 *
	 * @param location Update the clans base to a specified location.
	 */
	void setBase(@NotNull Location location);

	/**
	 * Send a message to the clan
	 *
	 * @param message String to broadcast.
	 */
	void broadcast(String message);

	/**
	 * Send a message to the clan
	 *
	 * @param message components to broadcast.
	 */
	void broadcast(BaseComponent... message);

	/**
	 * Send a message to the clan
	 *
	 * @param message components to broadcast.
	 */
	void broadcast(Message... message);


	/**
	 * Send a message to specific clan members.
	 *
	 * @param message String to broadcast.
	 */
	void broadcast(Predicate<Associate> predicate, String message);

	/**
	 * Give the clan some power
	 *
	 * @param amount double amount to give
	 */
	void givePower(double amount);

	/**
	 * Take some power from the clan
	 *
	 * @param amount double amount to take
	 */
	void takePower(double amount);

	/**
	 * Add to the clans max claim's.
	 *
	 * @param amount double amount to give
	 */
	void giveClaims(int amount);

	/**
	 * Take from the clans max claim's.
	 *
	 * @param amount double amount to take
	 */
	void takeClaims(int amount);

	/**
	 * Add win's to the clan's war counter
	 */
	void giveWins(int amount);

	/**
	 * Add losses to the clan's war counter
	 */
	void takeWins(int amount);

	/**
	 * Get all object key's within this clans data container.
	 *
	 * @see com.github.sanctum.labyrinth.data.container.PersistentContainer
	 * @return The list of key's for this clans data container.
	 */
	@NotNull List<String> getKeys();

	/**
	 * Get an array of information for the clan
	 *
	 * @return String array containing clan stats
	 */
	@NotNull String[] getClanInfo();

	/**
	 * Get the full member roster for the clan.
	 *
	 * @return A set of all clan associates.
	 */
	@NotNull Set<Associate> getMembers();

	/**
	 * @return The amount of wars this clan has won.
	 */
	int getWins();

	/**
	 * @return The amount of wars this clan has lost.
	 */
	int getLosses();

	/**
	 * Get the clans cooldown cache
	 *
	 * @return A collection of cooldown objects for this clan
	 */
	@NotNull List<ClanCooldown> getCooldowns();

	/**
	 * Save all the clans information to its backing file.
	 */
	@Override
	void save();

	/**
	 * Remove all the clans information from memory.
	 */
	@Override
	void remove();

	/**
	 * @return the clan member count.
	 */
	default int size() {
		return getMembers().size();
	}

	/**
	 * @return true if this clan is owned by the server not a player.
	 */
	default boolean isConsole() {
		return getOwner() instanceof ServerAssociate;
	}

	/**
	 * Get a member by specification from the clan.
	 *
	 * @param predicate The operation to use.
	 * @return The clan associate or null.
	 */
	default @Nullable Clan.Associate getMember(Predicate<Associate> predicate) {
		return getMembers().stream().filter(predicate).findFirst().orElse(null);
	}

	/**
	 * @return The implementation this object provides.
	 */
	default Implementation getImplementation() {
		return Implementation.UNKNOWN;
	}

	static <V extends Clan> BiFunction<String, V, String> memoryDocketReplacer() {
		return (s, clan) -> {
			FormattedString string = new FormattedString(s);
			return string.replace(":member_list:", clan.getMembers().stream().map(Clan.Associate::getNickname).collect(Collectors.joining(", ")))
					.replace(":member_count:", clan.size() + "")
					.replace(":clan_name:", clan.getName())
					.replace(":owner_name:", clan.getOwner().getName())
					.replace(":reservoir_status:", (Reservoir.get(clan) != null ? "&aUP" : "&4DOWN"))
					.replace(":reservoir_progress:", Optional.ofNullable(Reservoir.get(clan)).map(r -> new ProgressBar().setProgress((int) r.getPower()).setGoal((int) r.getMaxPower()).setFullColor("&5&l").setPrefix(null).setSuffix(null).toString()).orElse(new ProgressBar().setProgress(0).setGoal(100).setBars(10).toString()))
					.replace(":reservoir_power:", Optional.ofNullable(Reservoir.get(clan)).map(r -> r.getPower() + "").orElse(0 + ""))
					.replace(":reservoir_max_power:", Optional.ofNullable(Reservoir.get(clan)).map(r -> r.getMaxPower() + "").orElse(10000 + ""))
					.replace(":clan_logo:", clan.getLogo() == null ? "" : String.join("\n", clan.getLogo()))
					.replace(":clan_color:", clan.getPalette().toString())
					.replace(":clan_name_colored:", clan.getPalette().toString(clan.getName()))
					.replace(":clan_nick_name:", clan.getNickname() != null ? clan.getNickname() : clan.getName())
					.replace(":clan_nick_name_colored:", clan.getName())
					.get();
		};
	}

	enum Implementation {
		DEFAULT, CUSTOM, UNKNOWN
	}

	interface Rank extends Nameable {

		@NotNull String getSymbol();

		boolean isInheritable();

		int getLevel();

		@NotNull Clearance[] getDefaultPermissions();

		@NotNull Rank[] getInheritance();

		@Nullable Clan.Rank getPromotion();

		@Nullable Clan.Rank getDemotion();

		default boolean isHighest() {
			return RankRegistry.getInstance().getHighest().equals(this);
		}

		default boolean isLowest() {
			return RankRegistry.getInstance().getLowest().equals(this);
		}

	}

	/**
	 * A type of invasive entity that belongs to a parent entity known as a {@link Clan}.
	 */
	interface Associate extends InvasiveEntity {

		/**
		 * @return The users display name.
		 */
		@NotNull String getName();

		/**
		 * @return The users unique id.
		 */
		UUID getId();

		/**
		 * @return The users cached head skin.
		 */
		ItemStack getHead();

		/**
		 * @return The chat channel this user resides in.
		 */
		Channel getChannel();

		/**
		 * @return Gets the clan this user belongs to.
		 */
		Clan getClan();

		/**
		 * @return Gets the users personal message services object.
		 */
		Mailer getMailer();

		/**
		 * {@inheritDoc}
		 * @implNote The interface for associates isn't yet set up.
		 * Internal referencing for now uses the clan object.
		 */
		@Override
		@NotNull Claim[] getClaims();

		/**
		 * {@inheritDoc}
		 * @implNote The interface for associates isn't yet set up.
		 * Internal referencing for now uses the clan object.
		 */
		@Override
		@Nullable Claim newClaim(Chunk c);

		/**
		 * Gets the associate's claim information, if possible.
		 * <p>
		 * If the user is not in a claim the optional will be empty.
		 *
		 * @return an Optional describing the associate's claim information
		 */
		Optional<Claim.Resident> toResident();

		/**
		 * Validates this associate's clan and user data.
		 *
		 * @return true if this associate's clan and user data is valid
		 * @implNote This method returns true if the backing clan id matches a
		 * clan and the backing user data is also valid.
		 */
		boolean isValid();

		/**
		 * Gets the user's rank priority.
		 *
		 * @return the user's rank priority
		 */
		@NotNull Clan.Rank getRank();

		/**
		 * Gets the total amount of player kills within x amount of time of the specified
		 * threshold.
		 *
		 * @param threshold The time unit threshold to use for conversion
		 * @param time      The amount of time to call elapsed
		 * @return The amount of kills within x amount of time.
		 */
		long getKilled(TimeUnit threshold, long time);

		/**
		 * Gets the user's clan nickname.
		 * <p>
		 * If one is not present, this method will return their full name.
		 *
		 * @return the user's clan nickname if possible or their full username
		 */
		@NotNull String getNickname();

		/**
		 * Gets the user's clan biography.
		 *
		 * @return the user's clan biography
		 */
		@Nullable String getBiography();

		/**
		 * @return Gets the users clan join date.
		 */
		@NotNull Date getJoinDate();

		/**
		 * @return Gets the users kill/death ratio.
		 */
		double getKD();

		/**
		 * Change the users chat channel.
		 *
		 * @param chat The channel to switch them to.
		 */
		void setChannel(String chat);

		/**
		 * Update the associates rank priority.
		 *
		 * @param priority The rank priority to update the associate with.
		 */
		void setRank(Rank priority);

		/**
		 * Update the users clan biography.
		 *
		 * @param newBio The new biography to set to the associate
		 */
		void setBio(String newBio);

		/**
		 * Updates the user's clan nickname.
		 *
		 * @param newName their new nickname
		 */
		void setNickname(String newName);

		/**
		 * Get the consultant for this associate object.
		 *
		 * The consultant could be this very object instance if an animal entity.
		 * If this is a normal clan associate then the closest relative consultant will attempt
		 * to be returned (an owned animal).
		 *
		 * @return a message consultant object relative to this associate otherwise a server provided consultant via {@link ClansAPI#getConsultant()}
		 */
		default Consultant getConsultant() {
			if (isServer() || isTamable()) {
				return (Consultant) this;
			}
			for (InvasiveEntity entity : getClan()) {
				if (entity.isTamable()) {
					Tameable en = (Tameable) entity.getAsEntity();
					if (en.getOwner() != null && en.getOwner().getUniqueId().equals(getId())) {
						return entity.getAsAssociate().getConsultant();
					}
				}
			}
			return ClansAPI.getInstance().getConsultant();
		}

		/**
		 * Get an array of consultants for this associate object.
		 *
		 * The consultants follow strict ruling of owned only, so any invasive entity not in relation to this one will not
		 * be encountered in the search.
		 *
		 * @return an array of message consultant objects relative to this associate.
		 */
		default Consultant[] getConsultants() {
			Set<Consultant> consultants = new HashSet<>();
			for (InvasiveEntity entity : getClan()) {
				if (entity.isTamable()) {
					Tameable en = (Tameable) entity.getAsEntity();
					if (en.getOwner() != null && en.getOwner().getUniqueId().equals(getId())) {
						consultants.add(entity.getAsAssociate().getConsultant());
					}
				}
			}
			return consultants.toArray(new Consultant[0]);
		}

		/**
		 * Kick the user from their clan. (Nullifies current associate object)
		 */
		@Override
		default void remove() {
			Clan.ACTION.kick(getId()).deploy();
		}

		/**
		 * @return Gets the associates configured rank tag.
		 */
		default @Deprecated String getRankFull() {
			return getRank().getName();
		}

		/**
		 * @return Gets the associates configured wordless rank tag.
		 */
		default @Deprecated String getRankWordless() {
			return getRank().getSymbol();
		}

		/**
		 * Save the associates information to its backing file.
		 */
		@Override
		void save();

		static <V extends Associate> BiFunction<String, V, String> memoryDocketReplacer() {
			return (s, associate) -> {
				FormattedString string = new FormattedString(s);
				return string.replace(":member_name:", associate.getName())
						.replace(":member_kd:", associate.getKD() + "")
						.replace(":member_bio:", associate.getBiography())
						.replace(":member_balance:", (EconomyProvision.getInstance().isValid() && associate.isPlayer() ? EconomyProvision.getInstance().balance(associate.getAsPlayer()).orElse(0.0) : 0.0) + "")
						.replace(":member_nick_name:", associate.getNickname())
						.replace(":member_nick_name_colored:", associate.getClan().getPalette().toString(associate.getNickname()))
						.replace(":member_rank_full:", associate.getRankFull())
						.replace(":member_rank_wordless:", associate.getRankWordless())
						.replace(":member_list:", associate.getClan().getMembers().stream().map(Clan.Associate::getNickname).collect(Collectors.joining(", ")))
						.replace(":member_count:", associate.getClan().size() + "")
						.replace(":clan_name:", associate.getClan().getName())
						.replace(":clan_logo:", associate.getClan().getLogo() == null ? "" : String.join("\n", associate.getClan().getLogo()))
						.replace(":clan_name_colored:", associate.getClan().getPalette().toString(associate.getClan().getName()))
						.replace(":clan_nick_name:", associate.getClan().getNickname() != null ? associate.getClan().getNickname() : associate.getClan().getName())
						.replace(":clan_nick_name_colored:", associate.getClan().getName())
						.translate(associate.getAsPlayer())
						.get();
			};
		}

	}

	class Color implements CharSequence {

		private String start;
		private String end;

		public Color(Clan c) {
			ClanException.call(ClanError::new).check(c).run("Null clan cannot have color!");
		}

		public Color setStart(String start) {
			if (Arrays.stream(BukkitColor.values()).anyMatch(c -> c.toCode().equals(start))) {
				this.end = null;
			}
			this.start = start;
			return this;
		}

		public Color setEnd(String end) {
			if (this.start == null) {
				this.start = end; // you don't have a start color yet bruh lemme just do this for you..
				return this;
			}
			this.end = end;
			return this;
		}

		public Color set(CustomColor color) {
			this.start = color.getStart();
			this.end = color.getEnd();
			return this;
		}

		public void randomize() {
			if (LabyrinthProvider.getInstance().isNew()) {
				set(new RandomHex());
			} else {
				setStart(BukkitColor.random().toCode());
				// nullify gradient since we know we're legacy.
				setEnd(null);
			}
		}

		public boolean isGradient() {
			boolean result = end != null && start.contains("#") && end.contains("#");
			if (result && ClansAPI.getInstance().isTrial())
				result = false; // gradients are for premium only. But hey you still get hex!
			return result;
		}

		public boolean isHex() {
			return start.contains("#");
		}

		public String getStart() {
			return start;
		}

		public String getEnd() {
			return end;
		}

		@Override
		public int length() {
			return start.length();
		}

		@Override
		public char charAt(int index) {
			return start.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return this.start.subSequence(start, end);
		}

		@Override
		public @NotNull String toString() {
			return this.start;
		}

		public String toString(String context) {
			if (!isGradient()) return this + context;
			return toGradient().context(context).translate();
		}

		public @NotNull String[] toArray() {
			return new String[]{start, end};
		}

		public @Nullable GradientColor toGradient() {
			return isGradient() ? new GradientColor(this.start, this.end) : null;
		}

		public org.bukkit.Color toColor() {
			TextColor text = new TextColor(start.replace("#", ""));
			return org.bukkit.Color.fromRGB(text.getRed(), text.getGreen(), text.getBlue());
		}
	}

	@Override
	default Class<Clan> getSerializationSignature() {
		return Clan.class;
	}

	@Override
	default JsonElement write(Clan clan) {
		JsonObject o = new JsonObject();
		o.addProperty("id", clan.getId().toString());
		o.addProperty("name", clan.getName());
		o.addProperty("description", clan.getDescription());
		o.addProperty("password", clan.getPassword());
		JsonObject color = new JsonObject();
		color.addProperty("start", clan.getPalette().toString());
		color.addProperty("end", clan.getPalette().toString());
		o.add("color", color);
		JsonObject members = new JsonObject();
		clan.getMembers().forEach(a -> members.addProperty(a.getId().toString(), a.getRank().getName()));
		o.add("members", members);
		return o;
	}

	@Override
	default Clan read(Map<String, Object> o) {
		ClanManager manager = ClansAPI.getInstance().getClanManager();
		String id = (String) o.get("id");
		Clan test = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(id));
		if (test != null) {
			return test;
		}
		String name = (String) o.get("name");
		String password = (String) o.get("password");
		String description = (String) o.get("description");
		Map<String, String> members = (Map<String, String>) o.get("members");
		Map<String, String> color = (Map<String, String>) o.get("color");
		DefaultClan clan = new DefaultClan(RankRegistry.getInstance(), id);
		clan.setName(name);
		clan.getPalette().setStart(color.get("start")).setEnd(color.get("end"));
		clan.setPassword(password);
		clan.setDescription(description);
		manager.load(clan);
		for (Map.Entry<String, String> entry : members.entrySet()) {
			UUID user = UUID.fromString(entry.getKey());
			Clan.Associate associate = clan.newAssociate(user);
			if (associate != null) {
				clan.add(associate);
				associate.setRank(RankRegistry.getInstance().getRank(entry.getValue()));
				associate.save();
			}
		}
		return clan;
	}

	@Override
	default @NotNull Map<String, Object> serialize() {
		Map<String, Object> o = new HashMap<>();
		o.put("id", getId().toString());
		o.put("name", getName());
		o.put("description", getDescription());
		o.put("password", getPassword());
		Map<String, String> color = new HashMap<>();
		color.put("start", getPalette().toArray()[0]);
		color.put("end", getPalette().toArray()[1]);
		o.put("color", color);
		Map<String, String> members = new HashMap<>();
		for (Associate a : getMembers()) {
			members.put(a.getId().toString(), a.getRank().getName());
		}
		o.put("members", members);
		return o;
	}

	static Clan deserialize(Map<String, Object> o) {
		ClanManager manager = ClansAPI.getInstance().getClanManager();
		String id = (String) o.get("id");
		Clan test = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(id));
		if (test != null) {
			return test;
		}
		String name = (String) o.get("name");
		String password = (String) o.get("password");
		String description = (String) o.get("description");
		Map<String, String> members = (Map<String, String>) o.get("members");
		Map<String, String> color = (Map<String, String>) o.get("color");
		DefaultClan clan = new DefaultClan(RankRegistry.getInstance(), id);
		clan.setName(name);
		clan.getPalette().setStart(color.get("start")).setEnd(color.get("end"));
		clan.setPassword(password);
		clan.setDescription(description);
		manager.load(clan);
		for (Map.Entry<String, String> entry : members.entrySet()) {
			UUID user = UUID.fromString(entry.getKey());
			Clan.Associate associate = clan.newAssociate(user);
			if (associate != null) {
				clan.add(associate);
				associate.setRank(RankRegistry.getInstance().getRank(entry.getValue()));
				associate.save();
			}
		}
		return clan;
	}

	interface Action<O> extends Runnable {

		O deploy();

		@Override
		default void run() {
			deploy();
		}

	}
}
