package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.Resident;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.JsonAdapter;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.data.NodePointer;
import com.github.sanctum.labyrinth.data.Primitive;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.labyrinth.formatting.string.GradientColor;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.skulls.CustomHead;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NodePointer(value = "com.github.sanctum.Clan", type = DefaultClan.class)
@SerializableAs("com.github.sanctum.Clan")
public interface Clan extends Iterable<Clan.Associate>, JsonAdapter<Clan>, ConfigurationSerializable, Comparable<Clan>, Relatable<Clan>, ClanBank, Serializable {

	ClanAction ACTION = new ClanAction();

	/**
	 * Adds the specified target as a clan member and returns an associate object.
	 *
	 * @param target The target to collect.
	 * @return A valid clan associate or null.
	 */
	@Nullable Clan.Associate accept(UUID target);

	/**
	 * Claim the target chunk for this clan if possible.
	 *
	 * @param c The target chunk
	 * @return The newly claimed chunk or null if unable to claim.
	 */
	@Nullable Claim obtain(Chunk c);

	/**
	 * Get a specified cooldown from cache
	 *
	 * @param action The label to search for
	 * @return The clans cooldown information for the given action
	 */
	@Nullable ClanCooldown getCooldown(String action);

	/**
	 * Retrieve a value of specified type from this clans persistent data container.
	 *
	 * @param type The type of object to retrieve.
	 * @param key  The key delimiter for the object.
	 * @param <R>  The desired serializable object.
	 * @return The desired serializable object.
	 */
	<R> R getValue(Class<R> type, String key);

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
	 * Kick a specified member from the clan.
	 *
	 * @param target The specified target to kick.
	 * @return true if the target is a member and got kicked.
	 */
	boolean kick(UUID target);

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
	boolean transferOwnership(UUID target);

	/**
	 * Check if the clan is neutral in relation with another clan.
	 *
	 * @param targetClanId Target clan
	 * @return true if the two clans are neutral in relation.
	 */
	boolean isNeutral(String targetClanId);

	/**
	 * Check if the clan has a cooldown
	 *
	 * @param action The label to search for
	 * @return false if cooldown cache doesn't contain reference
	 */
	boolean hasCooldown(String action);

	/**
	 * Remove a persistent value from this clans data container.
	 *
	 * @param key The values key delimiter.
	 * @return true if successfully removed.
	 */
	boolean removeValue(String key);

	/**
	 * Change the clans name
	 *
	 * @param newTag String to change name to.
	 */
	void setName(String newTag);

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
	void addMaxClaim(int amount);

	/**
	 * Take from the clans max claim's.
	 *
	 * @param amount double amount to take
	 */
	void takeMaxClaim(int amount);

	/**
	 * Add win's to the clan's war counter
	 */
	void addWin(int amount);

	/**
	 * Add losses to the clan's war counter
	 */
	void addLoss(int amount);

	/**
	 * Send a target clan an ally request.
	 *
	 * @param targetClan The target clan to request positive relation with.
	 */
	void sendAllyRequest(HUID targetClan);

	/**
	 * Send a target clan an ally request.
	 *
	 * @param targetClan The target clan to request positive relation with.
	 * @param message    The custom message to send to the clan.
	 */
	void sendAllyRequest(HUID targetClan, String message);

	/**
	 * Force alliance with a specified clan.
	 *
	 * @param targetClan The target clan to ally by their id.
	 */
	void addAlly(HUID targetClan);

	/**
	 * Force alliance removal from a specified clan for this clan only.
	 * <p>
	 * Its possible you will need to also run this method from the targeted clan.
	 *
	 * @param targetClan The target clan to make neutral by their id.
	 */
	void removeAlly(HUID targetClan);

	/**
	 * Force enemy relation with a specified clan.
	 *
	 * @param targetClan The target clan to make enemies with by their id.
	 */
	void addEnemy(HUID targetClan);

	/**
	 * Force the removal of a specified enemy for this clan only.
	 * <p>
	 * Its possible you will need to also run this method from the targeted clan.
	 *
	 * @param targetClan The target clan to make neutral by their id.
	 */
	void removeEnemy(HUID targetClan);

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
	 * Get a member by specification from the clan.
	 *
	 * @param predicate The operation to use.
	 * @return The clan associate or null.
	 */
	@Nullable Clan.Associate getMember(Predicate<Associate> predicate);

