package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ScoreTag;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownArena;
import com.github.sanctum.clans.util.RankPriority;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.clans.util.events.clans.ClanCreateEvent;
import com.github.sanctum.clans.util.events.clans.ClanCreatedEvent;
import com.github.sanctum.clans.util.events.command.ClanInformationAdaptEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.formatting.string.PaginatedAssortment;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ClanAction extends StringLibrary {

	public final CooldownArena arena = new CooldownArena();

	public void create(UUID owner, String clanName, String password) {
		FileManager user = ClansAPI.getData().get(owner);
		if (getClanID(owner) == null) {
			if (clanName.length() > ClansAPI.getData().getMain().getConfig().getInt("Formatting.tag-size")) {
				if (Bukkit.getPlayer(owner) != null) {
					sendMessage(Bukkit.getPlayer(owner), ClansAPI.getData().getMessage("too-long"));
				}
				return;
			}
			ClanCreateEvent e = new ClanCreateEvent(owner, clanName, password);
			Bukkit.getPluginManager().callEvent(e);
			if (!e.isCancelled()) {
				FileConfiguration local = user.getConfig();
				String newID = clanCode();
				local.set("Clan.id", newID);
				local.set("Clan.join-date", new Date().getTime());
				ClansAPI.getData().ASSOCIATES.add(new ClanAssociate(owner));
				user.saveConfig();
				String status = "OPEN";
				if (password == null) {
					createClanFile(newID, clanName);
					String format = MessageFormat.format(ClansAPI.getData().getMessage("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				} else {
					status = "LOCKED";
					createClanFile(newID, clanName, password);
					String format = MessageFormat.format(ClansAPI.getData().getMessage("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				}
				FileManager clanFile = DataManager.FileType.CLAN_FILE.get(newID);
				FileConfiguration fc = clanFile.getConfig();
				List<String> members = fc.getStringList("members");
				members.add(owner.toString());
				fc.set("members", members);
				fc.set("owner", owner.toString());
				clanFile.saveConfig();
				DefaultClan instance = new DefaultClan(newID);
				ClansAPI.getInstance().getClanManager().load(instance);
				if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
					ScoreTag.set(Bukkit.getPlayer(owner), ClansAPI.getData().prefixedTag(getClan(getClanID(clanName)).getColor(), clanName));
				}
				ClanCreatedEvent event = new ClanCreatedEvent(owner, clanName);
				Bukkit.getPluginManager().callEvent(event);
			}
		} else {
			if (Bukkit.getPlayer(owner) != null) {
				sendMessage(Bukkit.getPlayer(owner), alreadyInClan());
			}
		}
	}

	public void createOffline(UUID owner, String clanName, String password) {
		FileManager user = ClansAPI.getData().get(owner);
		if (getClanID(owner) == null) {
			if (clanName.length() > ClansAPI.getData().getMain().getConfig().getInt("Formatting.tag-size")) {
				ClansPro.getInstance().getLogger().warning("- Clan tag is too long, API usage detected. Bypassing...");
			}
			ClanCreateEvent e = new ClanCreateEvent(owner, clanName, password);
			Bukkit.getPluginManager().callEvent(e);
			if (!e.isCancelled()) {
				FileConfiguration local = user.getConfig();
				String newID = clanCode();
				local.set("Clan.id", newID);
				local.set("Clan.join-date", new Date().getTime());
				ClansAPI.getData().ASSOCIATES.add(new ClanAssociate(owner));
				user.saveConfig();
				String status = "OPEN";
				if (password == null) {
					createClanFile(newID, clanName);
					String format = MessageFormat.format(ClansAPI.getData().getMessage("creation"), Bukkit.getOfflinePlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				} else {
					status = "LOCKED";
					createClanFile(newID, clanName, password);
					String format = MessageFormat.format(ClansAPI.getData().getMessage("creation"), Bukkit.getOfflinePlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				}
				FileManager clanFile = DataManager.FileType.CLAN_FILE.get(newID);
				FileConfiguration fc = clanFile.getConfig();
				List<String> members = fc.getStringList("members");
				members.add(owner.toString());
				fc.set("members", members);
				fc.set("owner", owner.toString());
				clanFile.saveConfig();
				for (String clanID : getAllClanIDs()) {
					DefaultClan instance = new DefaultClan(clanID);
					ClansAPI.getData().CLANS.add(instance);
				}
				ClanCreatedEvent event = new ClanCreatedEvent(owner, clanName);
				Bukkit.getPluginManager().callEvent(event);
			}
		} else {
			if (Bukkit.getPlayer(owner) != null) {
				sendMessage(Bukkit.getPlayer(owner), alreadyInClan());
			}
		}
	}

	public void removePlayer(UUID target) {
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(target).orElse(null);
		if (associate != null) {
			if (associate.getClanID() == null) {
				ClansPro.getInstance().dataManager.ASSOCIATES.remove(associate);
				return;
			}
			if (!(associate.getClan() instanceof DefaultClan))
				return;
			DefaultClan clanIndex = (DefaultClan) associate.getClan();
			FileManager clan = ClansAPI.getData().getClanFile(clanIndex);
			FileManager user = ClansAPI.getData().get(target);
			List<String> members = clan.getConfig().getStringList("members");
			if (associate.getPlayer().isOnline()) {
				ClansAPI.getData().CHAT_MODE.put(associate.getPlayer().getPlayer(), "GLOBAL");
				if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
					ScoreTag.remove(associate.getPlayer().getPlayer());
				}
			}
			switch (getRank(target)) {
				case "Owner":
					for (String s : clanIndex.getMembersList()) {
						OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(s));
						if (associate.getPlayer().isOnline()) {
							if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
								ScoreTag.remove(associate.getPlayer().getPlayer());
							}
						}
						if (!op.getUniqueId().equals(target)) {
							kickPlayer(op.getUniqueId());
						}
					}
					for (String ally : clanIndex.getAllyList()) {
						Clan a = ClansAPI.getInstance().getClan(ally);
						a.removeAlly(clanIndex.getId());
					}
					for (String enemy : clanIndex.getEnemyList()) {
						Clan a = ClansAPI.getInstance().getClan(enemy);
						a.removeEnemy(clanIndex.getId());
					}
					FileManager regions = ClansAPI.getInstance().getClaimManager().getFile();
					regions.getConfig().set(getClanID(target), null);
					regions.saveConfig();
					String clanName = clan.getConfig().getString("name");
					for (String s : associate.getClan().getDataKeys()) {
						associate.getClan().removeValue(s);
					}
					ClansPro.getInstance().dataManager.CLAN_ENEMY_MAP.remove(clanIndex.getId().toString());
					ClansPro.getInstance().dataManager.CLAN_ALLY_MAP.remove(clanIndex.getId().toString());
					ClansPro.getInstance().dataManager.CLANS.remove(clanIndex);
					user.getConfig().set("Clan", null);
					user.saveConfig();
					String format = MessageFormat.format(ClansAPI.getData().getMessage("deletion"), clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
					ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(target));
					clan.delete();
					ClansAPI.getInstance().getClaimManager().refresh();
					break;
				case "Admin":
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
					List<String> admins = clan.getConfig().getStringList("admins");
					admins.remove(target.toString());
					members.remove(target.toString());
					clan.getConfig().set("members", members);
					clan.getConfig().set("admins", admins);
					user.getConfig().set("Clan", null);
					clan.saveConfig();
					user.saveConfig();
					ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(target));
					break;
				case "Moderator":
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
					List<String> moderators = clan.getConfig().getStringList("moderators");
					moderators.remove(target.toString());
					members.remove(target.toString());
					clan.getConfig().set("members", members);
					clan.getConfig().set("moderators", moderators);
					user.getConfig().set("Clan", null);
					clan.saveConfig();
					user.saveConfig();
					ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(target));
					break;
				case "Member":
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
					user.getConfig().set("Clan", null);
					user.saveConfig();
					members.remove(target.toString());
					clan.getConfig().set("members", members);
					clan.saveConfig();
					ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(target));
					break;
			}
		} else {
			if (Bukkit.getPlayer(target) != null) {
				sendMessage(Bukkit.getPlayer(target), notInClan());
			}
		}
	}

	public void joinClan(UUID target, String clanName, String password) {
		if (getClanID(target) == null) {
			if (!getAllClanNames().contains(clanName)) {
				if (Bukkit.getOfflinePlayer(target).isOnline()) {
					sendMessage(Bukkit.getPlayer(target), clanUnknown(clanName));
				}
				return;
			}
			if (ClansAPI.getInstance().getClan(getClanID(clanName)).getPassword() == null) {
				FileManager user = ClansAPI.getData().get(target);
				user.getConfig().set("Clan.id", getClanID(clanName));
				user.getConfig().set("Clan.join-date", new Date().getTime());
				user.saveConfig();
				ClansAPI.getData().ASSOCIATES.add(new ClanAssociate(target));
				FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(target));
				List<String> members = clan.getConfig().getStringList("members");
				members.add(target.toString());
				clan.getConfig().set("members", members);
				clan.saveConfig();
				Clan clanIndex = ClansAPI.getInstance().getClan(target);
				clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-join"), Bukkit.getOfflinePlayer(target).getName()));
				if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						ScoreTag.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getData().prefixedTag(getClan(getClanID(clanName)).getColor(), clanName));
					}
				}
				return;
			}
			if (ClansAPI.getInstance().getClan(getClanID(clanName)).getPassword() != null && password.equals("none")) {
				if (Bukkit.getOfflinePlayer(target).isOnline()) {
					sendMessage(Bukkit.getPlayer(target), passwordInvalid());
				}
				return;
			}
			if (ClansAPI.getInstance().getClan(getClanID(clanName)).getPassword().equals(password)) {
				FileManager user = ClansAPI.getData().get(target);
				user.getConfig().set("Clan.id", getClanID(clanName));
				user.getConfig().set("Clan.join-date", new Date().getTime());
				user.saveConfig();
				ClansAPI.getData().ASSOCIATES.add(new ClanAssociate(target));
				FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(target));
				List<String> members = clan.getConfig().getStringList("members");
				members.add(target.toString());
				clan.getConfig().set("members", members);
				clan.saveConfig();
				Clan clanIndex = ClansAPI.getInstance().getClan(target);
				clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-join"), Bukkit.getOfflinePlayer(target).getName()));
				if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						ScoreTag.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getData().prefixedTag(getClan(getClanID(clanName)).getColor(), clanName));
					}
				}
			} else {
				if (Bukkit.getPlayer(target) != null) {
					sendMessage(Bukkit.getPlayer(target), wrongPassword());
				}
			}
		} else {
			if (Bukkit.getPlayer(target) != null) {
				sendMessage(Bukkit.getPlayer(target), alreadyInClan());
			}
		}
	}

	public UUID getUserID(String playerName) {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (player.getName().equalsIgnoreCase(playerName)) {
				return player.getUniqueId();
			}
		}
		return null;
	}

	public List<UUID> getAllUsers() {
		List<UUID> result = new ArrayList<>();
		for (File file : Objects.requireNonNull(ClansAPI.getData().getUserFolder().listFiles())) {
			result.add(UUID.fromString(file.getName().replace(".yml", "")));
		}
		return result;
	}

	public String getPriorityKey(int rankPower) {
		String result = "members";
		switch (rankPower) {
			case 0:
				result = "members";
				break;
			case 1:
				result = "moderators";
				break;
			case 2:
				result = "admins";
				break;
			case 3:
				result = "owner";
				break;
		}
		return result;
	}

	public String getPriorityUpgradeKey(int rankPower) {
		String result = "moderators";
		switch (rankPower) {
			case 0:
				result = "moderators";
				break;
			case 1:
				result = "admins";
				break;
		}
		return result;
	}

	public String getPriorityDowngradeKey(int rankPower) {
		String result = "members";
		switch (rankPower) {
			case 1:
				result = "members";
				break;
			case 2:
				result = "moderators";
				break;
		}
		return result;
	}

	private void createClanFile(String clanID, String name) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		FileConfiguration local = clan.getConfig();
		List<String> members = new ArrayList<>();
		List<String> mods = new ArrayList<>();
		List<String> admins = new ArrayList<>();
		List<String> allies = new ArrayList<>();
		List<String> enemies = new ArrayList<>();
		local.set("name", name);
		local.set("members", members);
		local.set("moderators", mods);
		local.set("admins", admins);
		local.set("allies", allies);
		local.set("enemies", enemies);
		switch (defaultMode().toLowerCase()) {
			case "war":
				local.set("peaceful", false);
				break;
			case "peace":
				local.set("peaceful", true);
				break;
		}
		clan.saveConfig();
		System.out.printf("[%s] - Clan " + '"' + clanID + '"' + " created.%n", ClansPro.getInstance().getDescription().getName());
	}

	private void createClanFile(String clanID, String name, String password) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		FileConfiguration local = clan.getConfig();
		List<String> members = new ArrayList<>();
		List<String> mods = new ArrayList<>();
		List<String> admins = new ArrayList<>();
		List<String> allies = new ArrayList<>();
		List<String> enemies = new ArrayList<>();
		local.set("name", name);
		local.set("password", password);
		local.set("members", members);
		local.set("moderators", mods);
		local.set("admins", admins);
		local.set("allies", allies);
		local.set("enemies", enemies);
		switch (defaultMode().toLowerCase()) {
			case "war":
				local.set("peaceful", false);
				break;
			case "peace":
				local.set("peaceful", true);
				break;
		}
		clan.saveConfig();
		System.out.printf("[%s] - Clan " + '"' + clanID + '"' + " created.%n", ClansPro.getInstance().getDescription().getName());
	}

	public void demotePlayer(UUID target) {
		ClanAction clanAction = new ClanAction();
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(target));
		if (clan.getConfig().getStringList("members").contains(target.toString())) {
			if (clanAction.getRankPower(target) != 3 || clanAction.getRankPower(target) != 0) {
				String currentRank = clanAction.getPriorityKey(clanAction.getRankPower(target));
				List<String> array = clan.getConfig().getStringList(clanAction.getPriorityDowngradeKey(clanAction.getRankPower(target)));
				if (!currentRank.equals("members")) {
					if (!clanAction.getPriorityDowngradeKey(clanAction.getRankPower(target)).equals("members")) {
						array.add(target.toString());
					}
				}
				clan.getConfig().set(clanAction.getPriorityDowngradeKey(clanAction.getRankPower(target)), array);
				List<String> array2 = clan.getConfig().getStringList(currentRank);
				if (!currentRank.equals("members")) {
					array2.remove(target.toString());
				}
				clan.getConfig().set(currentRank, array2);
				clan.saveConfig();
				Clan clanIndex = getClan(getClanID(target));
				String format = MessageFormat.format(ClansAPI.getData().getMessage("demotion"), Bukkit.getOfflinePlayer(target).getName(), getRankTag(getRank(target)));
				clanIndex.broadcast(format);
			}
		}
	}

	public void promotePlayer(UUID target) {
		ClanAction clanAction = new ClanAction();
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(target));
		if (clan.getConfig().getStringList("members").contains(target.toString())) {
			if (clanAction.getRankPower(target) < clanAction.maxRankPower()) {
				String currentRank = clanAction.getPriorityKey(clanAction.getRankPower(target));
				List<String> array = clan.getConfig().getStringList(clanAction.getPriorityUpgradeKey(clanAction.getRankPower(target)));
				List<String> array2 = clan.getConfig().getStringList(currentRank);
				if (!currentRank.equals("members")) {
					array2.remove(target.toString());
				}
				if (!array.contains(target.toString())) {
					array.add(target.toString());
				}
				clan.getConfig().set(clanAction.getPriorityUpgradeKey(clanAction.getRankPower(target)), array);
				clan.getConfig().set(currentRank, array2);
				clan.saveConfig();
				Clan clanIndex = getClan(getClanID(target));
				String format = MessageFormat.format(ClansAPI.getData().getMessage("promotion"), Bukkit.getOfflinePlayer(target).getName(), getRankTag(getRank(target)));
				clanIndex.broadcast(format);
			}
		}
	}

	public void kickPlayer(UUID target) {
		FileManager user = ClansAPI.getData().get(target);
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(target));
		FileConfiguration fc = clan.getConfig();
		List<String> members = fc.getStringList("members");
		List<String> admins = fc.getStringList("admins");
		List<String> moderators = fc.getStringList("moderators");
		if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
			if (Bukkit.getPlayer(target) != null) {
				ScoreTag.remove(Bukkit.getPlayer(target));
			}
		}
		ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(target));
		if (fc.getStringList("members").contains(target.toString())) {
			members.remove(target.toString());
			fc.set("members", members);
		}
		if (fc.getStringList("moderators").contains(target.toString())) {
			moderators.remove(target.toString());
			fc.set("moderators", moderators);
		}
		if (fc.getStringList("admins").contains(target.toString())) {
			admins.remove(target.toString());
			fc.set("admins", admins);
		}
		clan.saveConfig();
		user.getConfig().set("Clan", null);
		user.saveConfig();
	}

	public void teleportBase(Player p) {
		Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
		if (clan.getBase() != null) {
			boolean waiting = false;
			for (Entity e : p.getNearbyEntities(30, 30, 30)) {
				if (e instanceof Player) {
					if (!Arrays.asList(clan.getMembersList()).contains(e.getUniqueId().toString())) {
						waiting = true;
						break;
					}
				}
			}
			if (!waiting) {
				p.teleport(clan.getBase());
			} else {
				sendMessage(p, "&cSomeone is nearby. Teleporting in 10 seconds.");
				Schedule.sync(() -> p.teleport(clan.getBase())).wait(20 * 10);
			}
		}
	}

	public void transferOwner(UUID owner, UUID target) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(owner));
		Clan clanIndex = ClansAPI.getInstance().getClan(owner);
		if (Arrays.asList(clanIndex.getMembersList()).contains(target.toString())) {
			if (getRankPower(owner) == 3) {
				clan.getConfig().set("owner", target.toString());
				clan.saveConfig();
				sendMessage(Bukkit.getPlayer(owner), "&d&oOwnership transferred.. It was a nice run..");
			} else {
				sendMessage(Bukkit.getPlayer(owner), noClearance());
			}
		} else {
			sendMessage(Bukkit.getPlayer(owner), playerUnknown("clan member"));
		}
	}

	/**
	 * @param owner Target player to retrieve id from
	 * @return Gets the specified players clanID
	 * @deprecated Replaced by new {@link ClanAssociate} object.
	 * Use {@link ClanAssociate#getClanID()} instead.
	 */
	@Deprecated
	public String getClanID(UUID owner) {
		FileManager user = ClansAPI.getData().get(owner);
		if (user.exists()) {
			if (user.getConfig().getString("Clan.id") != null) {
				if (user.getConfig().getString("Clan.id").length() != 14) {
					throw new UnsupportedOperationException("[ClansPro] - Clan ID " + user.getConfig().getString("Clan.id") + " invalid, expected format ####-####-####", new Throwable(user.getConfig().getString("Clan.id")));
				}
				FileManager clan = DataManager.FileType.CLAN_FILE.get(user.getConfig().getString("Clan.id"));
				if (!clan.exists()) {
					user.getConfig().set("Clan", null);
					user.saveConfig();
					return null;
				}
				return user.getConfig().getString("Clan.id");
			}
		}
		return null;
	}

	/**
	 * @param target Target to check
	 * @return Gets the rank of the specified player in default format.
	 */
	public String getRank(UUID target) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getClanID(target));
		String rank = "";
		FileConfiguration fc = clan.getConfig();
		if (fc.getStringList("members").contains(target.toString())) {
			rank = "Member";
		}
		if (fc.getStringList("moderators").contains(target.toString())) {
			rank = "Moderator";
		}
		if (fc.getStringList("admins").contains(target.toString())) {
			rank = "Admin";
		}
		if (Objects.equals(fc.getString("owner"), target.toString())) {
			rank = "Owner";
		}
		return rank;
	}

	/**
	 * @param rank The default rank to check
	 * @return Gets the configured rank tag for the specifed default rank
	 * See getRank(Player p) for online usage.
	 */
	public String getRankTag(String rank) {
		String result = "";
		FileManager main = ClansAPI.getData().getMain();
		String member = main.getConfig().getString("Formatting.Styles.Full.Member");
		String mod = main.getConfig().getString("Formatting.Styles.Full.Moderator");
		String admin = main.getConfig().getString("Formatting.Styles.Full.Admin");
		String owner = main.getConfig().getString("Formatting.Styles.Full.Owner");
		switch (rank) {
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

	public int getRankPower(UUID target) {
		return getRankPriority(getRank(target)).toInt();
	}

	public int maxRankPower() {
		return 2;
	}

	public RankPriority getRankPriority(String rank) {
		RankPriority priority = null;
		switch (rank.toLowerCase()) {
			case "owner":
				priority = RankPriority.HIGHEST;
				break;
			case "admin":
				priority = RankPriority.HIGHER;
				break;
			case "moderator":
				priority = RankPriority.HIGH;
				break;
			case "member":
				priority = RankPriority.NORMAL;
				break;
		}
		return priority;
	}

	/**
	 * @param clanID Target clan to check
	 * @return Gets the clan tag from the specified clan.
	 */
	public String getClanTag(String clanID) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		return clan.getConfig().getString("name");
	}

	public Clan getClan(String clanID) {
		Clan clan = null;
		for (Clan c : ClansPro.getInstance().dataManager.CLANS) {
			if (c.getId().toString().equals(clanID)) {
				clan = c;
			}
		}
		return clan;
	}

	/**
	 * @param clanID       Primary clan
	 * @param targetClanID Target clan
	 * @return Gets the relation color in color code format for the two clans.
	 */
	public String clanRelationColor(String clanID, String targetClanID) {
		String result = "&f&o";
		Clan a = ClansAPI.getInstance().getClan(clanID);
		try {
			if (getAllClanIDs().contains(targetClanID)) {
				if (a.isNeutral(targetClanID)) {
					result = "&f";
				}
				if (clanID.equals(targetClanID)) {
					result = "&6";
				}
				if (a.getAllyList().contains(targetClanID)) {
					result = "&a";
				}
				if (a.getEnemyList().contains(targetClanID)) {
					result = "&c";
				}
			}
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	public void getMyClanInfo(Player target, int page) {
		String clanID = getClanID(target.getUniqueId());
		Clan clanIndex = ClansAPI.getInstance().getClan(target.getUniqueId());
		FileManager clan = DataManager.FileType.CLAN_FILE.get(clanID);
		List<String> array = new ArrayList<>();
		String owner = clan.getConfig().getString("owner");
		String password = clan.getConfig().getString("password");
		List<String> members = clan.getConfig().getStringList("members");
		List<String> mods = clan.getConfig().getStringList("moderators");
		List<String> admins = clan.getConfig().getStringList("admins");
		List<String> allies = clan.getConfig().getStringList("allies");
		List<String> allyRequests = clan.getConfig().getStringList("ally-requests");
		List<String> enemies = clan.getConfig().getStringList("enemies");
		String c = clanIndex.getColor();
		if (c.equals("&f"))
			c = "&6";
		array.add(" ");
		array.add("&6&lClan&7: &f" + clanIndex.getColor() + getClanTag(clanID));
		array.add("&f&m---------------------------");
		array.add("&6Description: &7" + clanIndex.getDescription());
		array.add("&6" + getRankTag("Owner") + ": &f" + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName());
		if (password == null)
			password = "NO PASS";
		if (clanIndex.getBase() != null)
			array.add("&6Base: &aSet");
		if (clanIndex.getBase() == null)
			array.add("&6Base: &7Not set");
		if (clanIndex.isPeaceful())
			array.add("&6Mode: &f&lPEACE");
		if (!clanIndex.isPeaceful())
			array.add("&6Mode: &4&lWAR");
		array.add("&6Color: " + clanIndex.getColor() + clanIndex.getColor().replace("&", "&f»" + clanIndex.getColor()));
		if (getRankPower(target.getUniqueId()) >= passwordClearance()) {
			array.add("&6Password: &f" + password);
		}
		array.add("&6&lPower [&e" + clanIndex.format(String.valueOf(clanIndex.getPower())) + "&6&l]");
		if (Bukkit.getPluginManager().isPluginEnabled("Enterprise") || Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			array.add("&6&lBank [&e" + clanIndex.format(String.valueOf(clanIndex.getBalance().doubleValue())) + "&6&l]");
		}
		array.add("&6" + getRankTag("Admin") + "s [" + c + admins.size() + "&6]");
		array.add("&6" + getRankTag("Moderator") + "s [" + c + mods.size() + "&6]");
		array.add("&6Claims [" + c + clanIndex.getOwnedClaimsList().length + "&f/ " + c + clanIndex.getMaxClaims() + "&6]");
		array.add("&f&m---------------------------");
		if (allyRequests.size() > 0) {
			array.add("&6Ally Requests [" + c + allyRequests.size() + "&6]");
			for (String clanId : allyRequests) {
				array.add("&f- &e&o" + getClanTag(clanId));
			}
		}
		if (allyRequests.isEmpty())
			array.add("&6Ally Requests [" + c + 0 + "&6]");
		if (allies.size() > 0) {
			array.add("&6Allies [" + c + allies.size() + "&6]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + getClanTag(clanId));
			}
		}
		for (String clanId : getAllClanIDs()) {
			if (clanIndex.getEnemyList().contains(clanID)) {
				enemies.add(clanId);
			}
		}
		if (allies.isEmpty())
			array.add("&6Allies [" + c + 0 + "&6]");
		if (enemies.size() > 0) {
			array.add("&6Enemies [" + c + enemies.size() + "&6]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + getClanTag(clanId));
			}
		}
		if (enemies.isEmpty())
			array.add("&6Enemies [" + c + 0 + "&6]");
		array.add("&f&m---------------------------");
		array.add("&n" + getRankTag("Member") + "s&r [" + c + members.size() + "&r]");
		ClanInformationAdaptEvent event = new ClanInformationAdaptEvent(array, clanID);
		Bukkit.getPluginManager().callEvent(event);
		printArray(target, event.getInsertions());
		paginatedMemberList(target, members, page);
		target.sendMessage(" ");
	}

	/**
	 * @param clanName Target clan to check
	 * @return Gets the ID of a specified clan by name.
	 */
	public String getClanID(String clanName) {
		String result = null;
		for (String ID : getAllClanIDs()) {
			FileManager clan = DataManager.FileType.CLAN_FILE.get(ID);
			if (Objects.equals(clan.getConfig().getString("name"), clanName)) {
				result = ID;
				break;
			}
		}
		return result;
	}

	public void forfeitWar(Player p, String clanID) {
		DefaultClan c = (DefaultClan) DefaultClan.action.getClan(clanID);
		if (c.getCurrentWar() != null) {
			DefaultClan winner = (DefaultClan) c.getCurrentWar().getTargeted();
			Bukkit.broadcastMessage(DefaultClan.action.color("&c&o" + c.getName() + " &f&lFORFEIT &c&ofrom member &4" + p.getName()));
			Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName()));
			winner.givePower((c.getPower() / 2) + winner.getCurrentWar().getPoints());
			String loserC;
			String winnerC;
			if (c.getCurrentWar().isRed()) {
				loserC = "&c";
				winnerC = "&9";
			} else {
				loserC = "&9";
				winnerC = "&c";
			}
			for (Player par : winner.getCurrentWar().getParticipants()) {
				par.giveExp((winner.getCurrentWar().getPoints() * 2));
				final boolean success;
				Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(18.14 * winner.getCurrentWar().getPoints()), par);

				success = opt.orElse(false);
				par.sendTitle(color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + c.getCurrentWar().getPoints()), color("&aWe win."), 10, 45, 10);

			}
			c.takePower((c.getPower() / 2) + winner.getCurrentWar().getPoints());
			for (Player par : c.getCurrentWar().getParticipants()) {
				par.giveExp((c.getCurrentWar().getPoints() * 2));
				final boolean success;
				Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(10.14 * c.getCurrentWar().getPoints()), par);

				success = opt.orElse(false);
				par.sendTitle(color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + c.getCurrentWar().getPoints()), color("&cWe lose."), 10, 45, 10);

			}
			ClanCooldown.remove(c.getCurrentWar().getArenaTimer());
			c.getCurrentWar().conclude();
		}
	}

	/**
	 * @return Gets a list of all saved clans by name
	 */
	public List<String> getAllClanNames() {
		List<String> array = new ArrayList<>();
		for (String clan : getAllClanIDs()) {
			FileManager c = DataManager.FileType.CLAN_FILE.get(clan);
			array.add(c.getConfig().getString("name"));
		}
		return array;
	}

	/**
	 * @return Gets a list of all saved clans by clanID
	 */
	public List<String> getAllClanIDs() {
		DataManager dm = new DataManager();
		List<String> array = new ArrayList<>();
		for (File file : Objects.requireNonNull(dm.getClanFolder().listFiles())) {
			array.add(file.getName().replace(".yml", ""));
		}
		return array;
	}


	private void printArray(Player p, List<String> list) {
		for (String l : list) {
			p.sendMessage(color(l));
		}
	}

	public boolean overPowerBypass() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getBoolean("Clans.raid-shield.claiming");
	}

	public int modeChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.mode-clearance");
	}

	public int tagChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.rename-clearance");
	}

	public int descriptionChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.description-clearance");
	}

	public int colorChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.recolor-clearance");
	}

	public int friendlyFireClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.friendly-fire.clearance");
	}

	public int positionClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.position-clearance");
	}

	public int unclaimAllClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.land-claiming.unclaim-all-clearance");
	}

	public int claimingClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.land-claiming.clearance");
	}

	public int invitationClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.invite-clearance");
	}

	public int baseClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.base-clearance");
	}

	public int kickClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.kick-clearance");

	}

	public int passwordClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getConfig().getInt("Clans.password-clearance");

	}

	private String clanCode() {
		// Triple check, The same clan ID must never co-exist.
		HUID code = HUID.randomID();
		if (getAllClanIDs().contains(code.toString())) {
			code = HUID.randomID();
			if (getAllClanIDs().contains(code.toString())) {
				code = HUID.randomID();
				if (getAllClanIDs().contains(code.toString())) {
					code = HUID.randomID();
				}
			}
		}
		return code.toString();
	}

	public boolean isNight(String world, int on, int off) {
		Server server = Bukkit.getServer();
		long time = 0;
		try {
			time = Objects.requireNonNull(server.getWorld(world)).getTime();
		} catch (NullPointerException e) {
			ClansPro.getInstance().getLogger().severe("- World not found in configuration. Raid-shield will not work properly.");
		}

		return time <= on || time >= off;
	}

	public void playerBoard(Player p, int page) {
		List<String> array = new ArrayList<>();
		DefaultClan clan = null;
		for (UUID id : getAllUsers()) {
			array.add(Bukkit.getOfflinePlayer(id).getName());
		}
		Collections.sort(array);
		PaginatedAssortment players = new PaginatedAssortment(p, array);
		if (Bukkit.getVersion().contains("1.16")) {
			players.setListTitle("&7&m------------&7&l[&6&oPlayer List&7&l]&7&m------------")
					.setNormalText("&aI am ")
					.setHoverText("&#787674%s")
					.setHoverTextMessage("&7Click to view &6%s &7player information")
					.setListBorder("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		} else {
			players.setListTitle("&7&m------------&7&l[&6&oPlayer List&7&l]&7&m------------")
					.setNormalText("&aI am ")
					.setHoverText("&b%s")
					.setHoverTextMessage("&7Click to view &6%s &7player information")
					.setListBorder("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		}
		players.setLinesPerPage(10)
				.setNavigateCommand("c players")
				.setCommandToRun("c info %s")
				.exportFancy(page);
	}

	public List<Clan> getTop() {
		List<Clan> c = new ArrayList<>(ClansAPI.getData().CLANS);
		c.sort(Comparator.comparingDouble(Clan::getPower));
		Collections.reverse(c);
		return Collections.unmodifiableList(c);
	}

	public void getLeaderboard(LeaderboardType type, Player p, int pageNum) {
		switch (type) {
			case MONEY:
				if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					getLeaderboard(LeaderboardType.POWER, p, pageNum);
					break;
				}

				PaginatedList<Clan> help = new PaginatedList<>(new ArrayList<>(ClansAPI.getData().CLANS))
						.limit(menuSize())
						.compare((o1, o2) -> Double.compare(o2.getBalanceDouble(), o1.getBalanceDouble()))
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oMost Money&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oMost Money&7&l]&7&m------------");
							}
						});

				help.finish((pagination, page, max) -> {
					Message.form(p).send("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					TextLib component = TextLib.getInstance();
					int next = page + 1;
					int last = Math.max(page - 1, 1);
					List<BaseComponent> toSend = new LinkedList<>();
					if (page == 1) {
						if (page == max) {
							toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
							toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
							toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
							p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
							return;
						}
						toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (page == max) {
						toSend.add(component.execute(() -> help.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (next <= max) {
						toSend.add(component.execute(() -> help.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
					}
				}).decorate((pagination, clan, page, max, placement) -> {
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + clan.format(String.valueOf(pagination.format(clan.getBalanceDouble(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + clan.format(String.valueOf(pagination.format(clan.getBalanceDouble(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
			case WINS:

				PaginatedList<Clan> help2 = new PaginatedList<>(new ArrayList<>(ClansAPI.getData().CLANS))
						.limit(menuSize())
						.compare((o1, o2) -> Integer.compare(o2.getWins(), o1.getWins()))
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oMost Wins&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oMost Wins&7&l]&7&m------------");
							}
						});

				help2.finish((pagination, page, max) -> {
					Message.form(p).send("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					TextLib component = TextLib.getInstance();
					int next = page + 1;
					int last = Math.max(page - 1, 1);
					List<BaseComponent> toSend = new LinkedList<>();
					if (page == 1) {
						if (page == max) {
							toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
							toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
							toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
							p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
							return;
						}
						toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help2.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (page == max) {
						toSend.add(component.execute(() -> help2.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (next <= max) {
						toSend.add(component.execute(() -> help2.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help2.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
					}
				}).decorate((pagination, clan, page, max, placement) -> {
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + clan.getWins(), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + clan.getWins(), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
			case POWER:
				PaginatedList<Clan> help4 = new PaginatedList<>(new ArrayList<>(ClansAPI.getData().CLANS))
						.limit(menuSize())
						.compare((o1, o2) -> Double.compare(o2.getPower(), o1.getPower()))
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oMost Power&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oMost Power&7&l]&7&m------------");
							}
						});

				help4.finish((pagination, page, max) -> {
					Message.form(p).send("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					TextLib component = TextLib.getInstance();
					int next = page + 1;
					int last = Math.max(page - 1, 1);
					List<BaseComponent> toSend = new LinkedList<>();
					if (page == 1) {
						if (page == max) {
							toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
							toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
							toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
							p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
							return;
						}
						toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help4.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (page == max) {
						toSend.add(component.execute(() -> help4.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (next <= max) {
						toSend.add(component.execute(() -> help4.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help4.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
					}
				}).decorate((pagination, clan, page, max, placement) -> {
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + clan.format(String.valueOf(pagination.format(clan.getPower(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + clan.format(String.valueOf(pagination.format(clan.getPower(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
			case KILLS:
				PaginatedList<Clan> help3 = new PaginatedList<>(new ArrayList<>(ClansAPI.getData().CLANS))
						.limit(menuSize())
						.compare((o1, o2) -> {
							double kd1 = 0;
							for (ClanAssociate associate : o1.getMembers().list()) {
								kd1 += associate.getKD();
							}
							double kd2 = 0;
							for (ClanAssociate associate : o2.getMembers().list()) {
								kd2 += associate.getKD();
							}
							return Double.compare(kd2, kd1);
						})
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oHighest K/D&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oHighest K/D&7&l]&7&m------------");
							}
						});

				help3.finish((pagination, page, max) -> {
					Message.form(p).send("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					TextLib component = TextLib.getInstance();
					int next = page + 1;
					int last = Math.max(page - 1, 1);
					List<BaseComponent> toSend = new LinkedList<>();
					if (page == 1) {
						if (page == max) {
							toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
							toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
							toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
							p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
							return;
						}
						toSend.add(component.textHoverable("", "&8« ", "&cYou are on the first page already."));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help3.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (page == max) {
						toSend.add(component.execute(() -> help3.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.textHoverable("", " &8»", "&cYou are already on the last page."));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (next <= max) {
						toSend.add(component.execute(() -> help3.get(last), component.textHoverable("", "&3« ", "&aGoto the previous page.")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> help3.get(next), component.textHoverable("", " &3»", "&aGoto the next page.")));
						p.spigot().sendMessage(toSend.toArray(new BaseComponent[0]));
					}
				}).decorate((pagination, clan, page, max, placement) -> {
					double kd = 0;
					for (ClanAssociate associate : clan.getMembers().list()) {
						kd += associate.getKD();
					}
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + pagination.format(kd, 2), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + pagination.format(kd, 2), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
		}
	}

	public enum LeaderboardType {
		MONEY, WINS, POWER, KILLS
	}

}
