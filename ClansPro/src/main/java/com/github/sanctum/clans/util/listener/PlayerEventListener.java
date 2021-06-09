package com.github.sanctum.clans.util.listener;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ScoreTag;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownCreate;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownRespawn;
import com.github.sanctum.clans.construct.extra.misc.ClanWar;
import com.github.sanctum.clans.util.InteractionType;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.clans.util.events.clans.ClaimInteractEvent;
import com.github.sanctum.clans.util.events.clans.ClaimResidentEvent;
import com.github.sanctum.clans.util.events.clans.ClanCreateEvent;
import com.github.sanctum.clans.util.events.clans.ClanCreatedEvent;
import com.github.sanctum.clans.util.events.clans.WildernessInhabitantEvent;
import com.github.sanctum.clans.util.events.damage.PlayerKillPlayerEvent;
import com.github.sanctum.clans.util.events.damage.PlayerPunchPlayerEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.HFEncoded;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.Synchronous;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

public class PlayerEventListener implements Listener {

	public ClanCooldown creationCooldown(UUID id) {
		ClanCooldown target = null;
		for (ClanCooldown c : ClansPro.getInstance().dataManager.COOLDOWNS) {
			if (c.getAction().equals("Clans:create-limit") && c.getId().equals(id.toString())) {
				target = c;
				break;
			}
		}
		if (target == null) {
			target = new CooldownCreate(id);
			if (!ClansPro.getInstance().dataManager.COOLDOWNS.contains(target)) {
				target.save();
			}
		}
		return target;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onWarPunch(PlayerPunchPlayerEvent e) {
		Player attacker = e.getAttacker();
		Cooldown test = Cooldown.getById("ClansPro-war-respawn-" + e.getVictim().getUniqueId().toString());
		if (test != null) {
			if (!test.isComplete()) {
				if (test.getSecondsLeft() == 0) {
					Cooldown.remove(test);
					return;
				}
				e.getUtil().sendMessage(attacker, "&cYou must wait &6&l" + test.getSecondsLeft() + " &cseconds before doing this to me.");
				e.setCanHurt(false);
			} else {
				Cooldown.remove(test);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClanBuy(ClanCreateEvent event) {
		if (event.getMaker().isOnline()) {
			Player p = event.getMaker().getPlayer();
			if (ClansPro.getInstance().isNameBlackListed(event.getName())) {
				String command = ClansAPI.getData().getMain().getConfig().getString("Clans.name-blacklist." + event.getName() + ".action");
				event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), DefaultClan.action.format(command, "{PLAYER}", p.getName()));
				event.setCancelled(true);
			}
			if (p != null && ClansAPI.getData().getEnabled("Clans.creation.cooldown.enabled")) {
				if (creationCooldown(p.getUniqueId()).isComplete()) {
					creationCooldown(p.getUniqueId()).setCooldown();
				} else {
					event.setCancelled(true);
					event.stringLibrary().sendMessage(p, "&c&oYou can't do this right now.");
					event.stringLibrary().sendMessage(p, creationCooldown(p.getUniqueId()).fullTimeLeft());
					return;
				}
			}
			if (ClansAPI.getData().getEnabled("Clans.creation.charge")) {
				double amount = ClansAPI.getData().getMain().getConfig().getDouble("Clans.creation.amount");
				Optional<Boolean> opt = EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName());

				boolean success = opt.orElse(false);
				if (!success) {
					event.setCancelled(true);
					event.stringLibrary().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClanCreate(ClanCreatedEvent e) {
		DefaultClan c = e.getClan();
		if (ClansAPI.getData().getEnabled("Clans.land-claiming.claim-influence.allow")) {
			if (ClansAPI.getData().getString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
				c.addMaxClaim(12);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerKillPlayerEvent e) {
		Player p = e.getVictim();
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (!Bukkit.getOnlinePlayers().contains(p)) {
			return;
		}
		Player killer = e.getKiller();
		if (killer != null) {

			ClanAssociate associate2 = ClansAPI.getInstance().getAssociate(killer).orElse(null);

			if (associate2 != null) {
				associate2.killed();
			}

			if (associate != null) {
				Clan c = associate.getClan();
				if (c.getCurrentWar() != null) {
					if (c.getCurrentWar().warActive()) {
						ClanWar team1 = c.getCurrentWar();
						if (team1.isRed()) {
							try {
								Location blue = (Location) new HFEncoded(ClansAPI.getData().arenaBlueTeamFile().getConfig().getString("spawn")).deserialized();
								Location red = (Location) new HFEncoded(ClansAPI.getData().arenaRedTeamFile().getConfig().getString("spawn")).deserialized();
								if (killer.getLocation().distance(red) <= 20) {
									for (Entity ent : killer.getNearbyEntities(8, 8, 8)) {
										if (ent instanceof Player) {
											Player target = (Player) ent;
											if (!c.getCurrentWar().getParticipants().contains(target)) {
												target.teleport(blue);
											}
										}
									}
									killer.teleport(blue);
									DefaultClan.action.sendMessage(killer, "&c&oYou've been stopped from spawn camping.");
								}
							} catch (IOException | ClassNotFoundException ex) {
								ex.printStackTrace();
							}
						} else {
							try {
								Location blue = (Location) new HFEncoded(ClansAPI.getData().arenaBlueTeamFile().getConfig().getString("spawn")).deserialized();
								Location red = (Location) new HFEncoded(ClansAPI.getData().arenaRedTeamFile().getConfig().getString("spawn")).deserialized();
								if (killer.getLocation().distance(blue) <= 20) {
									for (Entity ent : killer.getNearbyEntities(8, 8, 8)) {
										if (ent instanceof Player) {
											Player target = (Player) ent;
											if (!c.getCurrentWar().getParticipants().contains(target)) {
												target.teleport(red);
											}
										}
									}
									killer.teleport(red);
									DefaultClan.action.sendMessage(killer, "&c&oYou've been stopped from spawn camping.");
								}
							} catch (IOException | ClassNotFoundException ex) {
								ex.printStackTrace();
							}
						}
						e.setKeepInventory(true);
						e.setClearDrops(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onCommandWar(PlayerCommandPreprocessEvent e) {
		List<String> cmds = ClansAPI.getData().getMain().getConfig().getStringList("Clans.war.blocked-commands");
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
		if (associate != null) {
			if (associate.getClan().getCurrentWar() != null) {
				if (!associate.getClan().getCurrentWar().getArenaTimer().isComplete()) {
					for (String cmd : cmds) {
						if (StringUtils.use(e.getMessage()).containsIgnoreCase(cmd)) {
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		final ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (Claim.action.isEnabled()) {
			Synchronous sync = Schedule.sync(() -> {
				if (!ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
					WildernessInhabitantEvent event = new WildernessInhabitantEvent(p);
					Bukkit.getPluginManager().callEvent(event);
				} else {
					ClaimResidentEvent event = new ClaimResidentEvent(p);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						ClansAPI.getData().INHABITANTS.remove(event.getResident().getPlayer());
						if (event.getClaim().isActive()) {
							if (DefaultClan.action.getClanTag(event.getClaim().getOwner()) == null) {
								event.getClaim().remove();
								return;
							}
							if (!event.getClaim().getId().equals(event.getResident().getLastKnown().getId())) {
								if (event.getResident().isNotificationSent()) {
									if (!event.getResident().getLastKnown().getOwner().equals(event.getResident().getCurrent().getOwner())) {
										event.getResident().setNotificationSent(false);
										if (ClansAPI.getInstance().isInClan(event.getResident().getPlayer().getUniqueId())) {
											if (event.getResident().getLastKnown().getOwner().equals(ClansAPI.getInstance().getClan(event.getResident().getPlayer()).get().getId().toString())) {
												event.getResident().setTraversedDifferent(true);
											}
										}
										event.getResident().updateLastKnown(event.getClaim());
										event.getResident().updateJoinTime(System.currentTimeMillis());
									}
								}
							}
							if (!event.getResident().isNotificationSent()) {
								event.playTitle();
								event.getResident().setNotificationSent(true);
							} else {
								if (event.getResident().hasTraversedDifferent()) {
									if (ClansAPI.getInstance().getClan(event.getResident().getPlayer()).isPresent()) {
										event.getResident().setTraversedDifferent(false);
										event.getResident().setNotificationSent(false);
										event.getResident().updateJoinTime(System.currentTimeMillis());
									}
								}
							}
						}
					}
				}
			}).cancelAfter(p);
			if (ClansAPI.getData().getEnabled("Formatting.console-debug")) {
				sync.debug();
			}
			sync.repeatReal(2, 20);
		}
		Synchronous sync = Schedule.sync(() -> {
			if (associate != null) {
				if (!associate.isValid()) {
					Schedule.sync(() -> ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(p.getUniqueId()))).wait(1);
					return;
				}
				FileManager cl = DataManager.FileType.CLAN_FILE.get(associate.getClan().getId().toString());
				if (cl.getConfig().getString("name") == null) {
					Schedule.sync(() -> ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(p.getUniqueId()))).wait(1);
					FileManager user = ClansAPI.getData().get(p);
					user.getConfig().set("Clan", null);
					user.saveConfig();
					cl.delete();
					DefaultClan.action.sendMessage(p, "Your clan was disbanded due to owner dismissal..");
				}
				if (cl.exists() && !cl.getConfig().getStringList("members").contains(p.getUniqueId().toString())) {
					FileManager user = ClansAPI.getData().get(p);
					user.getConfig().set("Clan", null);
					user.saveConfig();
					DefaultClan.action.sendMessage(p, "&c&oA staff member manually removed you from your clan. Adjusting player data.");
				}
			}
		}).cancelAfter(p)
				.applyAfter(() -> {
					if (associate != null) {
						if (!associate.isValid()) {
							Schedule.sync(() -> ClansAPI.getData().ASSOCIATES.removeIf(a -> a.getPlayer().getUniqueId().equals(p.getUniqueId()))).wait(1);
							return;
						}
						Clan c = associate.getClan();
						for (ClanCooldown clanCooldown : c.getCooldowns()) {
							if (clanCooldown.isComplete()) {
								ClanCooldown.remove(clanCooldown);
								c.broadcast(MessageFormat.format(ClansAPI.getData().getMessage("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
							}
						}

						if (c.getCurrentWar() != null) {
							if (ClansAPI.getData().arenaFile().exists()) {
								if (c.getCurrentWar().getArenaTimer().isComplete()) {
									if (c.getCurrentWar().getTargeted() != null) {
										Clan winner = null;
										Clan loser = null;
										if (c.getCurrentWar().getPoints() > c.getCurrentWar().getTargeted().getCurrentWar().getPoints()) {
											winner = c;
											loser = c.getCurrentWar().getTargeted();
											Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName() + " &e(&6&l" + winner.getCurrentWar().getPoints() + "&e)"));
										}
										if (c.getCurrentWar().getTargeted().getCurrentWar().getPoints() > c.getCurrentWar().getPoints()) {
											winner = c.getCurrentWar().getTargeted();
											loser = c;
											Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6concluded with winner " + winner.getName() + " &e(&6&l" + winner.getCurrentWar().getPoints() + "&e)"));
										}
										if (c.getCurrentWar().getPoints() == c.getCurrentWar().getTargeted().getCurrentWar().getPoints()) {
											Bukkit.broadcastMessage(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &4&oWar &6between &4" + c.getName() + " &6and &4" + c.getCurrentWar().getTargeted().getName() + " &6finished in a draw."));
										}
										if (winner != null) {
											String loserC;
											String winnerC;
											if (loser.getCurrentWar().isRed()) {
												loserC = "&c";
												winnerC = "&9";
											} else {
												loserC = "&9";
												winnerC = "&c";
											}
											winner.givePower((loser.getPower() / 2) + winner.getCurrentWar().getPoints());
											for (Player par : winner.getCurrentWar().getParticipants()) {
												par.giveExp((winner.getCurrentWar().getPoints() * 2));
												final boolean success;
												Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(18.14 * winner.getCurrentWar().getPoints()), par);

												success = opt.orElse(false);

												if (!success) {
													if (par.isOp()) {
														DefaultClan.action.sendMessage(par, "&cYou don't have a valid economy system installed. No one received any money.");
													}
												}

												par.sendTitle(DefaultClan.action.color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + loser.getCurrentWar().getPoints()), DefaultClan.action.color("&aWe win."), 10, 45, 10);
											}
											loser.takePower((loser.getPower() / 2) + winner.getCurrentWar().getPoints());
											for (Player par : loser.getCurrentWar().getParticipants()) {
												par.giveExp((winner.getCurrentWar().getPoints() * 2));
												final boolean success;
												Optional<Boolean> opt = EconomyProvision.getInstance().deposit(BigDecimal.valueOf(10.14 * loser.getCurrentWar().getPoints()), par);

												success = opt.orElse(false);
												if (!success) {
													if (par.isOp()) {
														DefaultClan.action.sendMessage(par, "&cYou don't have a valid economy system installed. No one received any money.");
													}
												}
												par.sendTitle(DefaultClan.action.color(winnerC + winner.getCurrentWar().getPoints() + " &f/ " + loserC + loser.getCurrentWar().getPoints()), DefaultClan.action.color("&cWe lose."), 10, 45, 10);

											}
										}
										ClanCooldown.remove(c.getCurrentWar().getArenaTimer());
										c.getCurrentWar().conclude();
									}
								} else {
									for (Player par : c.getCurrentWar().getParticipants()) {
										par.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DefaultClan.action.color("&8Time left: &f(&3" + c.getCurrentWar().getArenaTimer().getMinutesLeft() + ":" + c.getCurrentWar().getArenaTimer().getSecondsLeft() + "&f)")));
									}
									for (Player par : c.getCurrentWar().getTargeted().getCurrentWar().getParticipants()) {
										par.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DefaultClan.action.color("&8Time left: &f(&3" + c.getCurrentWar().getArenaTimer().getMinutesLeft() + ":" + c.getCurrentWar().getArenaTimer().getSecondsLeft() + "&f)")));
									}
								}
							}
						}
						for (String ally : c.getAllyList()) {
							if (!DefaultClan.action.getAllClanIDs().contains(ally)) {
								c.removeAlly(HUID.fromString(ally));
								break;
							}
						}
						for (String enemy : c.getEnemyList()) {
							if (!DefaultClan.action.getAllClanIDs().contains(enemy)) {
								c.removeEnemy(HUID.fromString(enemy));
								break;
							}
						}
						for (String allyRe : c.getAllyRequests()) {
							if (!DefaultClan.action.getAllClanIDs().contains(allyRe)) {
								FileManager cl = ClansAPI.getData().getClanFile(c);
								List<String> allies = c.getAllyList();
								allies.remove(allyRe);
								cl.getConfig().set("ally-requests", allies);
								cl.saveConfig();
								break;
							}
						}
					}
					for (ClanCooldown clanCooldown : ClansPro.getInstance().dataManager.COOLDOWNS) {
						if (clanCooldown.getId().equals(p.getUniqueId().toString())) {
							if (clanCooldown.isComplete()) {
								Schedule.sync(() -> ClanCooldown.remove(clanCooldown)).run();
								DefaultClan.action.sendMessage(p, MessageFormat.format(ClansAPI.getData().getMessage("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
							}
						}
					}
				});
		if (ClansAPI.getData().getEnabled("Formatting.console-debug")) {
			sync.debug();
		}
		sync.repeat(0, 10);

		if (associate != null) {
			FileManager clan = DataManager.FileType.CLAN_FILE.get(associate.getClan().getId().toString());
			if (associate.isValid()) {
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					ScoreTag.set(p, ClansAPI.getData().prefixedTag(ClansAPI.getInstance().getClan(p.getUniqueId()).getColor(), ClansAPI.getInstance().getClan(p.getUniqueId()).getName()));
				}
				ClansAPI.getData().CLAN_ENEMY_MAP.put(associate.getClan().getId().toString(), new ArrayList<>(clan.getConfig().getStringList("enemies")));
				ClansAPI.getData().CLAN_ALLY_MAP.put(associate.getClan().getId().toString(), new ArrayList<>(clan.getConfig().getStringList("allies")));
			}
		}
		ClansAPI.getData().CHAT_MODE.put(p, "GLOBAL");
		if (p.isOp()) {
			if (ClansAPI.getData().assertDefaults()) {
				DefaultClan.action.sendMessage(p, "&b&oUpdated configuration to the latest plugin version.");
			}
			ClansUpdate check = new ClansUpdate(ClansPro.getInstance());
			try {
				if (check.hasUpdate()) {
					DefaultClan.action.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬oO[&fUpdate&b&l&m]Oo▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					DefaultClan.action.sendMessage(p, "&eNew version: &3Clans [Pro] &f" + check.getLatest());
					DefaultClan.action.sendMessage(p, "&e&oDownload: &f&n" + check.getResource());
					DefaultClan.action.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				}
			} catch (Exception ignored) {
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeave(PlayerQuitEvent e) {
		final Player p = e.getPlayer();
		if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
			Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
			if (ClansAPI.getData().prefixedTagsAllowed()) {
				ScoreTag.remove(p);
			}
			int clanSize = c.getMembersList().length;
			int offlineSize = 0;
			for (String player : c.getMembersList()) {
				if (!Bukkit.getOfflinePlayer(UUID.fromString(player)).isOnline()) {
					offlineSize++;
				}
			}
			if (offlineSize == clanSize) {
				ClansAPI.getData().CLAN_ENEMY_MAP.clear();
				ClansAPI.getData().CLAN_ALLY_MAP.clear();
			} else {
				DefaultClan.action.forfeitWar(p, c.getId().toString());
			}
		}
		ClansAPI.getData().RESIDENTS.removeIf(r -> r.getPlayer().getUniqueId().equals(p.getUniqueId()));
		ClansAPI.getData().INHABITANTS.remove(p);
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent e) {
		if (ClansAPI.getInstance().getClaimManager().isInClaim(e.getTo())) {
			e.setCanCreatePortal(ClansAPI.getData().getEnabled("Clans.land-claiming.portals-in-claims"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null && associate.isValid()) {
			if (!(associate.getClan() instanceof DefaultClan)) return;
			DefaultClan c = (DefaultClan) associate.getClan();
			if (c.getCurrentWar() != null) {
				if (c.getCurrentWar().warActive()) {
					CooldownRespawn respawn = new CooldownRespawn(p.getUniqueId());
					respawn.save();
					if (c.getCurrentWar().isRed()) {
						try {
							Location red = (Location) new HFEncoded(ClansAPI.getData().arenaRedTeamFile().getConfig().getString("re-spawn")).deserialized();
							e.setRespawnLocation(red);
							for (Entity ent : p.getNearbyEntities(8, 8, 8)) {
								if (ent instanceof Player) {
									Player target = (Player) ent;
									if (!c.getCurrentWar().getParticipants().contains(target)) {
										Vector v = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
										target.setVelocity(v);
									}
								}
							}
							Schedule.sync(() -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &8Current points: &3" + c.getCurrentWar().getPoints())))).cancelAfter(4).repeat(1, 1);
						} catch (ClassNotFoundException | IOException ignored) {

						}
					} else {
						try {
							Location blue = (Location) new HFEncoded(ClansAPI.getData().arenaBlueTeamFile().getConfig().getString("re-spawn")).deserialized();
							e.setRespawnLocation(blue);
							for (Entity ent : p.getNearbyEntities(8, 8, 8)) {
								if (ent instanceof Player) {
									Player target = (Player) ent;
									if (!c.getCurrentWar().getParticipants().contains(target)) {
										Vector v = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
										target.setVelocity(v);
									}
								}
							}
							Schedule.sync(() -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DefaultClan.action.color(DefaultClan.action.getPrefix() + " &8Current points: &3" + c.getCurrentWar().getPoints())))).cancelAfter(4).repeat(1, 1);
						} catch (ClassNotFoundException | IOException ignored) {

						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getKiller() != null) {
			Player p = event.getEntity().getKiller();
			Player target = event.getEntity();
			PlayerKillPlayerEvent e = new PlayerKillPlayerEvent(p, target);
			Bukkit.getPluginManager().callEvent(e);

			ClanAction clanUtil = e.getUtil();
			if (clanUtil.getClanID(e.getKiller().getUniqueId()) != null) {
				Clan kill = ClansAPI.getInstance().getClan(e.getKiller().getUniqueId());
				if (clanUtil.getClanID(e.getVictim().getUniqueId()) != null) {
					if (!clanUtil.getClanID(e.getKiller().getUniqueId()).equals(clanUtil.getClanID(e.getVictim().getUniqueId()))) {
						Clan dead = ClansAPI.getInstance().getClan(e.getVictim().getUniqueId());
						if (((DefaultClan) kill).getCurrentWar() != null) {
							((DefaultClan) kill).getCurrentWar().addPoint();
							Bukkit.broadcastMessage(e.stringLibrary().color(e.stringLibrary().getPrefix() + " &a&o+1 point for clan &6&l" + kill.getName()));
						}
						kill.givePower(0.11);
						dead.takePower(0.11);
					}
				} else {
					// victim not in a clan
					kill.givePower(0.11);
				}
			}

			if (clanUtil.getClanID(e.getKiller().getUniqueId()) == null) {
				if (clanUtil.getClanID(e.getVictim().getUniqueId()) != null) {
					Clan dead = ClansAPI.getInstance().getClan(e.getVictim().getUniqueId());
					dead.takePower(0.11);
				}
			}

			if (e.isKeepInventory()) {
				event.setKeepInventory(true);
			}
			if (e.isClearDrops()) {
				event.getDrops().clear();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBucketRelease(PlayerBucketEmptyEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation(), InteractionType.USE);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
			final Material bucketType = event.getBucket();
			if (ClansAPI.getData().getEnabled("Clans.land-claiming.debug")) {
				Schedule.sync(() -> {
					event.getBlock().setType(Material.AIR);
					event.getPlayer().getInventory().getItemInMainHand().setType(bucketType);
					event.getPlayer().updateInventory();
				}).run();
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation(), InteractionType.USE);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
			final Material bucketType = event.getBucket();
			final Material type = event.getBlock().getType();
			if (ClansAPI.getData().getEnabled("Clans.land-claiming.debug")) {
				Schedule.sync(() -> {
					event.getBlock().setType(type);
					event.getPlayer().getInventory().getItemInMainHand().setType(bucketType);
					event.getPlayer().updateInventory();
				}).run();
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType().isInteractable()) {
			ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getClickedBlock().getLocation(), InteractionType.USE);
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
				event.setCancelled(e.isCancelled());
			}
		}
	}

}