	/**
	 * Format a given double into different configured language types
	 *
	 * @param amount double to format
	 * @return Gets the formatted result as a local.
	 */
	@NotNull String format(String amount);

	/**
	 * Get a full roster of clan allies by clan id
	 *
	 * @return A string array of clan ids
	 */
	@NotNull List<String> getAllyList();

	/**
	 * Get a full roster of clan enemies by clan id
	 *
	 * @return A string list of clan ids
	 */
	@NotNull List<String> getEnemyList();

	/**
	 * Get all object key's within this clans data container.
	 *
	 * @return The list of key's for this clans data container.
	 */
	@NotNull List<String> getDataKeys();

	/**
	 * Get a full list of all current clan's attempting positive relation with us.
	 *
	 * @return A string list of clan ally requests by clan id.
	 */
	@NotNull List<String> getAllyRequests();

	/**
	 * Get the full roster of clan members.
	 *
	 * @return Gets the member list of the clan object.
	 */
	@NotNull String[] getMemberIds();

	/**
	 * Get an array of information for the clan
	 *
	 * @return String array containing clan stats
	 */
	@NotNull String[] getClanInfo();

	/**
	 * Get the full list of owned claims for this clan by id.
	 *
	 * @return An array of claim id's
	 */
	@NotNull String[] getOwnedClaimsList();

	/**
	 * Get the full list of owned claims for this clan.
	 *
	 * @return An array of clan claims
	 */
	@NotNull Claim[] getOwnedClaims();

	/**
	 * Get the full member roster for the clan.
	 *
	 * @return A set of all clan associates.
	 */
	@NotNull Set<Associate> getMembers();

	/**
	 * Get a full roster of allied clans for this clan.
	 *
	 * @return A uniformed component listing.
	 */
	@NotNull UniformedComponents<Clan> getAllies();

	/**
	 * Get a full roster of rivaled clans for this clan.
	 *
	 * @return A uniformed component listing.
	 */
	@NotNull UniformedComponents<Clan> getEnemies();

	/**
	 * Get's the location of the clans base
	 *
	 * @return A base location.
	 */
	@Nullable Location getBase();

	/**
	 * Get the amount of power the clan has
	 *
	 * @return double value
	 */
	double getPower();

	/**
	 * @return The max amount of chunks this clan can own.
	 */
	int getMaxClaims();

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

	/*
	default String getMotd() {
		return "Hello! This is a test message. If you're reading this, the message should be formatted!";
	}
	 */

	/**
	 * {@inheritDoc}
	 */
	default Implementation getImplementation() {
		return Implementation.UNKNOWN;
	}

	@Override
	default JsonElement write(Clan clan) {
		JsonObject o = new JsonObject();
		o.addProperty("id", clan.getId().toString());
		o.addProperty("name", clan.getName());
		o.addProperty("description", clan.getDescription());
		o.addProperty("password", clan.getPassword());
		JsonObject color = new JsonObject();
		color.addProperty("start", clan.getPalette().getStart());
		color.addProperty("end", clan.getPalette().getEnd());
		o.add("color", color);
		JsonObject members = new JsonObject();
		clan.forEach(a -> members.addProperty(a.getUser().getId().toString(), a.getPriority().name()));
		o.add("members", members);
		return o;
	}

	@Override
	default Clan read(Map<String, Object> o) {
		ClanManager manager = ClansAPI.getInstance().getClanManager();
		String id = (String) o.get("id");
		DefaultClan test = (DefaultClan) ClansAPI.getInstance().getClan(id);
		if (test != null) {
			return test;
		}
		String name = (String) o.get("name");
		String password = (String) o.get("password");
		String description = (String) o.get("description");
		Map<String, String> members = (Map<String, String>) o.get("members");
		Map<String, String> color = (Map<String, String>) o.get("color");
		DefaultClan clan = new DefaultClan(id);
		clan.setName(name);
		clan.getPalette().setStart(color.get("start")).setEnd(color.get("end"));
		clan.setPassword(password);
		clan.setDescription(description);
		manager.load(clan);
		for (Map.Entry<String, String> entry : members.entrySet()) {
			UUID user = UUID.fromString(entry.getKey());
			Clan.Associate associate = clan.accept(user);
			if (associate != null) {
				associate.setPriority(RankPriority.valueOf(entry.getValue()));
			}
		}
		return clan;
	}

	@Override
	default Class<Clan> getClassType() {
		return Clan.class;
	}

