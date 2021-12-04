package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Channel;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.api.PersistentEntity;
import com.github.sanctum.clans.construct.api.Relation;
import com.github.sanctum.clans.construct.api.Teleport;
import com.github.sanctum.clans.construct.extra.PrivateContainer;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.data.Atlas;
import com.github.sanctum.labyrinth.data.AtlasMap;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.MemorySpace;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.data.Primitive;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.skulls.CustomHead;
import java.text.MessageFormat;
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

public final class DefaultAssociate implements Clan.Associate, PersistentEntity {

	private final String name;
	private final UUID id;
	private final Clan clanObject;
	private final HUID clan;
	private Clan.Rank rank;
	private Channel chat;
	private final ItemStack head;
	private final Object data;
	private final Tag tag;
	private final Map<Long, Long> killMap;

	public DefaultAssociate(UUID uuid, Clan.Rank priority, Clan clan) {
		this.clan = clan.getId();
		this.clanObject = clan;
		this.name = LabyrinthProvider.getOfflinePlayers().stream().filter(p -> p.getId().equals(uuid)).map(LabyrinthUser::getName).findFirst().orElseGet(() -> {
			Entity test = Bukkit.getEntity(uuid);
			return test != null ? test.getName() : null;
		});
		this.id = uuid;
		this.tag = uuid::toString;
		this.data = new Object() {
			final PrivateContainer container;

			{
				container = new PrivateContainer() {

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
			protected PrivateContainer getContainer() {
				return container;
			}
		};
		this.rank = priority;
		this.chat = Channel.GLOBAL;
		this.head = CustomHead.Manager.get(name);
		this.killMap = new HashMap<>();
	}

	public @NotNull String getName() {
		return isEntity() ? InvasiveEntity.wrapNonAssociated(getAsEntity()).getName() : this.name;
	}

	public UUID getId() {
		return isEntity() ? id : getUser().getId();
	}

	public LabyrinthUser getUser() {
		return LabyrinthUser.get(getName());
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
	public Channel getChannel() {
		return chat;
	}

	/**
	 * Change the users chat channel.
	 *
	 * @param chat The channel to switch them to.
	 */
	public void setChannel(String chat) {
		this.chat = Channel.valueOf(chat);
	}

	/**
	 * @return Gets the clan this associate belongs to.
	 */
	public Clan getClan() {
		return Optional.ofNullable(ClansAPI.getInstance().getClanManager().getClan(clan)).orElse(clanObject);
	}

	@Override
	public Mailer getMailer() {
		return Mailer.empty(getUser().toBukkit().getPlayer()).prefix().start(ClansAPI.getInstance().getPrefix().joined()).finish();
	}

	/**
	 * @return Gets the associates possible claim information. If the player is not in a claim this will return empty.
	 */
	public Optional<Resident> toResident() {
		return getUser() != null && getUser().isValid() ? !getUser().toBukkit().isOnline() ? Optional.empty() : Optional.ofNullable(Claim.getResident(getUser().toBukkit().getPlayer())) : Optional.empty();
	}

	/**
	 * @return true if the backing clan id for this associate is linked with anything and the backing entity data is also valid.
	 */
	public boolean isValid() {
		if (isEntity() && !isPlayer()) {
			return getClan() != null && getAsEntity() != null && getAsEntity().isValid();
		}
		return getClan() != null && getUser() != null && getUser().isValid();
	}

	@Ordinal(1)
	private Object getData(int key) {
		if ((((70 * 5) + 84) - 14) == key) {
			return data;
		}
		throw new RuntimeException("You are not permitted to use this object!");
	}

	/**
	 * @return Gets the associates rank priority.
	 */
	public Clan.Rank getPriority() {
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
	@Ordinal(50)
	void countKill() {
		long time = System.currentTimeMillis();
		if (killMap.containsKey(time)) {
			killMap.put(time, killMap.get(time) + 1L);
		} else {
			killMap.put(time, 1L);
		}
		if (ClansAPI.getDataInstance().isTrue("Clans.war.killstreak.penalize")) {
			if (getKilled(TimeUnit.valueOf(ClansAPI.getDataInstance().getConfigString("Clans.war.killstreak.threshold")), Long.parseLong(ClansAPI.getDataInstance().getConfigString("Clans.war.killstreak.time-span"))) >= Long.parseLong(ClansAPI.getDataInstance().getConfigString("Clans.war.killstreak.amount"))) {
				killMap.clear();
				getClan().broadcast("&4Camping detected, penalizing power gain.");
				getClan().takePower(Double.parseDouble(ClansAPI.getDataInstance().getConfigString("Clans.war.killstreak.deduction")));
			}
		}
	}

	/**
	 * Update the associates rank priority.
	 *
	 * @param priority The rank priority to update the associate with.
	 */
	public void setPriority(Clan.Rank priority) {
		this.rank = priority;
	}

	/**
	 * @return Gets the associates clan nick-name, if one is not present their full user-name will be returned.
	 */
	public synchronized String getNickname() {
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
	public synchronized Date getJoinDate() {
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
		Optional<MemorySpace> memorySpace = getClan().getMemorySpace();
		if (memorySpace.isPresent()) {
			Node user_data = memorySpace.get().getNode("user-data");
			Node user = user_data.getNode(getId().toString());
			Node bio = user.getNode("bio");
			bio.set(newBio);
			bio.save();
			if (getUser().toBukkit().isOnline()) {
				Claim.ACTION.sendMessage(getUser().toBukkit().getPlayer(), MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-bio"), newBio));
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
				newName = getUser().getName();
			} else {
				bio.set(newName);
			}
			bio.save();
			if (getTag().isPlayer() && getTag().getPlayer().isOnline()) {
				Clan.ACTION.sendMessage(getUser().toBukkit().getPlayer(), MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("nickname"), newName));
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
		return getUser().isOnline() ? Claim.ACTION.claimHardcap(getUser().toBukkit().getPlayer()) : getClan().getClaimLimit();
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
	public List<Carrier> getCarriers() {
		return getClan().getCarriers();
	}

	@Override
	public List<Carrier> getCarriers(Chunk chunk) {
		return getClan().getCarriers(chunk);
	}

	@Override
	public Carrier newCarrier(Location location) {
		return getClan().newCarrier(location);
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
	public void remove(Carrier carrier) {
		getClan().remove(carrier);
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
}