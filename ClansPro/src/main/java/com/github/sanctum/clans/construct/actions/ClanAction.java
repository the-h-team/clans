package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.StashesAddon;
import com.github.sanctum.clans.bridge.internal.VaultsAddon;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Permission;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.command.ClanInformationAdaptEvent;
import com.github.sanctum.clans.events.core.ClanCreateEvent;
import com.github.sanctum.clans.events.core.ClanCreatedEvent;
import com.github.sanctum.clans.events.core.ClanLeaveEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.File;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanAction extends StringLibrary {

	private final ClansAPI API = ClansAPI.getInstance();

	public void create(UUID owner, String clanName, String password) {
		if (API.getClanID(owner) == null) {
			if (clanName.length() > ClansAPI.getData().getMain().read(c -> c.getInt("Formatting.tag-size"))) {
				if (Bukkit.getPlayer(owner) != null) {
					sendMessage(Bukkit.getPlayer(owner), ClansAPI.getData().getMessageResponse("too-long"));
				}
				return;
			}
			ClanCreateEvent e = ClanVentBus.call(new ClanCreateEvent(owner, clanName, password));
			if (!e.isCancelled()) {
				String status = "OPEN";
				if (password == null) {
					String format = MessageFormat.format(ClansAPI.getData().getMessageResponse("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				} else {
					status = "LOCKED";
					String format = MessageFormat.format(ClansAPI.getData().getMessageResponse("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				}
				String newID = generateCleanClanCode();
				DefaultClan instance = new DefaultClan(newID);
				instance.setName(clanName);
				boolean war = ClansAPI.getData().getMain().read(c -> c.getString("Clans.mode-change.default").equalsIgnoreCase("peace"));
				instance.setPeaceful(war);
				if (password != null) {
					instance.setPassword(password);
				}
				instance.getMembers().add(new Clan.Associate(owner, RankPriority.HIGHEST, HUID.fromString(newID)));
				instance.save();
				API.getClanManager().load(instance);
				ClansAPI.getData().getClanFile(instance).write(t -> t.set("user-data." + owner.toString() + ".join-date", new Date().getTime()));
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					ClanDisplayName.set(Bukkit.getPlayer(owner), ClansAPI.getData().prefixedTag(instance.getPalette().getStart(), clanName));
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
		// Triple call, The same clan ID must never co-exist.
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
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			if (!(associate.getClan() instanceof DefaultClan))
				return;

			DefaultClan clanIndex = (DefaultClan) associate.getClan();

			ClanLeaveEvent ev = ClanVentBus.call(new ClanLeaveEvent(associate));

			if (ev.isCancelled()) {
				return;
			}

			FileManager clan = ClansAPI.getData().getClanFile(clanIndex);
			if (associate.getUser().toBukkit().isOnline()) {
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					ClanDisplayName.remove(associate.getUser().toBukkit().getPlayer());
				}
			}
			switch (associate.getPriority()) {
				case HIGHEST:
					for (String ally : clanIndex.getAllyList()) {
						Clan a = API.getClan(ally);
						a.removeAlly(clanIndex.getId());
					}
					for (String enemy : clanIndex.getEnemyList()) {
						Clan a = API.getClan(enemy);
						a.removeEnemy(clanIndex.getId());
					}
					FileManager regions = API.getClaimManager().getFile();
					regions.write(t -> t.set(associate.getClan().getId().toString(), null));
					String clanName = clan.getRoot().getString("name");
					for (String s : associate.getClan().getDataKeys()) {
						associate.getClan().removeValue(s);
					}
					API.getClanManager().delete(clanIndex);
					String format = MessageFormat.format(ClansAPI.getData().getMessageResponse("deletion"), clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
					API.getClaimManager().refresh();
					break;
				case HIGHER:
				case NORMAL:
				case HIGH:
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessageResponse("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
					clan.write(t -> t.set("user-data." + target.toString(), null));
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
		Clan.Associate associate = API.getAssociate(target).orElse(null);
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
				Clan clanIndex = API.getClan(API.getClanID(clanName));
				ClansAPI.getData().getClanFile(clanIndex).write(t -> t.set("user-data." + target.toString() + ".join-date", new Date().getTime()));
				clanIndex.getMembers().add(new Clan.Associate(target, RankPriority.NORMAL, clanIndex.getId()));
				clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessageResponse("member-join"), Bukkit.getOfflinePlayer(target).getName()));
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						ClanDisplayName.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getData().prefixedTag(API.getClan(API.getClanID(clanName)).getPalette().getStart(), clanName));
					}
				}
				return;
			}
			if (c.getPassword().equals(password)) {
				Clan clanIndex = API.getClan(API.getClanID(clanName));
				ClansAPI.getData().getClanFile(clanIndex).write(t -> t.set("user-data." + target.toString() + ".join-date", new Date().getTime()));
				clanIndex.getMembers().add(new Clan.Associate(target, RankPriority.NORMAL, clanIndex.getId()));
				clanIndex.broadcast(MessageFormat.format(ClansAPI.getData().getMessageResponse("member-join"), Bukkit.getOfflinePlayer(target).getName()));
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					if (Bukkit.getOfflinePlayer(target).isOnline()) {
						ClanDisplayName.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getData().prefixedTag(API.getClan(API.getClanID(clanName)).getPalette().getStart(), clanName));
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
		LabyrinthUser user = LabyrinthUser.get(playerName);
		if (user.isValid()) {
			return user.getId();
		}
		return null;
	}

	public List<UUID> getAllUsers() {
		return LabyrinthProvider.getOfflinePlayers().stream().map(LabyrinthUser::getId).collect(Collectors.toList());
	}

	public void demotePlayer(UUID target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
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
			String format = MessageFormat.format(ClansAPI.getData().getMessageResponse("demotion"), Bukkit.getOfflinePlayer(target).getName(), associate.getRankTag());
			clanIndex.broadcast(format);
		}
	}

	public void promotePlayer(UUID target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
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
			String format = MessageFormat.format(ClansAPI.getData().getMessageResponse("promotion"), Bukkit.getOfflinePlayer(target).getName(), associate.getRankTag());
			clanIndex.broadcast(format);
		}
	}

	public void kickPlayer(UUID target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			if (ClansAPI.getData().prefixedTagsAllowed()) {
				if (Bukkit.getPlayer(target) != null) {
					ClanDisplayName.remove(Bukkit.getPlayer(target));
				}
			}
			FileManager clan = ClansAPI.getData().getClanFile(associate.getClan());
			clan.write(t -> t.set("user-data." + target.toString(), null));
			Schedule.sync(() -> associate.getClan().getMembers().remove(associate)).wait(1);
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
			return a.relate(API.getClan(targetClanID));
		} catch (NullPointerException ignored) {
		}
		return result;
	}

	/**
	 * Get the relation color between two clans.
	 * <p>
	 * WHITE = Neutral
	 * GREEN = Allies
	 * RED = Enemies
	 * GOLD = Same clan
	 *
	 * @param a Primary clan
	 * @param b Target clan
	 * @return Gets the relation color in color code format for the two clans.
	 */
	public String getRelationColor(Clan a, Clan b) {
		return a.relate(b);
	}

	/**
	 * @return Gets a list of all saved clans by name
	 */
	public List<String> getAllClanNames() {
		return ClansAPI.getInstance().getClanManager().getClans().map(Clan::getName).collect(Collectors.toList());
	}

	/**
	 * @return Gets a list of all saved clans by clanID
	 */
	public List<String> getAllClanIDs() {
		DataManager dm = new DataManager();
		List<String> array = new ArrayList<>();
		for (File file : Objects.requireNonNull(dm.getClanFolder().listFiles())) {
			array.add(file.getName().replace(".yml", "").replace(".data", ""));
		}
		return array;
	}

	public boolean overPowerBypass() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getBoolean("Clans.raid-shield.claiming");
	}

	public int modeChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.mode-clearance");
	}

	public int tagChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.rename-clearance");
	}

	public int descriptionChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.description-clearance");
	}

	public int colorChangeClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.recolor-clearance");
	}

	public int friendlyFireClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.friendly-fire.clearance");
	}

	public int positionClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.position-clearance");
	}

	public int unclaimAllClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.land-claiming.unclaim-all-clearance");
	}

	public int claimingClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.land-claiming.clearance");
	}

	public int invitationClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.invite-clearance");
	}

	public int baseClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.base-clearance");
	}

	public int kickClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.kick-clearance");
	}

	public int passwordClearance() {
		FileManager main = ClansAPI.getData().getMain();
		return main.getRoot().getInt("Clans.password-clearance");
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

	/*
	public String[] getMotd(List<String> logo, Clan clan) {
		String[] ar = logo.toArray(new String[0]);
		String[] motd = new Paragraph(clan.getMotd()).setRegex(Paragraph.COMMA_AND_PERIOD).get();
		for (int i = 0; i < ar.length; i++) {
			if (i > 0) {
				if ((Math.max(0, i - 1)) <= motd.length - 1) {
					String m = motd[Math.max(0, i - 1)];
					ar[i] = ar[i] + "   &r" + m;
				}
			}
		}
		return ar;
	}

	 */

	public List<Clan> getMostPowerful() {
		List<Clan> c = ClansAPI.getInstance().getClanManager().getClans().list();
		c.sort(Comparator.comparingDouble(Clan::getPower));
		Collections.reverse(c);
		return Collections.unmodifiableList(c);
	}

	public void getClanboard(Player target, int page) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);

		if (associate == null) return;

		Clan clan = associate.getClan();

		List<String> array = new ArrayList<>();

		String owner = clan.getOwner().getUser().getName();
		String password = clan.getPassword();

		List<String> members = clan.getMembers().stream().filter(a -> a.getPriority().toInt() != 3).map(Clan.Associate::getUser).map(LabyrinthUser::getId).map(UUID::toString).collect(Collectors.toList());
		List<String> mods = clan.getMembers().stream().filter(a -> a.getPriority().toInt() == 1).map(Clan.Associate::getUser).map(LabyrinthUser::getName).collect(Collectors.toList());
		List<String> admins = clan.getMembers().stream().filter(a -> a.getPriority().toInt() == 2).map(Clan.Associate::getUser).map(LabyrinthUser::getName).collect(Collectors.toList());
		List<String> allies = clan.getAllies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());
		List<String> allyRequests = clan.getAllyRequests();
		List<String> enemies = clan.getEnemies().map(Clan::getId).map(HUID::toString).collect(Collectors.toList());

		String c = clan.getPalette().getStart();

		if (c.equals("&f"))
			c = "&6";
		array.add(" ");
		array.add("&6&lClan&7: &f" + (clan.getPalette().isGradient() ? clan.getPalette().toGradient().context(clan.getName()).translate() : clan.getPalette().toString() + clan.getName()));
		array.add("&f&m---------------------------");
		array.add("&6Description: &7" + clan.getDescription());
		/*
		List<String> logo = (List<String>) new HFEncoded("rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAGdwQAAAAGdABswqc04paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqc04paRdACQwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paSwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paSwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRdACQwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqdBwqc2wqcxwqcywqc4wqdE4paTwqd4wqdBwqc2wqcxwqcywqc4wqdE4paTwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRdACQwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqdBwqc2wqcxwqcywqc4wqdE4paTwqd4wqdBwqc2wqcxwqcywqc4wqdE4paTwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRdACQwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paSwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqdBwqc2wqcxwqcywqc4wqdE4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paSwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRdABswqc04paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqd4wqcywqcxwqcwwqc5wqc0wqdG4paRwqc04paReA").deserialize(List.class);
		if (logo != null) {
			array.add("&f&m---------------------------");
			array.addAll(Arrays.asList(getMotd(logo, clan)));
			array.add("&f&m---------------------------");
		}
		 */


		array.add("&6" + clan.getOwner().getRankTag() + ": &f" + owner);
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
		if (clan.getPalette().isGradient()) {
			array.add("&6Color: " + (clan.getPalette().getStart() + clan.getPalette().getEnd()).replace("&", "").replace("#", "&f»" + c));
		} else {
			array.add("&6Color: " + c + c.replace("&", "&f»" + c).replace("#", "&f»" + c));
		}
		if (Permission.MANAGE_PASSWORD.test(associate)) {
			array.add("&6Password: &f" + password);
		}
		array.add("&6&lPower [&e" + clan.format(String.valueOf(clan.getPower())) + "&6&l]");
		if (EconomyProvision.getInstance().isValid()) {
			array.add("&6&lBank [&e" + clan.format(String.valueOf(clan.getBalance().doubleValue())) + "&6&l]");
		}
		array.add("&6" + ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Styles.Full.Admin") + "s [" + c + admins.size() + "&6]");
		array.add("&6" + ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Styles.Full.Moderator") + "s [" + c + mods.size() + "&6]");
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
		ClanInformationAdaptEvent event = ClanVentBus.call(new ClanInformationAdaptEvent(array, clan.getId().toString()));
		for (String l : event.getInsertions()) {
			target.sendMessage(color(l));
		}
		sendComponent(target, TextLib.getInstance().textRunnable("&fPerms &7(", "&bClick", "&7)", "&b&oClick to view clan permissions.", "clan perms"));
		StashesAddon stashes = ClanAddon.getAddon(StashesAddon.class);
		VaultsAddon vaults = ClanAddon.getAddon(VaultsAddon.class);
		if (stashes != null && stashes.getContext().isActive()) {
			sendComponent(target, TextLib.getInstance().textRunnable("&eStash &7(", "&bClick", "&7)", "&b&oClick to view the clan stash.", "clan stash"));
		}
		if (vaults != null && vaults.getContext().isActive()) {
			sendComponent(target, TextLib.getInstance().textRunnable("&6Vault &7(", "&bClick", "&7)", "&b&oClick to view the clan vault.", "clan vault"));
		}
		target.sendMessage(color("&f&m---------------------------"));
		target.sendMessage(color("&n" + ClansAPI.getData().getMain().getRoot().getString("Formatting.Chat.Styles.Full.Member") + "s&r [" + c + members.size() + "&r]"));
		getMemberboard(target, members, page);
		target.sendMessage(" ");
	}

	public void getPlayerboard(Player p, int page) {
		Message msg = Message.form(p);
		new PaginatedList<>(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList())).limit(10)
				.finish(b -> b.setPage(page).setPlayer(p).setPrefix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setSuffix("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))
				.start((pagination, page1, max) -> msg.send("&7&m------------&7&l[&6&oPlayer List&7&l]&7&m------------"))
				.compare(String::compareTo)
				.decorate((pagination, object, page1, max, placement) -> msg.build(TextLib.getInstance().textRunnable("&aI am", " &6" + object, "&7Click to view my clan info.", "c info " + object))).get(page);
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
							for (Clan.Associate associate : o1.getMembers()) {
								kd1 += associate.getKD();
							}
							double kd2 = 0;
							for (Clan.Associate associate : o2.getMembers()) {
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
					for (Clan.Associate associate : clan.getMembers()) {
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