	@Override
	default @NotNull Map<String, Object> serialize() {
		Map<String, Object> o = new HashMap<>();
		o.put("id", getId().toString());
		o.put("name", getName());
		o.put("description", getDescription());
		o.put("password", getPassword());
		Map<String, String> color = new HashMap<>();
		color.put("start", getPalette().getStart());
		color.put("end", getPalette().getEnd());
		o.put("color", color);
		Map<String, String> members = new HashMap<>();
		for (Associate a : getMembers()) {
			members.put(a.getUser().getId().toString(), a.getPriority().name());
		}
		o.put("members", members);
		return o;
	}

	static Clan deserialize(Map<String, Object> o) {
		ClanManager manager = ClansAPI.getInstance().getClanManager();
		String id = (String) o.get("id");
		DefaultClan test = (DefaultClan) ClansAPI.getInstance().getClan(id);
		if (test != null) {
			return test;
		}
		String name = (String) o.get("name");
		String password = (String) o.get("password");
		String description = (String) o.get("description");
		Map<String, String> members = (Map<String, String>) o.get("members");
		Map<String, String> color = (Map<String, String>) o.get("color");
		DefaultClan clan = new DefaultClan(id);
		clan.setName(name);
		clan.getPalette().setStart(color.get("start")).setEnd(color.get("end"));
		clan.setPassword(password);
		clan.setDescription(description);
		manager.load(clan);
		for (Map.Entry<String, String> entry : members.entrySet()) {
			UUID user = UUID.fromString(entry.getKey());
			Clan.Associate associate = clan.accept(user);
			if (associate != null) {
				associate.setPriority(RankPriority.valueOf(entry.getValue()));
			}
		}
		return clan;
	}

	void save();

	enum Implementation {
		DEFAULT, CUSTOM, UNKNOWN
	}


	/**
	 * Encapsulates player information when a clan id is found linked in file.
	 */
	class Associate {

		private final String player;

		private final HUID clan;

		private RankPriority rank;

		private String chat;

		private final ItemStack head;

		private final Map<Long, Long> killMap;

		public Associate(UUID uuid, RankPriority priority, HUID clanID) {
			this.player = LabyrinthProvider.getOfflinePlayers().stream().filter(p -> p.getId().equals(uuid)).map(LabyrinthUser::getName).findFirst().get();
			this.rank = priority;
			this.clan = clanID;
			this.chat = "GLOBAL";
			this.head = CustomHead.Manager.get(player);
			this.killMap = new HashMap<>();
		}

		public String getName() {
			return this.player;
		}

		public UUID getId() {
			return getUser().getId();
		}

		public LabyrinthUser getUser() {
			return LabyrinthUser.get(getName());
		}

		/**
		 * @return Gets the players cached head skin.
		 */
		public ItemStack getHead() {
			return head;
		}

		/**
		 * @return The chat channel this user resides in.
		 */
		public String getChat() {
			return chat;
		}

		/**
		 * Change the users chat channel.
		 *
		 * @param chat The channel to switch them to.
		 */
		public void setChat(String chat) {
			this.chat = chat;
		}

		/**
		 * @return Gets the clan this associate belongs to.
		 */
		public Clan getClan() {
			return ClansAPI.getInstance().getClan(getId());
		}

		/**
		 * @return Gets the associates possible claim information. If the player is not in a claim this will return empty.
		 */
		public Optional<Resident> toResident() {
			return !getUser().toBukkit().isOnline() ? Optional.empty() : Optional.ofNullable(Claim.getResident(getUser().toBukkit().getPlayer()));
		}

		/**
		 * @return true if the backing clan id for this associate is linked with a cached file.
		 */
		public boolean isValid() {
			return getClanID() != null;
		}

		/**
		 * @return Gets the associates clan id or null if an issue has occurred.
		 */
		public synchronized @Nullable HUID getClanID() {
			return this.clan;
		}

		/**
		 * @return Gets the associates configured rank tag.
		 */
		public synchronized String getRankTag() {
			String result = "";
			FileManager main = ClansAPI.getData().getMain();
			String member = main.getRoot().getString("Formatting.Chat.Styles.Full.Member");
			String mod = main.getRoot().getString("Formatting.Chat.Styles.Full.Moderator");
			String admin = main.getRoot().getString("Formatting.Chat.Styles.Full.Admin");
			String owner = main.getRoot().getString("Formatting.Chat.Styles.Full.Owner");
			switch (getPriority()) {
				case NORMAL:
					result = member;
					break;
				case HIGH:
					result = mod;
					break;
				case HIGHER:
					result = admin;
					break;
				case HIGHEST:
					result = owner;
					break;
			}
			return result;
		}

