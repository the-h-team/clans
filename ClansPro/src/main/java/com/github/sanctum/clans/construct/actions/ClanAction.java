package com.github.sanctum.clans.construct.actions;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.StashesAddon;
import com.github.sanctum.clans.bridge.internal.VaultsAddon;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.AbstractGameRule;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.Teleport;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.ComparatorUtil;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultAssociate;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.event.associate.AssociateQuitEvent;
import com.github.sanctum.clans.event.associate.AssociateRankManagementEvent;
import com.github.sanctum.clans.event.clan.ClanFreshlyFormedEvent;
import com.github.sanctum.clans.event.clan.ClanKickAssociateEvent;
import com.github.sanctum.clans.event.command.ClanInformationAdaptEvent;
import com.github.sanctum.clans.event.player.PlayerCreateClanEvent;
import com.github.sanctum.clans.event.player.PlayerJoinClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanAction extends StringLibrary {

	private final ClansAPI API = ClansAPI.getInstance();

	public void create(@NotNull UUID owner, @NotNull String clanName, @Nullable String password) {
		if (API.getClanManager().getClanID(owner) == null) {
			if (clanName.length() > ClansAPI.getDataInstance().getConfig().read(c -> c.getInt("Formatting.tag-size"))) {
				if (Bukkit.getPlayer(owner) != null) {
					sendMessage(Bukkit.getPlayer(owner), ClansAPI.getDataInstance().getMessageResponse("too-long"));
				}
				return;
			}
			PlayerCreateClanEvent e = ClanVentBus.call(new PlayerCreateClanEvent(owner, clanName, password));
			if (!e.isCancelled()) {
				String status = "OPEN";
				if (password == null) {
					String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				} else {
					status = "LOCKED";
					String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("creation"), Bukkit.getPlayer(owner).getName(), status, clanName);
					Bukkit.broadcastMessage(color(getPrefix() + " " + format));
				}
				String newID = generateCleanClanCode();
				DefaultClan instance = new DefaultClan(newID);
				instance.setName(clanName);
				boolean war = LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(ClansAPI.getInstance().getLocalPrintKey()).getString(AbstractGameRule.DEFAULT_WAR_MODE).equalsIgnoreCase("peace");
				instance.setPeaceful(war);
				if (password != null) {
					instance.setPassword(password);
				}
				instance.add(new DefaultAssociate(owner, Clan.Rank.HIGHEST, instance));
				instance.save();
				API.getClanManager().load(instance);
				if (!LabyrinthProvider.getInstance().isLegacy()) {
					if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
						ClanDisplayName.set(Bukkit.getPlayer(owner), ClansAPI.getDataInstance().formatDisplayTag(instance.getPalette().toString(), clanName));
					}
				}
				ClanVentBus.call(new ClanFreshlyFormedEvent(owner, clanName));
			}
		} else {
			if (Bukkit.getPlayer(owner) != null) {
				sendMessage(Bukkit.getPlayer(owner), alreadyInClan());
			}
		}
	}

	String generateCleanClanCode() {
		// Triple call, The same clan ID must never co-exist.
		HUID code = HUID.randomID();
		List<String> allids = getAllClanIDs();
		for (int i = 0; i < 3; i++) {
			if (allids.contains(code.toString())) {
				code = HUID.randomID();
			} else {
				break;
			}
		}
		return code.toString();
	}

	public void removePlayer(@NotNull UUID target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			if (!(associate.getClan() instanceof DefaultClan))
				return;

			DefaultClan clanIndex = (DefaultClan) associate.getClan();

			AssociateQuitEvent ev = ClanVentBus.call(new AssociateQuitEvent(associate));

			if (ev.isCancelled()) {
				return;
			}

			FileManager clan = ClansAPI.getDataInstance().getClanFile(clanIndex);
			if (associate.getTag().isPlayer() && associate.getUser().toBukkit().isOnline()) {
				if (!LabyrinthProvider.getInstance().isLegacy()) {
					if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
						ClanDisplayName.remove(associate.getUser().toBukkit().getPlayer());
					}
				}
			}
			switch (associate.getPriority()) {
				case HIGHEST:
					for (Claim c : associate.getClan().getClaims()) {
						c.remove();
					}
					clanIndex.getRelation().getAlliance().get(Clan.class).forEach(a -> a.getRelation().getAlliance().remove(clanIndex));
					clanIndex.getRelation().getRivalry().get(Clan.class).forEach(a -> a.getRelation().getRivalry().remove(clanIndex));
					FileManager regions = API.getClaimManager().getFile();
					regions.write(t -> t.set(associate.getClan().getId().toString(), null));
					Schedule.sync(() -> {
						String clanName = clan.getRoot().getString("name");
						for (String s : associate.getClan().getKeys()) {
							associate.getClan().removeValue(s);
						}
						API.getClanManager().delete(clanIndex);
						String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("deletion"), clanName);
						Bukkit.broadcastMessage(color(getPrefix() + " " + format));
						API.getClaimManager().refresh();
					}).waitReal(1);
					break;
				case HIGHER:
				case NORMAL:
				case HIGH:
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
					clan.write(t -> t.set("user-data." + target, null));
					Schedule.sync(associate::remove).run();
					break;
			}
		} else {
			if (Bukkit.getPlayer(target) != null) {
				sendMessage(Bukkit.getPlayer(target), notInClan());
			}
		}
	}

	public void joinClan(@NotNull UUID target, @NotNull String clanName, @Nullable String password) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate == null) {

			Clan c = API.getClanManager().getClan(API.getClanManager().getClanID(clanName));

			if (c == null) return;

			if (Bukkit.getPlayer(target) != null) {
				PlayerJoinClanEvent event = ClanVentBus.call(new PlayerJoinClanEvent(Bukkit.getPlayer(target), (DefaultClan) c));
				if (!event.isCancelled()) {
					if (!getAllClanNames().contains(clanName)) {
						if (Bukkit.getPlayer(target) != null) {
							sendMessage(Bukkit.getPlayer(target), clanUnknown(clanName));
						}
						return;
					}
					if (c.getPassword() == null) {
						Clan clanIndex = API.getClanManager().getClan(API.getClanManager().getClanID(clanName));
						clanIndex.add(new DefaultAssociate(target, Clan.Rank.NORMAL, clanIndex));
						clanIndex.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), Bukkit.getOfflinePlayer(target).getName()));
						if (!LabyrinthProvider.getInstance().isLegacy()) {
							if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
								if (Bukkit.getOfflinePlayer(target).isOnline()) {
									ClanDisplayName.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(API.getClanManager().getClan(API.getClanManager().getClanID(clanName)).getPalette().toString(), clanName));
								}
							}
						}
						return;
					}
					if (c.getPassword().equals(password)) {
						Clan clanIndex = API.getClanManager().getClan(API.getClanManager().getClanID(clanName));
						clanIndex.add(new DefaultAssociate(target, Clan.Rank.NORMAL, clanIndex));
						clanIndex.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), Bukkit.getOfflinePlayer(target).getName()));
						if (!LabyrinthProvider.getInstance().isLegacy()) {
							if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
								if (Bukkit.getOfflinePlayer(target).isOnline()) {
									ClanDisplayName.set(Bukkit.getOfflinePlayer(target).getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(API.getClanManager().getClan(API.getClanManager().getClanID(clanName)).getPalette().toString(), clanName));
								}
							}
						}
					} else {
						if (Bukkit.getPlayer(target) != null) {
							sendMessage(Bukkit.getPlayer(target), wrongPassword());
						}
					}
				}
			} else {
				if (!getAllClanNames().contains(clanName)) {
					if (Bukkit.getPlayer(target) != null) {
						sendMessage(Bukkit.getPlayer(target), clanUnknown(clanName));
					}
					return;
				}
				if (c.getPassword() == null) {
					Clan clanIndex = API.getClanManager().getClan(API.getClanManager().getClanID(clanName));
					Clan.Associate a = new DefaultAssociate(target, Clan.Rank.NORMAL, clanIndex);
					clanIndex.add(a);
					a.save();
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), a.getName()));
					if (!LabyrinthProvider.getInstance().isLegacy()) {
						if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
							if (!a.isEntity() && a.getUser().isOnline()) {
								ClanDisplayName.set(a.getUser().toBukkit().getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(a.getClan().getPalette().toString(), clanName));
							}
						}
					}
					return;
				}
				if (c.getPassword().equals(password)) {
					Clan clanIndex = API.getClanManager().getClan(API.getClanManager().getClanID(clanName));
					Clan.Associate a = new DefaultAssociate(target, Clan.Rank.NORMAL, clanIndex);
					clanIndex.add(a);
					a.save();
					clanIndex.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), a.getName()));
					if (!LabyrinthProvider.getInstance().isLegacy()) {
						if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
							if (!a.isEntity() && a.getUser().isOnline()) {
								ClanDisplayName.set(a.getUser().toBukkit().getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(a.getClan().getPalette().toString(), clanName));
							}
						}
					}
				} else {
					if (Bukkit.getPlayer(target) != null) {
						sendMessage(Bukkit.getPlayer(target), wrongPassword());
					}
				}
			}
		} else {
			if (Bukkit.getPlayer(target) != null) {
				sendMessage(Bukkit.getPlayer(target), alreadyInClan());
			}
		}
	}

	public UUID getUserID(String playerName) {
		if (getAllClanNames().contains(playerName)) return null;
		LabyrinthUser user = LabyrinthUser.get(playerName);
		if (user != null && user.isValid()) {
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
			Clan.Rank priority = null;
			if (associate.getPriority().toLevel() < 3) {
				switch (associate.getPriority().toLevel()) {
					case 2:
						priority = Clan.Rank.HIGH;
						break;
					case 1:
						priority = Clan.Rank.NORMAL;
						break;
				}
			}
			if (priority != null) {
				AssociateRankManagementEvent event = ClanVentBus.call(new AssociateRankManagementEvent(associate, priority));
				if (!event.isCancelled()) {
					associate.setPriority(event.getTo());
					Clan clanIndex = associate.getClan();
					String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("demotion"), associate.getName(), associate.getRankFull());
					clanIndex.broadcast(format);
				}
			}
		}
	}

	public void promotePlayer(UUID target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			Clan.Rank priority = null;
			if (associate.getPriority().toLevel() < 2) {
				switch (associate.getPriority().toLevel()) {
					case 0:
						priority = Clan.Rank.HIGH;
						break;
					case 1:
						priority = Clan.Rank.HIGHER;
						break;
				}
			}
			if (priority != null) {
				AssociateRankManagementEvent event = ClanVentBus.call(new AssociateRankManagementEvent(associate, priority));
				if (!event.isCancelled()) {
					associate.setPriority(event.getTo());
					Clan clanIndex = API.getClanManager().getClan(API.getClanManager().getClanID(target));
					String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("promotion"), associate.getName(), associate.getRankFull());
					clanIndex.broadcast(format);
				}
			}
		}
	}

	public void kickPlayer(UUID target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate != null) {
			ClanKickAssociateEvent event = ClanVentBus.call(new ClanKickAssociateEvent(associate));
			if (!event.isCancelled()) {
				if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
					if (Bukkit.getPlayer(target) != null) {
						ClanDisplayName.remove(Bukkit.getPlayer(target));
					}
				}
				FileManager clan = ClansAPI.getDataInstance().getClanFile(associate.getClan());
				clan.write(t -> t.set("user-data." + target.toString(), null));
				Schedule.sync(() -> associate.getClan().remove(associate)).wait(1);
			}
		}
	}

	public void teleport(Player p, Location location) {
		Clan clan = API.getClanManager().getClan(p.getUniqueId());
		if (clan != null) {
			if (location != null) {
				boolean waiting = false;
				for (Entity e : p.getNearbyEntities(30, 30, 30)) {
					if (e instanceof Player) {
						if (clan.getMember(a -> a.getId().equals(e.getUniqueId())) != null) {
							waiting = true;
							break;
						}
					}
				}
				if (!waiting) {
					p.teleport(location);
				} else {
					Teleport request = Teleport.get(API.getAssociate(p).get());
					if (request != null) {
						sendMessage(p, "&cYou already have a pending teleportation in progress.");
					} else {
						sendMessage(p, "&cSomeone is nearby...");
						request = new Teleport.Impl(API.getAssociate(p).get(), location);
						request.teleport();
					}
				}
			} else {
				sendMessage(p, "&cThis location doesn't exist.");
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
		Clan a = API.getClanManager().getClan(HUID.fromString(clanID));
		try {
			return a.relate(API.getClanManager().getClan(HUID.fromString(targetClanID)));
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
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getBoolean("Clans.raid-shield.claiming");
	}

	public int modeChangeClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.mode-clearance");
	}

	public int tagChangeClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.rename-clearance");
	}

	public int descriptionChangeClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.description-clearance");
	}

	public int colorChangeClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.recolor-clearance");
	}

	public int friendlyFireClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.friendly-fire.clearance");
	}

	public int positionClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.position-clearance");
	}

	public int unclaimAllClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.land-claiming.unclaim-all-clearance");
	}

	public int claimingClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.land-claiming.clearance");
	}

	public int invitationClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.invite-clearance");
	}

	public int baseClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.base-clearance");
	}

	public int kickClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
		return main.getRoot().getInt("Clans.kick-clearance");
	}

	public int passwordClearance() {
		FileManager main = ClansAPI.getDataInstance().getConfig();
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

	public ArmorStand getArmorStandInSight(Player player, int range) {
		List<ArmorStand> entities = player.getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof ArmorStand).map(entity -> (ArmorStand) entity).collect(Collectors.toList());
		try {
			List<Block> sightBlock = player.getLineOfSight(null, range);
			List<Location> sight = new ArrayList<>();
			int i;
			for (i = 0; i < sightBlock.size(); i++)
				sight.add(sightBlock.get(i).getLocation());
			for (i = 0; i < sight.size(); i++) {
				for (ArmorStand entity : entities) {
					if (entity.getLocation().distance(sight.get(i)) <= 0.8) return entity;
				}
			}
		} catch (IllegalStateException ignored) {
		}
		return null;
	}

	public List<Clan> getMostPowerful() {
		List<Clan> c = ClansAPI.getInstance().getClanManager().getClans().list();
		c.sort(ComparatorUtil.comparingByPower());
		Collections.reverse(c);
		return Collections.unmodifiableList(c);
	}

	public @NotNull String format(double amount) {
		BigDecimal b1 = BigDecimal.valueOf(amount);
		Locale loc = Locale.US;
		switch (ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.locale")) {
			case "fr":
				loc = Locale.FRANCE;
				break;
			case "de":
				loc = Locale.GERMANY;
				break;
			case "nl":
				loc = new Locale("nl", "NL");
				break;
		}
		return NumberFormat.getNumberInstance(loc).format(b1.doubleValue());
	}

	public void getClanboard(Player target, int page) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);

		if (associate == null) return;

		Clan clan = associate.getClan();

		List<String> array = new ArrayList<>();

		String owner = clan.getOwner().getName();
		String password = clan.getPassword();

		List<String> members = clan.getMembers().stream().filter(a -> a.getPriority().toLevel() != 3).map(Clan.Associate::getId).map(UUID::toString).collect(Collectors.toList());
		List<String> mods = clan.getMembers().stream().filter(a -> a.getPriority().toLevel() == 1).map(Nameable::getName).collect(Collectors.toList());
		List<String> admins = clan.getMembers().stream().filter(a -> a.getPriority().toLevel() == 2).map(Nameable::getName).collect(Collectors.toList());
		List<String> allies = clan.getRelation().getAlliance().stream().map(Nameable::getName).collect(Collectors.toList());
		List<String> enemies = clan.getRelation().getRivalry().stream().map(Nameable::getName).collect(Collectors.toList());

		String c = clan.getPalette().toString();

		if (c.equals("&f"))
			c = "&6";
		array.add(" ");
		array.add("&6&lClan&7: &f" + (clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()));
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


		array.add("&6" + clan.getOwner().getRankFull() + ": &f" + owner);
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
			array.add("&6Color: " + (clan.getPalette().toArray()[0] + clan.getPalette().toArray()[1]).replace("&", "").replace("#", "&f»" + c));
		} else {
			array.add("&6Color: " + c + c.replace("&", "&f»" + c).replace("#", "&f»" + c));
		}
		if (Clearance.MANAGE_PASSWORD.test(associate)) {
			array.add("&6Password: &f" + password);
		}
		array.add("&6&lPower [&e" + Clan.ACTION.format(clan.getPower()) + "&6&l]");
		if (EconomyProvision.getInstance().isValid()) {
			array.add("&6&lBank [&e" + Clan.ACTION.format(clan.getBalance().doubleValue()) + "&6&l]");
		}
		array.add("&6" + ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Styles.Full.Admin") + "s [" + c + admins.size() + "&6]");
		array.add("&6" + ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Styles.Full.Moderator") + "s [" + c + mods.size() + "&6]");
		array.add("&6Claims [" + c + clan.getClaims().length + "&f/ " + c + clan.getClaimLimit() + "&6]");
		array.add("&f&m---------------------------");
		if (clan.getRelation().getAlliance().getRequests().size() > 0) {
			array.add("&6Ally Requests [" + c + clan.getRelation().getAlliance().getRequests().size() + "&6]");
			clan.getRelation().getAlliance().getRequests(Clan.class).forEach(cl -> array.add("&f- &e&o" + cl.getName()));
		}
		if (clan.getRelation().getAlliance().getRequests().isEmpty())
			array.add("&6Ally Requests [" + c + 0 + "&6]");
		if (allies.size() > 0) {
			array.add("&6Allies [" + c + allies.size() + "&6]");
			for (String name : allies) {
				array.add("&f- &e&o" + name);
			}
		}
		if (allies.isEmpty())
			array.add("&6Allies [" + c + 0 + "&6]");
		if (enemies.size() > 0) {
			array.add("&6Enemies [" + c + enemies.size() + "&6]");
			for (String name : enemies) {
				array.add("&f- &c&o" + name);
			}
		}
		if (enemies.isEmpty())
			array.add("&6Enemies [" + c + 0 + "&6]");
		array.add("&f&m---------------------------");
		ClanInformationAdaptEvent event = ClanVentBus.call(new ClanInformationAdaptEvent(array, clan.getId().toString(), ClanInformationAdaptEvent.Type.PERSONAL));
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
		target.sendMessage(color("&n" + ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Styles.Full.Member") + "s&r [" + c + members.size() + "&r]"));
		getMemberboard(target, members, page);
		target.sendMessage(" ");
	}

	public void getPlayerboard(Player p, int page) {
		EasyPagination<? extends Player> test = new EasyPagination<>(p, Bukkit.getOnlinePlayers());
		test.limit(menuSize());
		test.setHeader((player, chunks) -> {
			if (LabyrinthProvider.getInstance().isNew()) {
				chunks.then("&7&m------------&7&l[&#ff7700&oPlayer List&7&l]&7&m------------");
			} else {
				chunks.then("&7&m------------&7&l[&6&oPlayer List&7&l]&7&m------------");
			}
		});
		test.setFormat((player, index, chunks) -> {
			chunks.then("#").color(ChatColor.GRAY).then(index).color(ChatColor.GOLD).then(" ").then(player.getName()).then(" ").then("-").then(" ").then(ClansAPI.getInstance().getAssociate(player).isPresent() ? "&aIn clan" : "&cNot in clan");
		});
		test.setFooter((player, chunks) -> {
			chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		});
		test.send(page);
	}

	public void getLeaderboard(LeaderboardType type, Player p, int pageNum) {
		switch (type) {
			case MONEY:
				if (!EconomyProvision.getInstance().isValid()) {
					getLeaderboard(LeaderboardType.POWER, p, pageNum);
					break;
				}

				EasyPagination<Clan> test = new EasyPagination<>(p, getMostPowerful(), ComparatorUtil.comparingByMoney());
				test.limit(menuSize());
				test.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oMost Money&7&l]&7&m------------");
					} else {
						chunks.then("&7&m------------&7&l[&6&oMost Money&7&l]&7&m------------");
					}
				});
				test.setFormat((clan, index, chunks) -> {
					chunks.then("#").color(ChatColor.GRAY).then(index).color(ChatColor.GOLD).then(" ").then(clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()).hover("&3Click for info.").command("c info " + clan.getName()).then(" ").then("-").then(" ").then(clan.getDescription());
				});
				test.setFooter((player, chunks) -> {
					chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				test.send(pageNum);
				break;
			case WINS:
				EasyPagination<Clan> test2 = new EasyPagination<>(p, getMostPowerful(), (o1, o2) -> Integer.compare(o2.getWins(), o1.getWins()));
				test2.limit(menuSize());
				test2.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oMost Wins&7&l]&7&m------------");
					} else {
						chunks.then("&7&m------------&7&l[&6&oMost Wins&7&l]&7&m------------");
					}
				});
				test2.setFormat((clan, index, chunks) -> {
					chunks.then("#").color(ChatColor.GRAY).then(index).color(ChatColor.GOLD).then(" ").then(clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()).hover("&3Click for info.").command("c info " + clan.getName()).then(" ").then("-").then(" ").then(clan.getDescription());
				});
				test2.setFooter((player, chunks) -> {
					chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				test2.send(pageNum);
				break;
			case POWER:
				EasyPagination<Clan> test3 = new EasyPagination<>(p, getMostPowerful(), ComparatorUtil.comparingByPower());
				test3.limit(menuSize());
				test3.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oMost Power&7&l]&7&m------------");
					} else {
						chunks.then("&7&m------------&7&l[&6&oMost Power&7&l]&7&m------------");
					}
				});
				test3.setFormat((clan, index, chunks) -> {
					chunks.then("#").color(ChatColor.GRAY).then(index).color(ChatColor.GOLD).then(" ").then(clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()).hover("&3Click for info.").command("c info " + clan.getName()).then(" ").then("-").then(" ").then(clan.getDescription());
				});
				test3.setFooter((player, chunks) -> {
					chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				test3.send(pageNum);
				break;
			case KILLS:
				EasyPagination<Clan> test4 = new EasyPagination<>(p, getMostPowerful(), ComparatorUtil.comparingByEntity());
				test4.limit(menuSize());
				test4.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oHighest K/D&7&l]&7&m------------");
					} else {
						chunks.then("&7&m------------&7&l[&6&oHighest K/D&7&l]&7&m------------");
					}
				});
				test4.setFormat((clan, index, chunks) -> {
					chunks.then("#").color(ChatColor.GRAY).then(index).color(ChatColor.GOLD).then(" ").then(clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()).hover("&3Click for info.").command("c info " + clan.getName()).then(" ").then("-").then(" ").then(clan.getDescription());
				});
				test4.setFooter((player, chunks) -> {
					chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				test4.send(pageNum);
				break;
		}
	}

	public enum LeaderboardType {
		MONEY, WINS, POWER, KILLS
	}

}
