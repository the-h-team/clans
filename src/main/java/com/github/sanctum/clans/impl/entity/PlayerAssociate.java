package com.github.sanctum.clans.impl.entity;

import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.util.HiddenMetadata;
import com.github.sanctum.clans.util.InvalidAssociateTypeException;
import com.github.sanctum.labyrinth.data.Atlas;
import com.github.sanctum.labyrinth.data.AtlasMap;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.Teleport;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.file.MemorySpace;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.file.Primitive;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.SkullType;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerAssociate implements Clan.Associate, PersistentEntity {

	private final String name;
	private final PlayerSearch search;
	private final UUID id;
	private final Clan clanObject;
	private Clan.Rank rank;
	private ChatChannel chat;
	private ItemStack head = SkullType.PLAYER.get();
	private final Object data;
	private final Tag tag;
	private final Map<Long, Long> killMap;

	public PlayerAssociate(UUID uuid, Clan.Rank priority, Clan clan) throws InvalidAssociateTypeException {
		this.clanObject = clan;
		this.id = uuid;
		this.tag = new Tag() {

			private static final long serialVersionUID = -2455699726164889680L;
			private OfflinePlayer player;

			@Override
			public @NotNull String getId() {
				return uuid.toString();
			}

			@Override
			public OfflinePlayer getPlayer() {
				if (player == null) {
					OfflinePlayer result = Bukkit.getOfflinePlayer(uuid);
					if (result.getName() != null) return (player = result);
				}
				return player;
			}
		};
		this.name = Optional.ofNullable(tag.getPlayer() != null ? tag.getPlayer().getName() : null).orElseGet(() -> {
			Entity test = Bukkit.getEntity(uuid);
			return test != null ? test.getName() : null;
		});
		if (tag.getPlayer() == null)
			throw new InvalidAssociateTypeException("No valid playerdata found under id " + '"' + tag + '"');
		this.search = PlayerSearch.of(name);
		this.data = new Object() {
			final HiddenMetadata container;

			{
				container = new HiddenMetadata() {

					final Atlas map = new AtlasMap();

					@Override
					public <T> T get(Class<T> clazz, String key) {
						return map.getNode(key).get(clazz);
					}

					@Override
					public void set(String key, Object o) {
						map.put(key, o);
					}

					@Override
					public Set<Map.Entry<String, Object>> entrySet() {
						return map.entrySet();
					}
				};
			}

			@Ordinal(32)
			protected HiddenMetadata getContainer() {
				return container;
			}
		};
		this.rank = priority;
		this.chat = ChatChannel.GLOBAL;
		TaskScheduler.of(() -> {
			this.head = CustomHead.Manager.get(name);
		}).scheduleLaterAsync(2L);
		this.killMap = new HashMap<>();
	}

	public @NotNull String getName() {
		return isEntity() ? InvasiveEntity.wrapNonAssociated(getAsEntity()).getName() : this.name;
	}

	public UUID getId() {
		return search != null ? search.getUniqueId() : this.id;
	}

	/**
	 * @return Gets the players cached head skin.
	 */
	public ItemStack getHead() {
		return head == null ? (Arrays.stream(Material.values()).map(Enum::name).collect(Collectors.toList()).contains("PLAYER_HEAD") ? Items.edit().setType(Items.findMaterial("PLAYER_HEAD")).build() : Items.edit().setType(Items.findMaterial("SKULL_ITEM")).build()) : head;
	}

	/**
	 * @return The chat channel this user resides in.
	 */
	public ChatChannel getChannel() {
		return chat;
	}

	/**
	 * Change the users chat channel.
	 *
	 * @param chat The channel to switch them to.
	 */
	public void setChannel(String chat) {
		this.chat = ChatChannel.valueOf(chat);
	}

	/**
	 * @return Gets the clan this associate belongs to.
	 */
	public Clan getClan() {
		return clanObject;
	}

	@Override
	public Mailer getMailer() {
		return Mailer.empty(getTag().getPlayer().getPlayer()).prefix().start(ClansAPI.getInstance().getPrefix().toString()).finish();
	}

	/**
	 * @return Gets the associates possible claim information. If the player is not in a claim this will return empty.
	 */
	public Optional<Claim.Resident> toResident() {
		return isValid() ? !getTag().getPlayer().isOnline() ? Optional.empty() : Optional.ofNullable(Claim.getResident(getTag().getPlayer().getPlayer())) : Optional.empty();
	}

	/**
	 * @return true if the backing clan id for this associate is linked with anything and the backing entity data is also valid.
	 */
	public boolean isValid() {
		if (isEntity() && !isPlayer()) {
			return getClan() != null && getAsEntity() != null && getAsEntity().isValid();
		}
		return getClan() != null;
	}

	@Override
	public @NotNull Clan.Rank getRank() {
		return this.rank;
	}

	@Ordinal(1)
	private Object getData(int key) {
		if ((((70 * 5) + 84) - 14) == key) {
			return data;
		}
		throw new RuntimeException("You are not permitted to use this object!");
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
	@Ordinal(50)
	void countKill() {
		long time = System.currentTimeMillis();
		if (killMap.containsKey(time)) {
			killMap.put(time, killMap.get(time) + 1L);
		} else {
			killMap.put(time, 1L);
		}
		if (ClansAPI.getDataInstance().isTrue("Clans.arena.killstreak.penalize")) {
			if (getKilled(TimeUnit.valueOf(ClansAPI.getDataInstance().getConfigString("Clans.arena.killstreak.threshold")), Long.parseLong(ClansAPI.getDataInstance().getConfigString("Clans.arena.killstreak.time-span"))) >= Long.parseLong(ClansAPI.getDataInstance().getConfigString("Clans.arena.killstreak.amount"))) {
				killMap.clear();
				getClan().broadcast("&4Camping detected, penalizing power gain.");
				getClan().takePower(Double.parseDouble(ClansAPI.getDataInstance().getConfigString("Clans.arena.killstreak.deduction")));
			}
		}
	}

	@Override
	public void setRank(Clan.Rank priority) {
		this.rank = priority;
	}

	/**
	 * @return Gets the associates clan nick-name, if one is not present their full user-name will be returned.
	 */
	public synchronized @NotNull String getNickname() {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node nickname = user.getNode("nickname");
			Primitive n = nickname.toPrimitive();
			return n.isString() ? n.getString() : getName();
		}
		return getName();
	}

	/**
	 * @return Gets the associates' clan biography.
	 */
	public synchronized String getBiography() {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("bio");
			Primitive b = bio.toPrimitive();
			return b.isString() ? b.getString() : "&fI much like other's, &fenjoy long walks on the beach.";
		}
		return "&fI much like other's, &fenjoy long walks on the beach.";
	}

	/**
	 * @return Gets the associates clan join date.
	 */
	public synchronized @NotNull Date getJoinDate() {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node date = user.getNode("join-date");
			Primitive d = date.toPrimitive();
			return d.isLong() ? new Date(d.getLong()) : new Date();
		}
		return new Date();
	}

	/**
	 * @return Gets the associates kill/death ratio.
	 */
	public synchronized double getKD() {
		if (isEntity()) return 0.0;
		OfflinePlayer player = getTag().getPlayer();
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
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("bio");
			bio.set(newBio);
			bio.save();
			if (getTag().getPlayer().isOnline()) {
				Claim.ACTION.sendMessage(getTag().getPlayer().getPlayer(), MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-bio"), newBio));
			}
		}
	}

	/**
	 * Update the associates clan nick-name;
	 *
	 * @param newName The new nick name
	 */
	public synchronized void setNickname(String newName) {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("nickname");
			if (newName.equals("empty")) {
				bio.set(getName());
				newName = getName();
			} else {
				bio.set(newName);
			}
			bio.save();
			if (getTag().isPlayer() && getTag().getPlayer().isOnline()) {
				Clan.ACTION.sendMessage(getTag().getPlayer().getPlayer(), MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("nickname"), newName));
			}
		}
	}

	@Override
	public @NotNull Tag getTag() {
		return tag;
	}

	@Override
	public @NotNull Claim[] getClaims() {
		return getClan().getClaims();
	}

	@Override
	public @Nullable Claim newClaim(Chunk c) {
		return getClan().newClaim(c);
	}

	@Override
	public @NotNull Relation getRelation() {
		return getClan().getRelation();
	}

	@Override
	public @Nullable Teleport getTeleport() {
		return Teleport.get(this);
	}

	@Override
	public int getClaimLimit() {
		return search != null && search.getPlayer().isOnline() ? Claim.ACTION.getPlayerHardcap(getTag().getPlayer().getPlayer()) : getClan().getClaimLimit();
	}

	@Override
	public String toString() {
		return getTag().getId();
	}

	@Override
	public List<String> getLogo() {
		return getClan().getLogo();
	}

	@Override
	public void save() {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			getNode("cached-name").set(getName());
			getNode("entity").set(isEntity() && !isPlayer() ? "ENTITY" : "PLAYER");
			if (isEntity() && !isPlayer()) {
				getNode("entity-type").set(getAsEntity().getType().name());
				getNode("last-location").set(getAsEntity().getLocation());
			}
			user_data.save();
		}
	}

	@Override
	public String getPath() {
		return getId().toString();
	}

	@Override
	public boolean isNode(String key) {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getPath());
			return user.isNode(key);
		}
		return false;
	}

	@Override
	public Node getNode(String key) {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getPath());
			return user.getNode(key);
		}
		return null;
	}

	@Override
	public Set<String> getKeys(boolean deep) {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getPath());
			return user.getKeys(deep);
		}
		return new HashSet<>();
	}

	@Override
	public Map<String, Object> getValues(boolean deep) {
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getPath());
			return user.getValues(deep);
		}
		return new HashMap<>();
	}

	@Override
	public @NotNull Optional<MemorySpace> getMemorySpace() {
		return Optional.of(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlayerAssociate)) return false;
		PlayerAssociate that = (PlayerAssociate) o;
		return getName().equals(that.getName()) && getId().equals(that.getId()) && getRank().equals(that.getRank());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getId(), getRank());
	}
}