		/**
		 * @return Gets the associates configured wordless rank tag.
		 */
		public synchronized String getRankShort() {
			String result = "";
			FileManager main = ClansAPI.getData().getMain();
			String member = main.getRoot().getString("Formatting.Chat.Styles.Wordless.Member");
			String mod = main.getRoot().getString("Formatting.Chat.Styles.Wordless.Moderator");
			String admin = main.getRoot().getString("Formatting.Chat.Styles.Wordless.Admin");
			String owner = main.getRoot().getString("Formatting.Chat.Styles.Wordless.Owner");
			switch (getPriority()) {
				case NORMAL:
					result = member;
					break;
				case HIGH:
					result = mod;
					break;
				case HIGHER:
					result = admin;
					break;
				case HIGHEST:
					result = owner;
					break;
			}
			return result;
		}

		/**
		 * @return Gets the associates rank priority.
		 */
		public RankPriority getPriority() {
			return this.rank;
		}

		/**
		 * Gets the total amount of kills within x amount of time of the specified
		 * threshold.
		 *
		 * @param threshold The time unit threshold to use for conversion
		 * @param time      The amount of time to call elapsed
		 * @return The amount of kills within x amount of time.
		 */
		public long getKilled(TimeUnit threshold, long time) {
			long amount = 0;
			for (Map.Entry<Long, Long> entry : killMap.entrySet()) {
				if (TimeWatch.start(entry.getKey()).isBetween(threshold, time)) {
					amount += entry.getValue();
				}
			}
			return amount;
		}

		/**
		 * Calling this method invokes +1 kill added to this users cache (Resets after the configured amount & penalizes)
		 */
		public void countKill() {
			long time = System.currentTimeMillis();
			if (killMap.containsKey(time)) {
				killMap.put(time, killMap.get(time) + 1L);
			} else {
				killMap.put(time, 1L);
			}
			if (ClansAPI.getData().isTrue("Clans.war.killstreak.penalize")) {
				if (getKilled(TimeUnit.valueOf(ClansAPI.getData().getConfigString("Clans.war.killstreak.threshold")), Long.parseLong(ClansAPI.getData().getConfigString("Clans.war.killstreak.time-span"))) >= Long.parseLong(ClansAPI.getData().getConfigString("Clans.war.killstreak.amount"))) {
					killMap.clear();
					getClan().broadcast("&4Camping detected, penalizing power gain.");
					getClan().takePower(Double.parseDouble(ClansAPI.getData().getConfigString("Clans.war.killstreak.deduction")));
				}
			}
		}

		/**
		 * Update the associates rank priority.
		 *
		 * @param priority The rank priority to update the associate with.
		 */
		public void setPriority(RankPriority priority) {
			this.rank = priority;
		}

		/**
		 * @return Gets the associates clan nick-name, if one is not present their full user-name will be returned.
		 */
		public synchronized String getNickname() {
			FileManager clan = ClansAPI.getData().getClanFile(getClan());
			Node user_data = clan.getRoot().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node nickname = user.getNode("nickname");
			Primitive n = nickname.toPrimitive();
			return n.isString() ? n.getString() : getName();
		}

		/**
		 * @return Gets the associates clan biography.
		 */
		public synchronized String getBiography() {
			FileManager clan = ClansAPI.getData().getClanFile(getClan());
			Node user_data = clan.getRoot().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("bio");
			Primitive b = bio.toPrimitive();
			return b.isString() ? b.getString() : "I much like other's, enjoy long walks on the beach.";
		}

		/**
		 * @return Gets the associates clan join date.
		 */
		public synchronized Date getJoinDate() {
			FileManager clan = ClansAPI.getData().getClanFile(getClan());
			Node user_data = clan.getRoot().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node date = user.getNode("join-date");
			Primitive d = date.toPrimitive();
			return d.isLong() ? new Date(d.getLong()) : new Date();
		}

