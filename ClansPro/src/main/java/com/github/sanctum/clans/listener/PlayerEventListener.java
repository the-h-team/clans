package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.impl.CooldownCreate;
import com.github.sanctum.clans.construct.impl.CooldownRespawn;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.core.ClaimInteractEvent;
import com.github.sanctum.clans.events.core.ClaimResidentEvent;
import com.github.sanctum.clans.events.core.ClanCreateEvent;
import com.github.sanctum.clans.events.core.ClanCreatedEvent;
import com.github.sanctum.clans.events.core.ClanWarActiveEvent;
import com.github.sanctum.clans.events.core.ClanWarStartEvent;
import com.github.sanctum.clans.events.core.ClanWarWonEvent;
import com.github.sanctum.clans.events.core.LandPreClaimEvent;
import com.github.sanctum.clans.events.core.WildernessInhabitantEvent;
import com.github.sanctum.clans.events.damage.PlayerKillPlayerEvent;
import com.github.sanctum.clans.events.damage.PlayerPunchPlayerEvent;
import com.github.sanctum.clans.events.damage.PlayerShootPlayerEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Experimental;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.Schedule;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class PlayerEventListener implements Listener {

	public PlayerEventListener() {
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onClaim(LandPreClaimEvent e) {
		EconomyProvision eco = EconomyProvision.getInstance();
		if (eco.isValid()) {
			if (ClansAPI.getData().isTrue("Clans.land-claiming.charge")) {
				double cost = ClansAPI.getData().getMain().read(f -> f.getDouble("Clans.land-claiming.amount"));
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

	@Subscribe()
	@Experimental("This event works but isn't guaranteed legacy safe.")
	@Note("Its primary purpose is for Cortex [Clans]")
	public void onBossBar(ClaimResidentEvent e) {
		Player p = e.getResident().getPlayer();
		ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
			if (!a.getBar().isVisible()) {
				a.getBar().addPlayer(p);
				a.getBar().setVisible(true);
				a.getBar().setProgress(1.0);
			}
			if (a.getClan().getPalette().isGradient()) {
				a.getBar().setTitle(a.getClan().getPalette().toGradient().context(a.getClan().getDescription()).translate());
			} else {
				a.getBar().setTitle(StringUtils.use(e.getClaim().getClan().getPalette().getStart() + e.getClaim().getClan().getDescription()).translate());
			}
		});
	}

	@Subscribe
	public void onWild(WildernessInhabitantEvent e) {
		Player p = e.getPlayer();
		ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
			if (a.getBar().isVisible()) {
				a.getBar().removePlayer(p);
				a.getBar().setVisible(false);
			}
		});
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
		Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("ClansPro-war-respawn-" + e.getVictim().getUniqueId().toString());
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

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onWarPunch(PlayerPunchPlayerEvent e) {
		ClansAPI.getInstance().getAssociate(e.getAttacker()).ifPresent(a -> {
			War w = ClansAPI.getInstance().getArenaManager().get(a);
			if (w != null && !w.isRunning()) {
				ClansAPI.getInstance().getAssociate(e.getVictim()).ifPresent(as -> {
					if (w.getTeam(as.getClan()) != null) {
						e.getUtil().sendMessage(e.getAttacker(), "&cYou are in a waiting period. War not started yet.");
						e.setCanHurt(false);
					}
				});
			}
		});
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

	@Subscribe
	public void onClanCreate(ClanCreateEvent event) {
		if (event.getMaker().isOnline()) {
			Player p = event.getMaker().getPlayer();
			if (ClansAPI.getInstance().isNameBlackListed(event.getClanName())) {
				String command = ClansAPI.getData().getMain().getRoot().getString("Clans.name-blacklist." + event.getClanName() + ".action");
				event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Clan.ACTION.format(command, "{PLAYER}", p.getName()));
				event.setCancelled(true);
			}
			if (p != null && ClansAPI.getData().isTrue("Clans.creation.cooldown.enabled")) {
				if (creationCooldown(p.getUniqueId()).isComplete()) {
					creationCooldown(p.getUniqueId()).setCooldown();
				} else {
					event.setCancelled(true);
					event.stringLibrary().sendMessage(p, "&c&oYou can't do this right now.");
					event.stringLibrary().sendMessage(p, creationCooldown(p.getUniqueId()).fullTimeLeft());
					return;
				}
			}
			if (ClansAPI.getData().isTrue("Clans.creation.charge")) {
				double amount = ClansAPI.getData().getMain().getRoot().getDouble("Clans.creation.amount");
				Optional<Boolean> opt = EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName());

				boolean success = opt.orElse(false);
				if (!success) {
					event.setCancelled(true);
					event.stringLibrary().sendMessage(p, "&c&oYou don't have enough money. Amount needed: &6" + amount);
				}
			}
		}
	}

	private String calc(long i) {
		String val = String.valueOf(i);
		int size = String.valueOf(i).length();
		if (size == 1) {
			val = "0" + i;
		}
		return val;
	}

	@Subscribe
	public void onWarStart(ClanWarStartEvent e) {
		War w = e.getWar();
		TimeWatch.Recording r = e.getRecording();
		Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("war-" + w.getId() + "-start");
		if (test != null) {
			String time = calc(r.getMinutes()) + ":" + calc(r.getSeconds());
			if (time.equals("01:00")) {
				e.start();
				Cooldown.remove(test);
			} else {
				Message m = LabyrinthProvider.getService(Service.MESSENGER).getNewMessage();
				String t = calc(test.getMinutesLeft()) + ":" + calc(test.getSecondsLeft());
				w.forEach(a -> {
					Player p = a.getPlayer().getPlayer();
					if (p != null) {
						m.setPlayer(p).action("&2War start&f: &e" + t);
					}
				});
			}
		}
	}

	@Subscribe
	public void onWarWatch(ClanWarActiveEvent e) {
		Cooldown timer = e.getWar().getTimer();
		Message msg = LabyrinthProvider.getService(Service.MESSENGER).getNewMessage();
		e.getWar().forEach(a -> {
			Player p = a.getPlayer().getPlayer();
			if (p != null) {
				War.Team t = e.getWar().getTeam(a.getClan());
				int points = e.getWar().getPoints(t);
				String time = calc(timer.getMinutesLeft()) + ":" + calc(timer.getSecondsLeft());
				msg.setPlayer(p).action("&3Points&f:&b " + points + " &6| &3Time left&f:&e " + time);
			}
		});
	}

	@Subscribe
	public void onWarWin(ClanWarWonEvent e) {
		double reward = new Random().nextInt(e.getWinner().getValue()) + 0.17;
		e.getWinner().getKey().givePower(reward);
		e.getLosers().forEach((clan, integer) -> clan.takePower(reward));
	}

	@Subscribe
	public void onClanCreated(ClanCreatedEvent e) {
		DefaultClan c = e.getClan();
		if (ClansAPI.getData().isTrue("Clans.land-claiming.claim-influence.allow")) {
			if (ClansAPI.getData().getConfigString("Clans.land-claiming.claim-influence.dependence").equalsIgnoreCase("LOW")) {
				c.addMaxClaim(12);
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGH)
	public void onDamage(DefaultEvent.PlayerDamagePlayer event) {
		if (event.isPhysical()) {
			if (!Bukkit.getOnlinePlayers().contains(event.getVictim())) {
				return;
			}
			Player p = event.getPlayer();
			if (!ClansAPI.getData().getMain().getRoot().getStringList("Clans.world-whitelist").contains(p.getWorld().getName()))
				return;
			PlayerPunchPlayerEvent e = ClanVentBus.call(new PlayerPunchPlayerEvent(p, event.getVictim()));

			Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getAttacker()).orElse(null);
			Clan.Associate associate2 = ClansAPI.getInstance().getAssociate(e.getVictim()).orElse(null);
			if (associate != null) {
				Clan at = associate.getClan();
				if (associate2 != null) {
					if (at.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getAttacker(), e.stringLibrary().peacefulDeny());
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					Clan v = associate2.getClan();
					if (v.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getAttacker(), e.stringLibrary().peacefulDenyOther(v.getName()));
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (at.getId().equals(v.getId())) {

						e.setCanHurt(at.isFriendlyFire());
						if (!at.isFriendlyFire()) {
							e.getUtil().sendMessage(e.getAttacker(), e.stringLibrary().friendlyFire());
						}
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (at.getAllyList().contains(v.getId().toString())) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getAttacker(), e.stringLibrary().friendlyFire());
					}
				} else {
					if (at.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getAttacker(), e.stringLibrary().peacefulDeny());
					}
				}
			} else {
				if (associate2 != null) {
					Clan v = associate2.getClan();
					if (v.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getAttacker(), e.stringLibrary().peacefulDenyOther(v.getName()));
					}
				}
			}

			if (e.canHurt()) {
				event.setCancelled(e.canHurt());
			}
		} else {

			if (!Bukkit.getOnlinePlayers().contains(event.getVictim())) {
				return;
			}
			if (!ClansAPI.getData().getMain().getRoot().getStringList("Clans.world-whitelist").contains(event.getPlayer().getWorld().getName())) {
				return;
			}
			PlayerShootPlayerEvent e = ClanVentBus.call(new PlayerShootPlayerEvent(event.getPlayer(), event.getVictim()));

			Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getShooter()).orElse(null);
			Clan.Associate associate2 = ClansAPI.getInstance().getAssociate(e.getShot()).orElse(null);
			if (associate != null) {
				if (associate2 != null) {
					Clan a = associate.getClan();
					Clan b = associate2.getClan();
					if (a.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getShooter(), e.stringLibrary().peacefulDeny());
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (b.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getShooter(), e.stringLibrary().peacefulDenyOther(b.getName()));
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (a.getId().equals(b.getId())) {
						Clan at = ClansAPI.getInstance().getClan(e.getShooter().getUniqueId());
						e.setCanHurt(at.isFriendlyFire());
						if (!at.isFriendlyFire()) {
							e.getUtil().sendMessage(e.getShooter(), e.stringLibrary().friendlyFire());
						}
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (a.getAllyList().contains(b.getId().toString())) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getShooter(), e.stringLibrary().friendlyFire());
					}
				}
			}
			if (e.canHurt()) {
				event.setCancelled(e.canHurt());
			}

		}
	}

	@Subscribe
	public void onPlayerKillPlayer(PlayerKillPlayerEvent e) {
		Player p = e.getVictim();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (!Bukkit.getOnlinePlayers().contains(p)) {
			return;
		}
		Player killer = e.getKiller();
		if (killer != null) {

			ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
				a.getClan().givePower(0.11);
				a.countKill();
			});

			if (associate != null) {
				associate.getClan().takePower(0.11);
				War war = ClansAPI.getInstance().getArenaManager().get(associate);
				if (war != null) {
					if (war.isRunning()) {
						ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
							War.Team t = war.getTeam(a.getClan());
							int points = war.getPoints(t);
							war.setPoints(t, points + 1);
							a.getClan().broadcast("&aWe just scored 1 point");
						});
						e.setClearDrops(true);
						e.setKeepInventory(true);
					}
				}
			}
		}
	}

	@Subscribe
	public void onCommandWar(DefaultEvent.Communication e) {
		if (e.getCommunicationType() == DefaultEvent.Communication.Type.COMMAND) {
			DefaultEvent.Communication.ChatCommand cmd = e.getCommand().get();

			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {

				War w = ClansAPI.getInstance().getArenaManager().get(a);
				if (w != null) {
					if (w.isRunning()) {
						for (String c : ClansAPI.getData().WAR_BLOCKED_CMDS) {
							if (StringUtils.use(cmd.getText()).containsIgnoreCase(c)) {
								e.setCancelled(true);
							}
						}
					} else {
						if (cmd.get().length >= 1) {
							if (!(cmd.getText().equals("c"))) {
								Clan.ACTION.sendMessage(e.getPlayer(), "&cYou cannot do this while queued for a match! Use &6/c surrender &cto safely leave queue.");
								e.setCancelled(true);
							}
						}
					}
				}
			});
		}
	}

	@Subscribe(priority = Vent.Priority.HIGH, processCancelled = true)
	public void onPlayerJoin(DefaultEvent.Join e) {

		final Player p = e.getPlayer();

		final ClansAPI API = ClansAPI.getInstance();

		final Clan.Associate associate = API.getAssociate(p.getName()).orElse(null);

		ClansAPI.getInstance().getClaimManager().getTask().joinTask(p);

		if (associate != null) {
			if (associate.isValid()) {
				if (Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")) {
					if (ClansAPI.getData().prefixedTagsAllowed()) {
						if (associate.getClan().getPalette().isGradient()) {
							Clan c = associate.getClan();
							ClanDisplayName.set(associate, ClansAPI.getData().prefixedTag("", c.getPalette().toGradient().context(c.getName()).translate()));
						} else {
							ClanDisplayName.set(associate, ClansAPI.getData().prefixedTag(associate.getClan().getPalette().toString(), associate.getClan().getName()));

						}
					} else {
						ClanDisplayName.remove(associate);
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

	@Subscribe
	public void onPlayerLeave(DefaultEvent.Leave e) {
		final Player p = e.getPlayer();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null) {
			if (ClansAPI.getData().prefixedTagsAllowed()) {
				ClanDisplayName.remove(p);
			}
			War current = ClansAPI.getInstance().getArenaManager().get(associate);
			if (current != null) {
				Message m = Message.loggedFor(ClansAPI.getInstance().getPlugin()).setPrefix(ClansAPI.getInstance().getPrefix().joined());
				if (current.isRunning()) {
					if (current.getQueue().unque(associate)) {
						m.broadcast(associate.getNickname() + "&c has left the battlefield.");
						Schedule.sync(() -> {
							if (current.getQueue().associates().length == 0) {
								m.broadcast("&cThere is no one left in the arena. War in &7#&6" + current.getId() + " &chas reset.");
								current.stop();
								current.reset();
								return;
							}
							if (current.getQueue().count(associate.getClan()) == 0) {
								int alive = 0;
								for (Clan c : current.getQueue().teams()) {
									int count = current.getQueue().count(c);
									if (count != 0) {
										alive++;
									}
								}
								if (alive <= 1) {
									m.broadcast("&cEveryone has left the battlefield. War in &7#&6" + current.getId() + " &cconcluded with winning team &b" + current.getMostPoints().getKey().name());
									current.stop();
									current.reset();
								} else {
									current.getQueue().unque(associate);
								}
							}
						}).run();
					}
				} else {
					if (current.getQueue().associates().length <= ClansAPI.getData().getInt("Clans.war.que-needed") + 1) {
						if (current.avoid()) {
							current.reset();
							m.broadcast("&cEvery queued member has left the game. War in &7#&6" + current.getId() + " &cfailed to start.");
						}
					} else {
						Schedule.sync(() -> current.getQueue().unque(associate)).applyAfter(() -> {
							if (current.getQueue().count(associate.getClan()) == 0) {
								int alive = 0;
								for (Clan c : current.getQueue().teams()) {
									int count = current.getQueue().count(c);
									if (count != 0) {
										alive++;
									}
								}
								if (alive <= 1) {
									m.broadcast("&cEveryone has left the battlefield. War in &7#&6" + current.getId() + " &cconcluded with winning team &b" + current.getMostPoints().getKey().name());
									current.stop();
									current.reset();
								} else {
									current.getQueue().unque(associate);
								}
							}
						}).run();
					}
				}
			}
		}
		ClansAPI.getData().RESIDENTS.removeIf(r -> r.getPlayer().getUniqueId().equals(p.getUniqueId()));
		ClansAPI.getData().INHABITANTS.remove(p);
		ClansAPI.getInstance().getClaimManager().getTask().leaveTask(p);
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent e) {
		if (ClansAPI.getInstance().getClaimManager().isInClaim(e.getTo())) {
			e.setCanCreatePortal(ClansAPI.getData().isTrue("Clans.land-claiming.portals-in-claims"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null && associate.isValid()) {
			War war = ClansAPI.getInstance().getArenaManager().get(associate);
			if (war != null && war.isRunning()) {
				War.Team t = war.getTeam(associate.getClan());
				if (t.getSpawn() != null) {
					Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("ClansPro-war-respawn-" + p.getUniqueId().toString());
					if (test != null) {
						Cooldown.remove(test);
					}
					new CooldownRespawn(p.getUniqueId()).save();
					e.setRespawnLocation(t.getSpawn());
					for (Entity s : p.getNearbyEntities(20, 20, 20)) {
						if (s instanceof Player) {
							Player pl = (Player) s;
							Clan.Associate a = ClansAPI.getInstance().getAssociate(pl).orElse(null);
							if (a != null) {
								if (war.getTeam(a.getClan()) != null) {
									if (war.getTeam(a.getClan()) != t) {
										Location difference = pl.getLocation().subtract(p.getLocation());
										Vector normalizedDifference = difference.toVector().normalize();
										Vector multiplied = normalizedDifference.multiply(1.5);
										pl.setVelocity(multiplied);
									}
								}
							}
						}
					}
				} else {
					Clan.ACTION.sendMessage(p, "&cThe clan arena system isn't properly configured. Contact staff for help.");
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
			if (ClansAPI.getData().isTrue("Clans.land-claiming.debug")) {
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
			if (ClansAPI.getData().isTrue("Clans.land-claiming.debug")) {
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
