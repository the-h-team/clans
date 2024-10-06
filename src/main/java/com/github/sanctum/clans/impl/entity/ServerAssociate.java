package com.github.sanctum.clans.impl.entity;

import com.github.sanctum.clans.model.Channel;
import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Consultant;
import com.github.sanctum.clans.model.IncomingConsultationListener;
import com.github.sanctum.clans.model.InvasiveEntity;
import com.github.sanctum.clans.model.OutgoingConsultationListener;
import com.github.sanctum.clans.model.Relation;
import com.github.sanctum.clans.model.Teleport;
import com.github.sanctum.clans.model.Ticket;
import com.github.sanctum.clans.util.HiddenMetadata;
import com.github.sanctum.labyrinth.data.Atlas;
import com.github.sanctum.labyrinth.data.AtlasMap;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.skulls.SkullType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerAssociate implements Clan.Associate, Consultant {

	private final Map<String, IncomingConsultationListener> incomingMessageListeners = new HashMap<>();
	private final Map<String, OutgoingConsultationListener> outgoingResponseListeners = new HashMap<>();
	private final InvasiveEntity parent;
	private final Clan clan;
	private final Date join;
	private Clan.Rank rank;
	private Channel chat;
	private final Object data;
	private final Tag tag;
	private final Map<Long, Long> killMap;
	private String nick;
	private String bio;

	public ServerAssociate(InvasiveEntity parent, Clan.Rank priority, Clan clan) {
		this.clan = clan;
		this.parent = parent;
		this.join = new Date();
		this.tag = parent.getTag();
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
			private HiddenMetadata getContainer() {
				return container;
			}
		};
		this.rank = priority;
		this.chat = Channel.valueOf("CONSOLE");
		this.killMap = new HashMap<>();
	}

	public @NotNull String getName() {
		return this.parent.getName();
	}

	public @NotNull UUID getId() {
		return UUID.fromString(getTag().getId());
	}

	/**
	 * @return Gets the players cached head skin.
	 */
	public ItemStack getHead() {
		return SkullType.PLAYER.get();
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
		return clan;
	}

	@Override
	public Mailer getMailer() {
		return Mailer.empty(Bukkit.getConsoleSender());
	}

	/**
	 * @return Gets the associates possible claim information. If the player is not in a claim this will return empty.
	 */
	public Optional<Claim.Resident> toResident() {
		return Optional.empty();
	}

	/**
	 * @return true if the backing clan id for this associate is linked with anything and the backing entity data is also valid.
	 */
	public boolean isValid() {
		return getClan() != null && getId().equals(ClansAPI.getInstance().getSessionId());
	}

	@Ordinal(1)
	private Object getData(int key) {
		if ((((70 * 5) + 84) - 14) == key) {
			return data;
		}
		throw new RuntimeException("You are not permitted to use this object!");
	}

	@NotNull
	@Override
	public Clan.Rank getRank() {
		return this.rank;
	}

	@Override
	public void setRank(Clan.Rank priority) {
		this.rank = priority;
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

	/**
	 * @return Gets the associates clan nick-name, if one is not present their full user-name will be returned.
	 */
	public synchronized @NotNull String getNickname() {
		return nick != null ? nick : getName();
	}

	/**
	 * @return Gets the associates clan biography.
	 */
	public synchronized String getBiography() {
		return this.bio != null ? this.bio : "&fI much like other's, &fenjoy long walks on the beach.";
	}

	/**
	 * @return Gets the associates clan join date.
	 */
	public synchronized @NotNull Date getJoinDate() {
		return join;
	}

	/**
	 * @return Gets the associates kill/death ratio.
	 */
	public synchronized double getKD() {
		return 0.0;
	}

	/**
	 * Update the associates clan biography.
	 *
	 * @param newBio The new biography to set to the associate
	 */
	public synchronized void setBio(String newBio) {
		this.bio = newBio;
	}

	/**
	 * Update the associates clan nick-name;
	 *
	 * @param newName The new nick name
	 */
	public synchronized void setNickname(String newName) {
		this.nick = newName;
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
		return getClan().getClaimLimit();
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
	}

	@Override
	public @NotNull Ticket[] sendMessage(@NotNull Supplier<Object> supplier) {
		if (incomingMessageListeners.isEmpty()) return new Ticket[0];
		List<Ticket> tickets = new ArrayList<>();
		Object object = supplier.get();
		incomingMessageListeners.forEach((id, incomingListener) -> tickets.add(incomingListener.onReceiveMessage(object)));
		tickets.forEach(ticket -> outgoingResponseListeners.forEach((id, outgoingListener) -> outgoingListener.onReceiveResponse(ticket)));
		return tickets.toArray(new Ticket[0]);
	}

	@Override
	public @NotNull Ticket sendMessage(@NotNull Tag channel, @NotNull Supplier<Object> supplier) {
		if (incomingMessageListeners.isEmpty() || incomingMessageListeners.get(channel.getId()) == null)
			return new Ticket();
		Object object = supplier.get();
		IncomingConsultationListener incomingListener = incomingMessageListeners.get(channel.getId());
		Ticket ticket = incomingListener.onReceiveMessage(object);
		OutgoingConsultationListener outgoingListener = outgoingResponseListeners.get(channel.getId());
		if (outgoingListener != null) {
			outgoingListener.onReceiveResponse(ticket);
		}
		return ticket;
	}

	@Override
	public void registerIncomingListener(@NotNull Tag holder, @NotNull IncomingConsultationListener listener) {
		TaskScheduler.of(() -> incomingMessageListeners.put(holder.getId(), listener)).schedule();
	}

	@Override
	public void registerOutgoingListener(@NotNull Tag holder, @NotNull OutgoingConsultationListener listener) {
		TaskScheduler.of(() -> outgoingResponseListeners.put(holder.getId(), listener)).schedule();
	}

	@Override
	public boolean hasIncomingListener(@NotNull Tag holder) {
		return incomingMessageListeners.containsKey(holder.getId());
	}

	@Override
	public boolean hasOutgoingListener(@NotNull Tag holder) {
		return outgoingResponseListeners.containsKey(holder.getId());
	}

	@Override
	public void remove(Carrier carrier) {
		getClan().remove(carrier);
	}

	@Override
	public void remove() {
		getClan().remove(this);
		InvasiveEntity.removeNonAssociated(this.parent, false);
	}
}