		/**
		 * @return Gets the associates kill/death ratio.
		 */
		public synchronized double getKD() {
			OfflinePlayer player = getUser().toBukkit();
			if (!Bukkit.getVersion().contains("1.14") || !Bukkit.getVersion().contains("1.15") || !Bukkit.getVersion().contains("1.16")
					|| !Bukkit.getVersion().contains("1.17")) {
				return 0.0;
			}
			int kills = player.getStatistic(Statistic.PLAYER_KILLS);
			int deaths = player.getStatistic(Statistic.DEATHS);
			double result;
			if (deaths == 0) {
				result = kills;
			} else {
				double value = (double) kills / deaths;
				result = Math.round(value);
			}
			return result;
		}

		/**
		 * Update the associates clan biography.
		 *
		 * @param newBio The new biography to set to the associate
		 */
		public synchronized void setBio(String newBio) {
			FileManager clan = ClansAPI.getData().getClanFile(getClan());
			Node user_data = clan.getRoot().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("bio");
			bio.set(newBio);
			bio.save();
			if (getUser().toBukkit().isOnline()) {
				Claim.ACTION.sendMessage(getUser().toBukkit().getPlayer(), MessageFormat.format(ClansAPI.getData().getMessageResponse("member-bio"), newBio));
			}
		}

		/**
		 * Update the associates clan nick-name;
		 *
		 * @param newName The new nick name
		 */
		public synchronized void setNickname(String newName) {
			FileManager clan = ClansAPI.getData().getClanFile(getClan());
			Node user_data = clan.getRoot().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("nickname");
			if (newName.equals("empty")) {
				bio.set(getName());
				newName = getUser().getName();
			} else {
				bio.set(newName);
			}
			bio.save();
			if (getUser().toBukkit().isOnline()) {
				ACTION.sendMessage(getUser().toBukkit().getPlayer(), MessageFormat.format(ClansAPI.getData().getMessageResponse("nickname"), newName));
			}
		}

		public synchronized void kick() {
			ACTION.kickPlayer(getUser().getId());
		}

		public abstract static class Teleport {

			private static final Set<Teleport> REQUESTS = new HashSet<>();

			private final Player target;

			private final Date date;

			private Date accepted;

			private State state;

			private final Associate associate;

			protected Teleport(Associate teleporter, Player target) {
				this.target = target;
				this.associate = teleporter;
				this.date = new Date();
				this.state = State.INITIALIZED;
				REQUESTS.add(this);
			}

			public Associate getAssociate() {
				return associate;
			}

			public TimeWatch.Recording getRecording() {
				return TimeWatch.Recording.subtract(date.getTime());
			}

			public TimeWatch.Recording getAccepted() {
				return TimeWatch.Recording.subtract(accepted.getTime());
			}

			public Player getTarget() {
				return target;
			}

			public void setState(State state) {
				this.state = state;
			}

			public State getState() {
				return this.state;
			}

			public void teleport() {
				Message.form(getAssociate().getUser().toBukkit().getPlayer()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&aTeleporting in 10 seconds, don't move.");
				Message.form(getTarget()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&a" + associate.getUser().getName() + " is teleporting to you.");
				this.state = State.TELEPORTING;
				this.accepted = new Date();
				Schedule.sync(() -> {
					if (getState() == State.TELEPORTING) {
						associate.getUser().toBukkit().getPlayer().teleport(getTarget());
						cancel();
						associate.getUser().toBukkit().getPlayer().getWorld().playSound(associate.getUser().toBukkit().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
					} else {
						cancel();
					}
				}).waitReal(20 * 10);
			}

			public void cancel() {
				REQUESTS.remove(this);
			}

			public static Teleport get(Associate associate) {
				return REQUESTS.stream().filter(r -> r.getAssociate().equals(associate)).findFirst().orElse(null);
			}

			public static class Impl extends Teleport {
				public Impl(Associate teleporter, Player target) {
					super(teleporter, target);
				}
			}

			public enum State {
				INITIALIZED, TELEPORTING, EXPIRED;
			}

		}
	}

	class Color {

		private final Clan parent;
		private String start;
		private String end;

		public Color(Clan c) {
			this.parent = c;
		}

		public Color setStart(String start) {
			this.start = start;
			return this;
		}

		public Color setEnd(String end) {
			this.end = end;
			return this;
		}

		public boolean isGradient() {
			return end != null;
		}

		@Override
		public String toString() {
			return isGradient() ? toGradient().context(parent.getName()).translate() : this.start;
		}

		public String getStart() {
			return start;
		}

		public String getEnd() {
			return end;
		}

		public GradientColor toGradient() {
			return new GradientColor(this.start, this.end);
		}

	}
}
