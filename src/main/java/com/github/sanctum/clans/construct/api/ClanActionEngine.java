package com.github.sanctum.clans.construct.api;

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
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.ClansComparators;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.construct.impl.entity.DefaultAssociate;
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
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.util.HUID;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanActionEngine extends StringLibrary {

	private final ClansAPI API = ClansAPI.getInstance();
	private final List<String> info_board = ClansAPI.getDataInstance().getMessages().getRoot().getStringList("info-simple");

	public Clan.Action<Clan> create(@NotNull UUID owner, @NotNull String name, @Nullable String password, boolean silent) {
		return new Clan.Action<Clan>() {

			List<String> getAllClanIDs() {
				DataManager dm = ClansAPI.getDataInstance();
				List<String> array = new ArrayList<>();
				for (File file : Objects.requireNonNull(dm.getClanFolder().listFiles())) {
					array.add(file.getName().replace(".yml", "").replace(".data", ""));
				}
				return array;
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

			@Override
			public Clan deploy() {
				Player p = Bukkit.getPlayer(owner);
				StringLibrary lib = StringLibrary.LOCAL;
				if (API.getClanManager().getClanID(owner) == null) {
					if (name.length() > ClansAPI.getDataInstance().getConfig().read(c -> c.getInt("Formatting.tag-size"))) {
						if (!silent) {
							if (p != null) {
								lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("too-long"));
							}
						}
						return null;
					}
					PlayerCreateClanEvent e = ClanVentBus.call(new PlayerCreateClanEvent(owner, name, password));
					if (!e.isCancelled()) {
						if (!silent) {
							String status = "OPEN";
							if (password == null) {
								String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("creation"), p.getName(), status, name);
								Bukkit.broadcastMessage(lib.color(lib.getPrefix() + " " + format));
							} else {
								status = "LOCKED";
								String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("creation"), p.getName(), status, name);
								Bukkit.broadcastMessage(lib.color(lib.getPrefix() + " " + format));
							}
						}
						String newID = generateCleanClanCode();
						DefaultClan instance = new DefaultClan(newID);
						instance.setName(name);
						boolean war = LabyrinthProvider.getInstance().getLocalPrintManager()
								.getPrint(ClansAPI.getInstance().getLocalPrintKey())
								.getString(AbstractGameRule.DEFAULT_WAR_MODE)
								.equalsIgnoreCase("peace");
						instance.setPeaceful(war);
						if (password != null) {
							instance.setPassword(password);
						}
						TaskScheduler.of(() -> {
							instance.add(new DefaultAssociate(owner, Clan.Rank.HIGHEST, instance));
							instance.save();
							API.getClanManager().load(instance);
							if (!LabyrinthProvider.getInstance().isLegacy()) {
								if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
									if (instance.getPalette().isGradient()) {
										ClanDisplayName.set(p, ClansAPI.getDataInstance().formatDisplayTag("", instance.getPalette().toGradient().context(instance.getName()).translate()));
									} else {
										ClanDisplayName.set(p, ClansAPI.getDataInstance().formatDisplayTag(instance.getPalette().toString(), instance.getName()));
									}
								}
							}
							ClanVentBus.call(new ClanFreshlyFormedEvent(owner, name));
						}).schedule();
						return instance;
					}
				} else {
					if (!silent) {
						if (p != null) {
							lib.sendMessage(p, lib.alreadyInClan());
						}
					}
				}
				return null;
			}
		};
	}

	public Clan.Action<Clan> remove(@NotNull UUID target, boolean silent) {
		return () -> {
			Clan.Associate associate = API.getAssociate(target).orElse(null);
			if (associate != null) {
				final Clan clanIndex = associate.getClan();

				AssociateQuitEvent ev = ClanVentBus.call(new AssociateQuitEvent(associate));
				if (ev.isCancelled()) {
					return clanIndex;
				}

				FileManager clan = ClansAPI.getDataInstance().getClanFile(clanIndex);
				if (associate.getTag().isPlayer() && associate.getTag().getPlayer().isOnline()) {
					if (!LabyrinthProvider.getInstance().isLegacy()) {
						if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
							ClanDisplayName.remove(associate.getTag().getPlayer().getPlayer());
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
						TaskScheduler.of(() -> {
							String clanName = clan.getRoot().getString("name");
							for (String s : associate.getClan().getKeys()) {
								associate.getClan().removeValue(s);
							}
							API.getClanManager().delete(clanIndex);
							if (!silent) {
								String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("deletion"), clanName);
								Bukkit.broadcastMessage(color(getPrefix() + " " + format));
							}
							API.getClaimManager().refresh();
						}).scheduleLater("ClansPro-deletion;" + target, 1);
						break;
					case HIGHER:
					case NORMAL:
					case HIGH:
						if (!silent) {
							clanIndex.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-leave"), Bukkit.getOfflinePlayer(target).getName()));
						}
						clan.write(t -> t.set("user-data." + target, null));
						TaskScheduler.of(associate::remove).schedule();
						break;
				}
				return clanIndex;
			} else {
				if (Bukkit.getPlayer(target) != null && !silent) {
					sendMessage(Bukkit.getPlayer(target), notInClan());
				}
				return null;
			}
		};
	}

	public Clan.Action<Clan> kick(@NotNull UUID target) {
		return () -> {
			Clan.Associate associate = API.getAssociate(target).orElse(null);
			if (associate != null) {
				Clan.Associate c = associate.getClan().getOwner();
				final Clan cl = associate.getClan();
				if (associate.equals(c)) {
					remove(target, true).deploy();
				} else {
					ClanKickAssociateEvent event = ClanVentBus.call(new ClanKickAssociateEvent(associate));
					if (!event.isCancelled()) {
						if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
							if (Bukkit.getPlayer(target) != null) {
								ClanDisplayName.remove(Bukkit.getPlayer(target));
							}
						}
						FileManager clan = ClansAPI.getDataInstance().getClanFile(associate.getClan());
						if (!associate.getClan().isConsole()) {
							clan.write(t -> t.set("user-data." + target.toString(), null));
						}
						TaskScheduler.of(() -> associate.getClan().remove(associate)).scheduleLater(1);
					}
				}
				return cl;
			}
			return null;
		};
	}

	public Clan.Action<Clan> join(@NotNull UUID target, @NotNull String clanName, @Nullable String password, boolean silent) {
		return () -> {
			Clan.Associate associate = API.getAssociate(target).orElse(null);
			Player p = Bukkit.getPlayer(target);
			if (associate == null) {

				Clan c = API.getClanManager().getClan(API.getClanManager().getClanID(clanName));
				if (c == null) return null;

				if (p != null) {
					PlayerJoinClanEvent event = ClanVentBus.call(new PlayerJoinClanEvent(p, (DefaultClan) c));
					if (!event.isCancelled()) {
						if (!getAllClanNames().contains(clanName)) {
							sendMessage(p, clanUnknown(clanName));
							return c;
						}
						if (c.getPassword() == null) {
							c.add(new DefaultAssociate(target, Clan.Rank.NORMAL, c));
							if (!silent) {
								c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), p.getName()));
							}
							if (!LabyrinthProvider.getInstance().isLegacy()) {
								if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
									if (c.getPalette().isGradient()) {
										ClanDisplayName.set(p, ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
									} else {
										ClanDisplayName.set(p, ClansAPI.getDataInstance().formatDisplayTag(c.getPalette().toString(), c.getName()));
									}
								}
							}
							return c;
						}
						if (c.getPassword().equals(password)) {
							c.add(new DefaultAssociate(target, Clan.Rank.NORMAL, c));
							if (!silent) {
								c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), p.getName()));
							}
							if (!LabyrinthProvider.getInstance().isLegacy()) {
								if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
									if (c.getPalette().isGradient()) {
										ClanDisplayName.set(p, ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
									} else {
										ClanDisplayName.set(p, ClansAPI.getDataInstance().formatDisplayTag(c.getPalette().toString(), c.getName()));
									}
								}
							}
						} else {
							if (!silent) {
								sendMessage(p, wrongPassword());
							}
						}
					}
				} else {
					if (!getAllClanNames().contains(clanName)) {
						return c;
					}
					if (c.getPassword() == null) {
						Clan.Associate a = new DefaultAssociate(target, Clan.Rank.NORMAL, c);
						c.add(a);
						a.save();
						c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), a.getName()));
						return c;
					}
					if (c.getPassword().equals(password)) {
						Clan.Associate a = new DefaultAssociate(target, Clan.Rank.NORMAL, c);
						c.add(a);
						a.save();
						c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), a.getName()));
					}
				}
				return c;
			} else {
				if (Bukkit.getPlayer(target) != null) {
					sendMessage(Bukkit.getPlayer(target), alreadyInClan());
				}
				return associate.getClan();
			}
		};
	}

	public Clan.Action<Clan> demote(@NotNull UUID target) {
		return () -> {
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
				return associate.getClan();
			}
			return null;
		};
	}

	public Clan.Action<Clan> promote(@NotNull UUID target) {
		return () -> {
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
				return associate.getClan();
			}
			return null;
		};
	}

	public Clan.Action<Boolean> test(@NotNull CommandSender p, @NotNull String permission) {
		return () -> {
			if (p.hasPermission("clans.*")) return true;
			final StringUtils utils = StringUtils.use(permission);
			if (utils.containsIgnoreCase(".admin")) {
				if (p.hasPermission("clans.admin.*")) return true;
			}
			if (utils.containsIgnoreCase(".ally")) {
				if (p.hasPermission("clans.ally.*")) return true;
			}
			if (utils.containsIgnoreCase(".enemy")) {
				if (p.hasPermission("clans.enemy.*")) return true;
			}
			if (utils.containsIgnoreCase(".bank")) {
				if (p.hasPermission(BankPermissions.BANKS_STAR.getNode()) || p.hasPermission(BankPermissions.BANKS_USE_STAR.getNode())) return true;
			}
			if (!utils.containsIgnoreCase(".admin")) {
				if (p.hasPermission("clans.use.*")) return true;
			}
			return p.hasPermission(permission);
		};
	}

	public Clan.Action<Boolean> teleport(@NotNull Player p, @Nullable Location location) {
		return () -> {
			Clan clan = API.getClanManager().getClan(p.getUniqueId());
			if (clan != null) {
				if (location != null) {
					Teleport request = Teleport.get(API.getAssociate(p).get());
					if (request != null) {
						sendMessage(p, "&cYou already have a pending teleportation in progress.");
					} else {
						new Teleport.Impl(API.getAssociate(p).get(), location).teleport();
						return true;
					}
				} else {
					sendMessage(p, "&cThis location doesn't exist.");
				}
			}
			return false;
		};
	}

	public Clan.Action<UUID> getId(String playerName) {
		return new Clan.Action<UUID>() {
			final PlayerSearch user = PlayerSearch.of(playerName);
			@Override
			public UUID deploy() {
				return user != null ? user.getId() : null;
			}
		};
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
		return ClansAPI.getInstance().getClanManager().getClans().stream().map(Clan::getName).collect(Collectors.toList());
	}

	/**
	 * @return Gets a list of all saved clans by clanID
	 */
	public List<String> getAllClanIDs() {
		DataManager dm = ClansAPI.getDataInstance();
		List<String> array = new ArrayList<>();
		for (File file : Objects.requireNonNull(dm.getClanFolder().listFiles())) {
			array.add(file.getName().replace(".yml", "").replace(".data", ""));
		}
		return array;
	}

	public boolean isIgnoringShield() {
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

	public ArmorStand getArmorStandInSight(Player player, int range) {
		List<ArmorStand> entities = player.getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof ArmorStand).map(entity -> (ArmorStand) entity).collect(Collectors.toList());
		if (entities.size() == 0) return null;
		try {
			List<Block> sightBlock = player.getLineOfSight(null, range);
			PantherCollection<Location> sight = new PantherList<>();
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

	public EnderCrystal getEnderCrystalInSight(Player player, int range) {
		List<EnderCrystal> entities = player.getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof EnderCrystal).map(entity -> (EnderCrystal) entity).collect(Collectors.toList());
		if (entities.size() == 0) return null;
		try {
			List<Block> sightBlock = player.getLineOfSight(null, range);
			PantherCollection<Location> sight = new PantherList<>();
			int i;
			for (i = 0; i < sightBlock.size(); i++)
				sight.add(sightBlock.get(i).getLocation());
			for (i = 0; i < sight.size(); i++) {
				for (EnderCrystal entity : entities) {
					if (entity.getLocation().distance(sight.get(i)) <= 2.8) return entity;
				}
			}
		} catch (IllegalStateException ignored) {
		}
		return null;
	}

	public List<Clan> getMostPowerful() {
		List<Clan> c = ClansAPI.getInstance().getClanManager().getClans().stream().sorted(ClansComparators.comparingByPower()).collect(Collectors.toList());
		Collections.reverse(c);
		return Collections.unmodifiableList(c);
	}

	/**
	 * Format a given double amount using the configured number language.
	 *
	 * @param amount the amount to format.
	 * @return A formatted language correct string.
	 */
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

	public void getClanboard(Player target) {
		Clan.Associate associate = API.getAssociate(target).orElse(null);
		if (associate == null) return;
		Clan clan =  associate.getClan();
		List<String> array = new ArrayList<>();
		info_board.forEach(s -> {
			if (clan instanceof DefaultClan) {
				array.add(((DefaultClan)clan).replacePlaceholders(s));
			}
		});
		ClanInformationAdaptEvent event = ClanVentBus.call(new ClanInformationAdaptEvent(array, clan.getId().toString(), ClanInformationAdaptEvent.Type.PERSONAL));
		for (String l : event.getInsertions()) {
			target.sendMessage(color(l));
		}

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
			case NAME:
				EasyPagination<Clan> test0 = new EasyPagination<>(p, getMostPowerful(), ClansComparators.comparingByEntity());
				test0.limit(menuSize());
				test0.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oRoster&7&l]&7&m------------");
					} else {
						chunks.then("&7&m------------&7&l[&6&oRoster&7&l]&7&m------------");
					}
				});
				test0.setFormat((clan, index, chunks) -> {
					chunks.then("#").color(ChatColor.GRAY).then(index).color(ChatColor.GOLD).then(" ").then(clan.getPalette().isGradient() ? clan.getPalette().toString(clan.getName()) : clan.getPalette() + clan.getName()).hover("&3Click for info.").command("c info " + clan.getName()).then(" ").then("-").then(" ").then(clan.getDescription());
				});
				test0.setFooter((player, chunks) -> {
					chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				test0.send(pageNum);
				break;
			case MONEY:
				if (!EconomyProvision.getInstance().isValid()) {
					getLeaderboard(LeaderboardType.POWER, p, pageNum);
					break;
				}

				EasyPagination<Clan> test = new EasyPagination<>(p, getMostPowerful(), ClansComparators.comparingByMoney());
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
					chunks.then("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				test2.send(pageNum);
				break;
			case POWER:
				EasyPagination<Clan> test3 = new EasyPagination<>(p, getMostPowerful(), ClansComparators.comparingByPower());
				test3.limit(menuSize());
				test3.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oMost Power&7&l]&7&m-----------");
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
				EasyPagination<Clan> test4 = new EasyPagination<>(p, getMostPowerful(), ClansComparators.comparingByEntity());
				test4.limit(menuSize());
				test4.setHeader((player, chunks) -> {
					if (LabyrinthProvider.getInstance().isNew()) {
						chunks.then("&7&m------------&7&l[&#ff7700&oHighest K/D&7&l]&7&m-----------");
					} else {
						chunks.then("&7&m------------&7&l[&6&oHighest K/D&7&l]&7&m-----------");
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
		MONEY, WINS, POWER, KILLS, NAME
	}

}
