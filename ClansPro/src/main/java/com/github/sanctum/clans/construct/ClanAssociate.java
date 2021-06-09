package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.Resident;
import com.github.sanctum.clans.util.RankPriority;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.TimeWatch;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates player information when a clan id is found linked in file.
 */
public class ClanAssociate {

	private final UUID player;

	private final Map<Long, Long> killMap;

	public ClanAssociate(UUID uuid) {
		this.player = uuid;
		this.killMap = new HashMap<>();
	}

	public ClanAssociate(OfflinePlayer player) {
		this.player = player.getUniqueId();
		this.killMap = new HashMap<>();
	}

	/**
	 * @return Gets the backing player object behind this clan associate.
	 */
	public OfflinePlayer getPlayer() {
		return Bukkit.getOfflinePlayer(player);
	}

	/**
	 * @return Gets the clan this associate belongs to.
	 */
	public Clan getClan() {
		return ClansAPI.getInstance().getClan(player);
	}

	/**
	 * @return Gets the associates possible claim information. If the player is not in a claim this will return empty.
	 */
	public Optional<Resident> toResident() {
		return !getPlayer().isOnline() ? Optional.empty() : Optional.ofNullable(Claim.getResident(getPlayer().getPlayer()));
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
		FileManager user = ClansAPI.getData().get(player);
		if (user.exists()) {
			if (user.getConfig().getString("Clan.id") != null) {
				if (Objects.requireNonNull(user.getConfig().getString("Clan.id")).length() != 14) {
					throw new UnsupportedOperationException("[ClansPro] - Clan ID " + user.getConfig().getString("Clan.id") + " invalid, expected format ####-####-####", new Throwable(user.getConfig().getString("Clan.id")));
				}
				FileManager clan = DataManager.FileType.CLAN_FILE.get(user.getConfig().getString("Clan.id"));
				if (!clan.exists()) {
					user.getConfig().set("Clan", null);
					user.saveConfig();
					return null;
				}
				return HUID.fromString(Objects.requireNonNull(user.getConfig().getString("Clan.id")));
			}
		}
		return null;
	}

	/**
	 * @return Gets the associates configured rank tag.
	 */
	public synchronized String getRankTag() {
		String result = "";
		FileManager main = ClansAPI.getData().getMain();
		String member = main.getConfig().getString("Formatting.Styles.Full.Member");
		String mod = main.getConfig().getString("Formatting.Styles.Full.Moderator");
		String admin = main.getConfig().getString("Formatting.Styles.Full.Admin");
		String owner = main.getConfig().getString("Formatting.Styles.Full.Owner");
		switch (getRank()) {
			case "Member":
				result = member;
				break;
			case "Moderator":
				result = mod;
				break;
			case "Admin":
				result = admin;
				break;
			case "Owner":
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
		String member = main.getConfig().getString("Formatting.Styles.Wordless.Member");
		String mod = main.getConfig().getString("Formatting.Styles.Wordless.Moderator");
		String admin = main.getConfig().getString("Formatting.Styles.Wordless.Admin");
		String owner = main.getConfig().getString("Formatting.Styles.Wordless.Owner");
		switch (getRank()) {
			case "Member":
				result = member;
				break;
			case "Moderator":
				result = mod;
				break;
			case "Admin":
				result = admin;
				break;
			case "Owner":
				result = owner;
				break;
		}
		return result;
	}

	/**
	 * @return Gets the associates direct clan position.
	 */
	public synchronized String getRank() {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getClan().getId().toString());
		String rank = "";
		if (clan.getConfig().getStringList("members").contains(player.toString())) {
			rank = "Member";
		}
		if (clan.getConfig().getStringList("moderators").contains(player.toString())) {
			rank = "Moderator";
		}
		if (clan.getConfig().getStringList("admins").contains(player.toString())) {
			rank = "Admin";
		}
		if (Objects.equals(clan.getConfig().getString("owner"), player.toString())) {
			rank = "Owner";
		}
		return rank;
	}

