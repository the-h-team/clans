package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.CooldownArena;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.command.ClanInformationAdaptEvent;
import com.github.sanctum.clans.events.core.ClanCreateEvent;
import com.github.sanctum.clans.events.core.ClanCreatedEvent;
import com.github.sanctum.clans.events.core.ClanLeaveEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanAction extends StringLibrary {

	public final CooldownArena ARENA = new CooldownArena();

	private final ClansAPI API = ClansAPI.getInstance();

	public void create(UUID owner, String clanName, String password) {
		FileManager user = ClansAPI.getData().get(owner);
		if (API.getClanID(owner) == null) {
			if (clanName.length() > ClansAPI.getData().getMain().getConfig().getInt("Formatting.tag-size")) {
				if (Bukkit.getPlayer(owner) != null) {
					sendMessage(Bukkit.getPlayer(owner), ClansAPI.getData().getMessage("too-long"));
				}
				return;
			}
			ClanCreateEvent e = ClanVentBus.call(new ClanCreateEvent(owner, clanName, password));
			if (!e.isCancelled()) {
				FileConfiguration local = user.getConfig();
				String newID = generateCleanClanCode();
				local.set("Clan.join-date", new Date().getTime());
				user.saveConfig();
				String status = "OPEN";
				if (password == null) {
					String format = MessageFormat.format(ClansAPI.getData().getMessage("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				} else {
					status = "LOCKED";
					String format = MessageFormat.format(ClansAPI.getData().getMessage("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				}
				DefaultClan instance = new DefaultClan(newID);
				instance.setName(clanName);
				boolean war = ClansAPI.getData().getMain().getConfig().getString("Clans.mode-change.default").equalsIgnoreCase("peace");
				instance.setPeaceful(war);
				if (password != null) {
					instance.setPassword(password);
				}
				instance.getMembers().add(new ClanAssociate(owner, RankPriority.HIGHEST, HUID.fromString(newID)));
				instance.save();
				API.getClanManager().load(instance);
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					ClanDisplayName.set(Bukkit.getPlayer(owner), ClansAPI.getData().prefixedTag(API.getClan(API.getClanID(clanName)).getColor(), clanName));
				}
				ClanVentBus.call(new ClanCreatedEvent(owner, clanName));
			}
		} else {
			if (Bukkit.getPlayer(owner) != null) {
				sendMessage(Bukkit.getPlayer(owner), alreadyInClan());
			}
		}
	}

	private String generateCleanClanCode() {
		// Triple check, The same clan ID must never co-exist.
		HUID code = HUID.randomID();
		List<String> allids = getAllClanIDs();
		for (int i = 0; i < 3; i++) {
			if (allids.contains(code.toString())) {
				code = HUID.randomID();
			}
		}
		return code.toString();
	}

	public void removePlayer(UUID target) {
		ClanAssociate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			if (!(associate.getClan() instanceof DefaultClan))
				return;

			DefaultClan clanIndex = (DefaultClan) associate.getClan();

			ClanLeaveEvent ev = ClanVentBus.call(new ClanLeaveEvent(associate));

			if (ev.isCancelled()) {
				return;
			}

			FileManager clan = ClansAPI.getData().getClanFile(clanIndex);
			FileManager user = ClansAPI.getData().get(target);
			if (associate.getPlayer().isOnline()) {
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					ClanDisplayName.remove(associate.getPlayer().getPlayer());
				}
			}
			switch (associate.getPriority()) {
				case HIGHEST:
					for (ClanAssociate s : clanIndex.getMembers()) {
						kickPlayer(s.getPlayer().getUniqueId());
					}
					for (String ally : clanIndex.getAllyList()) {
						Clan a = API.getClan(ally);
						a.removeAlly(clanIndex.getId());
					}
					for (String enemy : clanIndex.getEnemyList()) {
						Clan a = API.getClan(enemy);
						a.removeEnemy(clanIndex.getId());
					}
					FileManager regions = API.getClaimManager().getFile();
					regions.getConfig().set(API.getClanID(target).toString(), null);
					regions.saveConfig();
					String clanName = clan.getConfig().getString("name");
					for (String s : associate.getClan().getDataKeys()) {
						associate.getClan().removeValue(s);
					}
					API.getClanManager().delete(clanIndex);
					user.getConfig().set("Clan", null);
					user.saveConfig();
					String format = MessageFormat.format(ClansAPI.getData().getMessage("deletion"), clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
					API.getClaimManager().refresh();
					break;
				case HIGHER:
				case NORMAL:
				case HIGH:
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
					user.getConfig().set("Clan", null);
					user.saveConfig();
					Schedule.sync(associate::kick).run();
					break;
			}
		} else {
			if (Bukkit.getPlayer(target) != null) {
				sendMessage(Bukkit.getPlayer(target), notInClan());
			}
		}
	}

	public void joinClan(UUID target, String clanName, String password) {
		ClanAssociate associate = API.getAssociate(target).orElse(null);
		if (associate == null) {
			Clan c = API.getClan(API.getClanID(clanName));

			if (c == null) return;

			if (!getAllClanNames().contains(clanName)) {
				if (Bukkit.getOfflinePlayer(target).isOnline()) {
					sendMessage(Bukkit.getPlayer(target), clanUnknown(clanName));
				}
				return;
			}
			if (c.getPassword() == null) {
				FileManager user = ClansAPI.getData().get(target);
				user.getConfig().set("Clan.join-date", new Date().getTime());
				user.saveConfig();
				Clan clanIndex = API.getClan(API.getClanID(clanName));
				clanIndex.getMembers().add(new ClanAssociate(target, RankPriority.NORMAL, clanIndex.getId()));
				clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-join"), Bukkit.getOfflinePlayer(target).getName()));
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						ClanDisplayName.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getData().prefixedTag(API.getClan(API.getClanID(clanName)).getColor(), clanName));
					}
				}
				return;
			}
			if (c.getPassword().equals(password)) {
				FileManager user = ClansAPI.getData().get(target);
				user.getConfig().set("Clan.join-date", new Date().getTime());
				user.saveConfig();
				Clan clanIndex = API.getClan(API.getClanID(clanName));
				clanIndex.getMembers().add(new ClanAssociate(target, RankPriority.NORMAL, clanIndex.getId()));
				clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("member-join"), Bukkit.getOfflinePlayer(target).getName()));
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						ClanDisplayName.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getData().prefixedTag(API.getClan(API.getClanID(clanName)).getColor(), clanName));
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
			if (playerName.equals(player.getName())) {
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

	public void demotePlayer(UUID target) {
		ClanAssociate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {

			if (associate.getPriority().toInt() < 3) {
				switch (associate.getPriority().toInt()) {
					case 2:
						associate.setPriority(RankPriority.HIGH);
						break;
					case 1:
						associate.setPriority(RankPriority.NORMAL);
						break;
				}
			}
			Clan clanIndex = associate.getClan();
			String format = MessageFormat.format(ClansAPI.getData().getMessage("demotion"), Bukkit.getOfflinePlayer(target).getName(), associate.getRankTag());
			clanIndex.broadcast(format);
		}
	}

	public void promotePlayer(UUID target) {
		ClanAssociate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {

			if (associate.getPriority().toInt() < 2) {
				switch (associate.getPriority().toInt()) {
					case 0:
						associate.setPriority(RankPriority.HIGH);
						break;
					case 1:
						associate.setPriority(RankPriority.HIGHER);
						break;
				}
			}
			Clan clanIndex = API.getClan(API.getClanID(target).toString());
			String format = MessageFormat.format(ClansAPI.getData().getMessage("demotion"), Bukkit.getOfflinePlayer(target).getName(), associate.getRankTag());
			clanIndex.broadcast(format);
		}
	}

	public void kickPlayer(UUID target) {
		FileManager user = ClansAPI.getData().get(target);
		ClanAssociate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			if (ClansAPI.getData().prefixedTagsAllowed()) {
				if (Bukkit.getPlayer(target) != null) {
					ClanDisplayName.remove(Bukkit.getPlayer(target));
				}
			}
			Schedule.sync(() -> associate.getClan().getMembers().remove(associate)).wait(1);
			user.getConfig().set("Clan", null);
			user.saveConfig();
		}
	}

	public void teleportBase(Player p) {
		Clan clan = API.getClan(p.getUniqueId());
		if (clan != null) {
			Location base = clan.getBase();
			if (base != null) {
				boolean waiting = false;
				for (Entity e : p.getNearbyEntities(30, 30, 30)) {
					if (e instanceof Player) {
						if (!Arrays.asList(clan.getMemberIds()).contains(e.getUniqueId().toString())) {
							waiting = true;
							break;
						}
					}
				}
				if (!waiting) {
					sendMessage(p, commandBase());
					p.teleport(base);
				} else {
					sendMessage(p, "&cSomeone is nearby. Teleporting in 10 seconds.");
					Schedule.sync(() -> p.teleport(base)).wait(20 * 10);
				}
			} else {
				sendMessage(p, "&cOur clan has no base set.");
			}
		}
	}

	public int maxRankPower() {
		return 2;
	}

	/**
	 * Get the relation color between two clans.
	 *
	 * @param clanID       Primary clan
	 * @param targetClanID Target clan
	 * @return Gets the relation color in color code format for the two clans.
	 */
	public String getRelationColor(String clanID, String targetClanID) {
		String result = "&f&o";
		Clan a = API.getClan(clanID);
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

	/**
	 * Get the relation color between two clans.
	 *
	 * @param a Primary clan
	 * @param b Target clan
	 * @return Gets the relation color in color code format for the two clans.
	 */
	public String getRelationColor(Clan a, Clan b) {
		String result = "&f&o";
		try {
			if (getAllClanIDs().contains(b.getId().toString())) {
				if (a.isNeutral(b.getId().toString())) {
					result = "&f";
				}
				if (a.getId().equals(b.getId())) {
					result = "&6";
				}
				if (a.getAllyList().contains(b.getId().toString())) {
					result = "&a";
				}
				if (a.getEnemyList().contains(b.getId().toString())) {
					result = "&c";
				}
			}
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	public void forfeitWar(ClanAssociate associate) {
		DefaultClan c = API.getClanManager().cast(DefaultClan.class, associate.getClan());
		if (c == null) return;
		if (c.getCurrentWar() != null) {
			DefaultClan winner = (DefaultClan) c.getCurrentWar().getTargeted();
			Bukkit.broadcastMessage(Clan.ACTION.color("&c&o" + c.getName() + " &f&lFORFEIT &c&ofrom member &4" + associate.getName()));
			Bukkit.broadcastMessage(Clan.ACTION.color(Clan.ACTION.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName()));
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
				EconomyProvision.getInstance().deposit(BigDecimal.valueOf(18.14 * winner.getCurrentWar().getPoints()), par);
				par.sendTitle(color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + c.getCurrentWar().getPoints()), color("&aWe win."), 10, 45, 10);

			}
			c.takePower((c.getPower() / 2) + winner.getCurrentWar().getPoints());
			for (Player par : c.getCurrentWar().getParticipants()) {
				par.giveExp((c.getCurrentWar().getPoints() * 2));
				EconomyProvision.getInstance().deposit(BigDecimal.valueOf(10.14 * c.getCurrentWar().getPoints()), par);
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

	public boolean isNight(@NotNull World world, int on, int off) {
		long time = 0;
		try {
			time = world.getTime();
		} catch (NullPointerException e) {
			API.getPlugin().getLogger().severe("- World not found in configuration. Raid-shield will not work properly.");
		}

		return time <= on || time >= off;
	}

	public List<Clan> getMostPowerful() {
		List<Clan> c = ClansAPI.getInstance().getClanManager().getClans().list();
		c.sort(Comparator.comparingDouble(Clan::getPower));
		Collections.reverse(c);
		return Collections.unmodifiableList(c);
	}

	public void getClanboard(Player target, int page) {
		ClanAssociate associate = API.getAssociate(target).orElse(null);

		if (associate == null) return;

		Clan clan = associate.getClan();

		List<String> array = new ArrayList<>();

		String owner = clan.getOwner().getPlayer().getName();
		String password = clan.getPassword();

		List<String> members = clan.getMembers().stream().filter(a -> a.getPriority().toInt() == 0).map(ClanAssociate::getPlayer).map(OfflinePlayer::getUniqueId).map(UUID::toString).collect(Collectors.toList());
		List<String> mods = clan.getMembers().stream().filter(a -> a.getPriority().toInt() == 1).map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList());
		List<String> admins = clan.getMembers().stream().filter(a -> a.getPriority().toInt() == 2).map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList());
		List<String> allies = clan.getAllies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
		List<String> allyRequests = clan.getAllyRequests();
		List<String> enemies = clan.getEnemies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());

		String c = clan.getColor();

		if (c.equals("&f"))
			c = "&6";
		array.add(" ");
		array.add("&6&lClan&7: &f" + clan.getColor() + clan.getName());
		array.add("&f&m---------------------------");
		array.add("&6Description: &7" + clan.getDescription());
		array.add("&6" + associate.getRankTag() + ": &f" + owner);
		if (password == null)
			password = "NO PASS";
		if (clan.getBase() != null)
			array.add("&6Base: &aSet");
		if (clan.getBase() == null)
			array.add("&6Base: &7Not set");
		if (clan.isPeaceful())
			array.add("&6Mode: &f&lPEACE");
		if (!clan.isPeaceful())
			array.add("&6Mode: &4&lWAR");
		array.add("&6Color: " + clan.getColor() + clan.getColor().replace("&", "&f»" + clan.getColor()));
		if (associate.getPriority().toInt() >= passwordClearance()) {
			array.add("&6Password: &f" + password);
		}
		array.add("&6&lPower [&e" + clan.format(String.valueOf(clan.getPower())) + "&6&l]");
		if (Bukkit.getPluginManager().isPluginEnabled("Enterprise") || Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			array.add("&6&lBank [&e" + clan.format(String.valueOf(clan.getBalance().doubleValue())) + "&6&l]");
		}
		array.add("&6" + ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Styles.Full.Admin") + "s [" + c + admins.size() + "&6]");
		array.add("&6" + ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Styles.Full.Moderator") + "s [" + c + mods.size() + "&6]");
		array.add("&6Claims [" + c + clan.getOwnedClaimsList().length + "&f/ " + c + clan.getMaxClaims() + "&6]");
		array.add("&f&m---------------------------");
		if (allyRequests.size() > 0) {
			array.add("&6Ally Requests [" + c + allyRequests.size() + "&6]");
			for (String clanId : allyRequests) {
				array.add("&f- &e&o" + API.getClanName(clanId));
			}
		}
		if (allyRequests.isEmpty())
			array.add("&6Ally Requests [" + c + 0 + "&6]");
		if (allies.size() > 0) {
			array.add("&6Allies [" + c + allies.size() + "&6]");
			for (String clanId : allies) {
				array.add("&f- &e&o" + API.getClanName(clanId));
			}
		}
		if (allies.isEmpty())
			array.add("&6Allies [" + c + 0 + "&6]");
		if (enemies.size() > 0) {
			array.add("&6Enemies [" + c + enemies.size() + "&6]");
			for (String clanId : enemies) {
				array.add("&f- &c&o" + API.getClanName(clanId));
			}
		}
		if (enemies.isEmpty())
			array.add("&6Enemies [" + c + 0 + "&6]");
		array.add("&f&m---------------------------");
		array.add("&n" + ClansAPI.getData().getMain().getConfig().getString("Formatting.Chat.Styles.Full.Member") + "s&r [" + c + members.size() + "&r]");
		ClanInformationAdaptEvent event = ClanVentBus.call(new ClanInformationAdaptEvent(array, clan.getId().toString()));
		for (String l : event.getInsertions()) {
			target.sendMessage(color(l));
		}
		getMemberboard(target, members, page);
		target.sendMessage(" ");
	}

	public void getPlayerboard(Player p, int page) {
		List<String> array = new ArrayList<>();
		Message msg = Message.form(p);
		new PaginatedList<>(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList())).limit(10)
				.finish(b -> b.setPage(page).setPlayer(p).setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setSuffix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))
				.start((pagination, page1, max) -> msg.send("&7&m------------&7&l[&6&oPlayer List&7&l]&7&m------------"))
				.compare(String::compareTo)
				.decorate((pagination, object, page1, max, placement) -> msg.build(TextLib.getInstance().textRunnable("&aI am", " &6" + object, "&7Click toview my clan info.", "c info " + object)));
	}

	public void getLeaderboard(LeaderboardType type, Player p, int pageNum) {
		switch (type) {
			case MONEY:
				if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					getLeaderboard(LeaderboardType.POWER, p, pageNum);
					break;
				}

				PaginatedList<Clan> help = new PaginatedList<>(new ArrayList<>(ClansAPI.getInstance().getClanManager().getClans().list()))
						.limit(menuSize())
						.compare((o1, o2) -> Double.compare(o2.getBalanceDouble(), o1.getBalanceDouble()))
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oMost Money&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oMost Money&7&l]&7&m------------");
							}
						});

				help.finish(builder -> builder.setPlayer(p)
						.setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")).decorate((pagination, clan, page, max, placement) -> {
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + clan.format(String.valueOf(pagination.format(clan.getBalanceDouble(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + clan.format(String.valueOf(pagination.format(clan.getBalanceDouble(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
			case WINS:

				PaginatedList<Clan> help2 = new PaginatedList<>(new ArrayList<>(ClansAPI.getInstance().getClanManager().getClans().list()))
						.limit(menuSize())
						.compare((o1, o2) -> Integer.compare(o2.getWins(), o1.getWins()))
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oMost Wins&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oMost Wins&7&l]&7&m------------");
							}
						});

				help2.finish((builder -> builder.setPlayer(p)
						.setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))).decorate((pagination, clan, page, max, placement) -> {
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + clan.getWins(), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + clan.getWins(), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
			case POWER:
				PaginatedList<Clan> help4 = new PaginatedList<>(new ArrayList<>(ClansAPI.getInstance().getClanManager().getClans().list()))
						.limit(menuSize())
						.compare((o1, o2) -> Double.compare(o2.getPower(), o1.getPower()))
						.start((pagination, page, max) -> {
							if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
								Message.form(p).send("&7&m------------&7&l[&#ff7700&oMost Power&7&l]&7&m------------");
							} else {
								Message.form(p).send("&7&m------------&7&l[&6&oMost Power&7&l]&7&m------------");
							}
						});

				help4.finish((builder -> builder.setPlayer(p)
						.setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))).decorate((pagination, clan, page, max, placement) -> {
					if (Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.16")) {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " #787674# #0eaccc&l" + placement + " #00fffb&o" + clan.getName() + " #787674: #ff7700&l" + clan.format(String.valueOf(pagination.format(clan.getPower(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					} else {
						Message.form(p).build(TextLib.getInstance().textRunnable("", " &7# &3&l" + placement + " &b&o" + clan.getName() + " &7: &6&l" + clan.format(String.valueOf(pagination.format(clan.getPower(), 2))), "&6Click to view &3&l" + clan.getName() + "'s &6info.", "c info " + clan.getName()));
					}
				}).get(pageNum);
				break;
			case KILLS:
				PaginatedList<Clan> help3 = new PaginatedList<>(new ArrayList<>(ClansAPI.getInstance().getClanManager().getClans().list()))
						.limit(menuSize())
						.compare((o1, o2) -> {
							double kd1 = 0;
							for (ClanAssociate associate : o1.getMembers()) {
								kd1 += associate.getKD();
							}
							double kd2 = 0;
							for (ClanAssociate associate : o2.getMembers()) {
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

				help3.finish((builder -> builder.setPlayer(p)
						.setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))).decorate((pagination, clan, page, max, placement) -> {
					double kd = 0;
					for (ClanAssociate associate : clan.getMembers()) {
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
