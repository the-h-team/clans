package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.ClanWar;
import com.github.sanctum.clans.construct.extra.StartProcedure;
import com.github.sanctum.clans.construct.impl.CooldownCreate;
import com.github.sanctum.clans.construct.impl.CooldownRespawn;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.core.ClaimInteractEvent;
import com.github.sanctum.clans.events.core.ClaimResidentEvent;
import com.github.sanctum.clans.events.core.ClanCreateEvent;
import com.github.sanctum.clans.events.core.ClanCreatedEvent;
import com.github.sanctum.clans.events.core.LandPreClaimEvent;
import com.github.sanctum.clans.events.core.WildernessInhabitantEvent;
import com.github.sanctum.clans.events.damage.PlayerKillPlayerEvent;
import com.github.sanctum.clans.events.damage.PlayerPunchPlayerEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.HFEncoded;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class PlayerEventListener implements Listener {
	private final Set<Player> test = new HashSet<>();

	public PlayerEventListener() {
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onClaim(LandPreClaimEvent e) {
		EconomyProvision eco = EconomyProvision.getInstance();
		if (eco.isValid()) {
			if (ClansAPI.getData().getEnabled("Clans.land-claiming.charge")) {
				double cost = ClansAPI.getData().getMain().readValue(f -> f.getDouble("Clans.land-claiming.amount"));
				boolean test = eco.withdraw(BigDecimal.valueOf(cost), e.getClaimer(), e.getClaimer().getWorld().getName()).orElse(false);
				if (test) {
					Clan.ACTION.sendMessage(e.getClaimer(), "&a+1 &f| &6$" + cost);
				} else {
					double amount = eco.balance(e.getClaimer()).orElse(0.0);
					Clan.ACTION.sendMessage(e.getClaimer(), Clan.ACTION.notEnough(cost - amount));
				}

			}

		}
	}

	@Subscribe
	public void onResident(ClaimResidentEvent e) {
		Player p = e.getResident().getPlayer();
		if (!test.contains(p)) {
			ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
				a.getBar().addPlayer(p);
				a.getBar().setVisible(true);
				a.getBar().setProgress(1.0);
				a.getBar().setTitle(StringUtils.use(e.getClaim().getClan().getColor() + e.getClaim().getClan().getDescription()).translate());
				test.add(p);
			});
		}
	}

	@Subscribe
	public void onWild(WildernessInhabitantEvent e) {
		Player p = e.getPlayer();
		if (test.contains(p)) {
			ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
				a.getBar().removePlayer(p);
				test.remove(p);
			});
		}
	}

	@Subscribe
	public void onInteract(DefaultEvent.Interact event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getBlock().get().getType().isInteractable()) {
			ClaimInteractEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new ClaimInteractEvent(event.getPlayer(), event.getBlock().get().getLocation(), ClaimInteractEvent.InteractionType.USE)).run();
			if (e.isCancelled()) {
				e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
				event.setCancelled(e.isCancelled());
			}
		}
	}

	@Subscribe
	public void onPunch(PlayerPunchPlayerEvent e) {
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

	@Subscribe
	public void onClanCreate(ClanCreateEvent event) {
		if (event.getMaker().isOnline()) {
			Player p = event.getMaker().getPlayer();
			if (ClansAPI.getInstance().isNameBlackListed(event.getClanName())) {
				String command = ClansAPI.getData().getMain().getConfig().getString("Clans.name-blacklist." + event.getClanName() + ".action");
				event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Clan.ACTION.format(command, "{PLAYER}", p.getName()));
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

	@Subscribe
	public void onClanCreated(ClanCreatedEvent e) {
		DefaultClan c = e.getClan();
		if (ClansAPI.getData().getEnabled("Clans.land-claiming.claim-influence.allow")) {
			if (ClansAPI.getData().getString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
				c.addMaxClaim(12);
			}
		}
	}

	@Subscribe
	public void onPlayerKillPlayer(PlayerKillPlayerEvent e) {
		Player p = e.getVictim();
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (!Bukkit.getOnlinePlayers().contains(p)) {
			return;
		}
		Player killer = e.getKiller();
		if (killer != null) {

			ClansAPI.getInstance().getAssociate(killer).ifPresent(ClanAssociate::killed);

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
									Clan.ACTION.sendMessage(killer, "&c&oYou've been stopped from spawn camping.");
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
									Clan.ACTION.sendMessage(killer, "&c&oYou've been stopped from spawn camping.");
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

	@NotNull ClanCooldown creationCooldown(UUID id) {
		ClanCooldown target = null;
		for (ClanCooldown c : ClansAPI.getData().COOLDOWNS) {
			if (c.getAction().equals("Clans:create-limit") && c.getId().equals(id.toString())) {
				target = c;
				break;
			}
		}
		if (target == null) {
			target = new CooldownCreate(id);
			if (!ClansAPI.getData().COOLDOWNS.contains(target)) {
				target.save();
			}
		}
		return target;
	}

	@EventHandler
	public void onCommandWar(PlayerCommandPreprocessEvent e) {
		List<String> CMDS = ClansAPI.getData().WAR_BLOCKED_CMDS;
		ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
			if (a.getClan().getCurrentWar() != null) {
				if (!a.getClan().getCurrentWar().getArenaTimer().isComplete()) {
					for (String cmd : CMDS) {
						if (StringUtils.use(e.getMessage()).containsIgnoreCase(cmd)) {
							e.setCancelled(true);
						}
					}
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {

		final Player p = e.getPlayer();

		final ClansAPI API = ClansAPI.getInstance();

		final ClanAssociate associate = API.getAssociate(p.getName()).orElse(null);

		StartProcedure.CLAIM_TASK.joinTask(p);

		if (associate != null) {
			if (associate.isValid()) {
				if (Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")) {
					ClanDisplayName.remove(associate);
					if (ClansAPI.getData().prefixedTagsAllowed()) {
						ClanDisplayName.set(p, ClansAPI.getData().prefixedTag(API.getClan(p.getUniqueId()).getColor(), API.getClan(p.getUniqueId()).getName()));
					}
				}
			}
		} else {
			if (Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")) {
				ClanDisplayName.remove(p);
			}
		}
		if (p.isOp()) {
			if (ClansAPI.getData().assertDefaults()) {
				Clan.ACTION.sendMessage(p, "&b&oUpdated configuration to the latest plugin version.");
			}
			ClansUpdate check = new ClansUpdate(ClansAPI.getInstance().getPlugin());
			try {
				if (check.hasUpdate()) {
					Clan.ACTION.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬oO[&fUpdate&b&l&m]Oo▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					Clan.ACTION.sendMessage(p, "&eNew version: &3Clans [Pro] &f" + check.getLatest());
					Clan.ACTION.sendMessage(p, "&e&oDownload: &f&n" + check.getResource());
					Clan.ACTION.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				}
			} catch (Exception ignored) {
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeave(PlayerQuitEvent e) {
		final Player p = e.getPlayer();
		ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null) {
			if (ClansAPI.getData().prefixedTagsAllowed()) {
				ClanDisplayName.remove(p);
			}
			Clan.ACTION.forfeitWar(associate);
		}
		ClansAPI.getData().RESIDENTS.removeIf(r -> r.getPlayer().getUniqueId().equals(p.getUniqueId()));
		ClansAPI.getData().INHABITANTS.remove(p);
		StartProcedure.CLAIM_TASK.leaveTask(p);
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
							Schedule.sync(() -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Clan.ACTION.color(Clan.ACTION.getPrefix() + " &8Current points: &3" + c.getCurrentWar().getPoints())))).cancelAfter(4).repeat(1, 1);
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
							Schedule.sync(() -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Clan.ACTION.color(Clan.ACTION.getPrefix() + " &8Current points: &3" + c.getCurrentWar().getPoints())))).cancelAfter(4).repeat(1, 1);
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
			PlayerKillPlayerEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new PlayerKillPlayerEvent(p, target)).run();

			ClanAction clanUtil = e.getUtil();
			if (ClansAPI.getInstance().getClanID(e.getKiller().getUniqueId()) != null) {
				Clan kill = ClansAPI.getInstance().getClan(e.getKiller().getUniqueId());
				if (ClansAPI.getInstance().getClanID(e.getVictim().getUniqueId()) != null) {
					if (!ClansAPI.getInstance().getClanID(e.getKiller().getUniqueId()).equals(ClansAPI.getInstance().getClanID(e.getVictim().getUniqueId()))) {
						Clan dead = ClansAPI.getInstance().getClan(e.getVictim().getUniqueId());
						if (kill instanceof DefaultClan) {
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

			if (ClansAPI.getInstance().getClanID(e.getKiller().getUniqueId()) == null) {
				if (ClansAPI.getInstance().getClanID(e.getVictim().getUniqueId()) != null) {
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
		ClaimInteractEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation(), ClaimInteractEvent.InteractionType.USE)).run();
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
		ClaimInteractEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation(), ClaimInteractEvent.InteractionType.USE)).run();
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

}