	/**
	 * @return Gets the associates rank priority.
	 */
	public RankPriority getPriority() {
		RankPriority priority = null;
		switch (getRank()) {
			case "Owner":
				priority = RankPriority.HIGHEST;
				break;
			case "Admin":
				priority = RankPriority.HIGHER;
				break;
			case "Moderator":
				priority = RankPriority.HIGH;
				break;
			case "Member":
				priority = RankPriority.NORMAL;
				break;
		}
		return priority;
	}

	/**
	 * Gets the total amount of kills within x amount of time of the specified
	 * threshold.
	 *
	 * @param threshold The time unit threshold to use for conversion
	 * @param time      The amount of time to check elapsed
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
	 * Calling this method invokes +1 kill added to this users cache (Resets after 7 & penalizes)
	 */
	public void killed() {
		long time = System.currentTimeMillis();
		if (killMap.containsKey(time)) {
			killMap.put(time, killMap.get(time) + 1L);
		} else {
			killMap.put(time, 1L);
		}
		if (ClansAPI.getData().getEnabled("Clans.war.killstreak.penalize")) {
			if (getKilled(TimeUnit.valueOf(ClansAPI.getData().getString("Clans.war.killstreak.threshold")), Long.valueOf(ClansAPI.getData().getString("Clans.war.killstreak.time-span"))) >= Long.valueOf(ClansAPI.getData().getString("Clans.war.killstreak.amount"))) {
				killMap.clear();
				getClan().broadcast("&4Camping detected, penalizing power gain.");
				getClan().takePower(Double.valueOf(ClansAPI.getData().getString("Clans.war.killstreak.deduction")));
			}
		}
	}

	/**
	 * Update the associates rank priority.
	 *
	 * @param priority The rank priority to update the associate with.
	 */
	public void setPriority(RankPriority priority) {
		ClansAPI.getInstance().setRank(getClan().getId(), player, priority);
	}

	/**
	 * @return Gets the associates clan nick-name, if one is not present their full user-name will be returned.
	 */
	public synchronized String getNickname() {
		FileManager user = ClansAPI.getData().get(player);
		return user.getConfig().getString("Clan.nickname") != null ? user.getConfig().getString("Clan.nickname") : getPlayer().getName();
	}

	/**
	 * @return Gets the associates clan biography.
	 */
	public synchronized String getBiography() {
		FileManager user = ClansAPI.getData().get(player);
		return user.getConfig().getString("Clan.bio") != null ? user.getConfig().getString("Clan.bio") : "I much like other's, enjoy long walks on the beach.";
	}

	/**
	 * @return Gets the associates clan join date.
	 */
	public synchronized Date getJoinDate() {
		FileManager user = ClansAPI.getData().get(player);
		return user.getConfig().getString("Clan.join-date") != null ? new Date(user.getConfig().getLong("Clan.join-date")) : null;
	}

	/**
	 * @return Gets the associates kill/death ratio.
	 */
	public synchronized double getKD() {
		OfflinePlayer player = Bukkit.getOfflinePlayer(this.player);
		if (Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.13")
				|| Bukkit.getVersion().contains("1.14")) {
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
	public synchronized void changeBio(String newBio) {
		FileManager user = ClansAPI.getData().get(player);
		user.getConfig().set("Clan.bio", newBio);
		user.saveConfig();
		if (getPlayer().isOnline()) {
			Claim.action.sendMessage(getPlayer().getPlayer(), MessageFormat.format(ClansAPI.getData().getMessage("member-bio"), newBio));
		}
	}

	/**
	 * Update the associates clan nick-name;
	 *
	 * @param newName The new nick name
	 */
	public synchronized void changeNickname(String newName) {
		FileManager user = ClansAPI.getData().get(player);
		if (newName.equals("empty")) {
			user.getConfig().set("Clan.nickname", getPlayer().getName());
			newName = getPlayer().getName();
		} else {
			user.getConfig().set("Clan.nickname", newName);
		}
		user.saveConfig();
		if (getPlayer().isOnline()) {
			DefaultClan.action.sendMessage(getPlayer().getPlayer(), MessageFormat.format(ClansAPI.getData().getMessage("nickname"), newName));
		}
	}

	public synchronized void kick() {
		DefaultClan.action.kickPlayer(getPlayer().getUniqueId());
	}

}
