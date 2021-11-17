package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.actions.ClansUpdate;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.api.LogoHolder;
import com.github.sanctum.clans.construct.api.Teleport;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.AsynchronousLoanableTask;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.FancyLogoAppendage;
import com.github.sanctum.clans.construct.extra.ReservedLogoCarrier;
import com.github.sanctum.clans.construct.impl.CooldownRespawn;
import com.github.sanctum.clans.construct.impl.SimpleEntry;
import com.github.sanctum.clans.event.TimerEvent;
import com.github.sanctum.clans.event.associate.AssociateClaimEvent;
import com.github.sanctum.clans.event.claim.ClaimInteractEvent;
import com.github.sanctum.clans.event.clan.ClanCooldownCompleteEvent;
import com.github.sanctum.clans.event.player.PlayerCooldownCompleteEvent;
import com.github.sanctum.clans.event.player.PlayerKillPlayerEvent;
import com.github.sanctum.clans.event.player.PlayerLookAtCarrierEvent;
import com.github.sanctum.clans.event.player.PlayerPunchPlayerEvent;
import com.github.sanctum.clans.event.player.PlayerShootPlayerEvent;
import com.github.sanctum.clans.event.war.WarActiveEvent;
import com.github.sanctum.clans.event.war.WarWonEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.TextChunk;
import com.github.sanctum.labyrinth.formatting.ToolTip;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Procedure;
import com.github.sanctum.labyrinth.task.Schedule;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerEventListener implements Listener {

	private final static Map<Location, ArmorStand> STAND_MAP = new HashMap<>();
	public static final Procedure<Object> STAND_REMOVAL = Procedure.request(() -> Object.class).next(o -> STAND_MAP.values().forEach(ArmorStand::remove));
	protected static final AsynchronousLoanableTask LOANABLE_TASK = new AsynchronousLoanableTask((p, task) -> {
		if (ClansAPI.getInstance() == null) {
			task.stop();
			return;
		}
		ClanVentBus.call(new TimerEvent(p.getUniqueId(), true){});
		task.synchronize(() -> ClanVentBus.call(new TimerEvent(p.getUniqueId(), false){}));
	});

	@Subscribe(priority = Vent.Priority.LOW)
	public void onInitial(TimerEvent e) {
		if (!e.isAsynchronous()) return;
		Player p = e.getPlayer();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) return;

		Clan c = associate.getClan();

		for (ClanCooldown clanCooldown : c.getCooldowns()) {
			if (clanCooldown.isComplete()) {
				ClanVentBus.call(new ClanCooldownCompleteEvent(c, clanCooldown));
				ClanCooldown.remove(clanCooldown);
				Schedule.sync(() -> c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")))).run();
			}
		}

		War war = ClansAPI.getInstance().getArenaManager().get("PRO");

		if (war != null) {

			if (war.isRunning()) {
				if (war.getTimer().isComplete()) {
					if (war.stop()) {
						War.Team winner = war.getMostPoints().getKey();
						int points = war.getMostPoints().getValue();
						Clan w = war.getClan(winner);
						Map<Clan, Integer> map = new HashMap<>();
						for (Clan clan : war.getQueue().getTeams()) {
							if (!clan.getName().equals(w.getName())) {
								War.Team t = war.getTeam(clan);
								map.put(clan, war.getPoints(t));
							}
						}
						Schedule.sync(() -> {
							WarWonEvent event = ClanVentBus.call(new WarWonEvent(war, new SimpleEntry<>(w, points), map));
							if (!event.isCancelled()) {
								Message msg = LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined());
								Bukkit.broadcastMessage(" ");
								msg.broadcast("&3A war between clans &b[" + Arrays.stream(war.getQueue().getTeams()).map(Clan::getName).collect(Collectors.joining(",")) + "]&3 in arena &7#&e" + war.getId() + " &3concluded with winner &6&l" + w.getName() + " &f(&a" + points + "&f)");
								Bukkit.broadcastMessage(" ");
							}
							war.reset();
						}).run();
					}
				} else {
					Schedule.sync(() -> ClanVentBus.call(new WarActiveEvent(war))).run();
				}
			}

		}

	}

	@Subscribe(priority = Vent.Priority.MEDIUM)
	public void onTimer(TimerEvent e) {
		if (!e.isAsynchronous()) {
			Player p = e.getPlayer();
			ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
				Teleport teleport = Teleport.get(a);
				if (teleport != null) {
					if (p.getLocation().distance(teleport.getLocationBeforeTeleport()) > 0) {
						teleport.setState(Teleport.State.EXPIRED);
						Clan.ACTION.sendMessage(p, "&cYou moved! Teleportation cancelled.");
						teleport.cancel();
					}
				}
			});
			for (ClanCooldown clanCooldown : ClansAPI.getDataInstance().getCooldowns()) {
				if (clanCooldown.getId().equals(p.getUniqueId().toString())) {
					if (clanCooldown.isComplete()) {
						ClanVentBus.call(new PlayerCooldownCompleteEvent(p, clanCooldown));
						Schedule.sync(() -> ClanCooldown.remove(clanCooldown)).run();
						Clan.ACTION.sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
					}
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY, processCancelled = true)
	public void onViewLogo(TimerEvent e) {
		Player p = e.getPlayer();
		if (!e.isAsynchronous()) {
			ArmorStand test = Clan.ACTION.getArmorStandInSight(p, 5);
			if (test != null) {
				LogoHolder.Carrier t = LogoHolder.getCarrier(test.getLocation());
				if (t != null) {
					Location location = new Location(t.getTop().getWorld(), t.getTop().getX(), t.getTop().getY(), t.getTop().getZ(), t.getTop().getYaw(), t.getTop().getPitch()).add(0, 0.5, 0);
					PlayerLookAtCarrierEvent event = ClanVentBus.call(new PlayerLookAtCarrierEvent(p, t, "(" + t.getId() + ") " + OrdinalProcedure.select(t, 2).cast(() -> Clan.class).getName(), 2));
					if (!event.isCancelled()) {
						ArmorStand stand = Entities.ARMOR_STAND.spawn(location, armorStand -> {
							armorStand.setVisible(false);
							armorStand.setMarker(true);
							armorStand.setSmall(true);
							armorStand.setCustomName(event.getTitle());
							armorStand.setCustomNameVisible(true);
						});
						STAND_MAP.put(location, stand);
						Schedule.sync(() -> {
							if (stand.isValid()) {
								stand.remove();
							}
						}).wait(event.getDespawn());
					}
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onClaim(AssociateClaimEvent e) {
		EconomyProvision eco = EconomyProvision.getInstance();
		if (eco.isValid()) {
			if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.charge")) {
				double cost = ClansAPI.getDataInstance().getConfig().read(f -> f.getDouble("Clans.land-claiming.amount"));
				boolean test = eco.withdraw(BigDecimal.valueOf(cost), e.getClaimer(), e.getClaimer().getWorld().getName()).orElse(false);
				if (test) {
					Clan.ACTION.sendMessage(e.getClaimer(), "&a+1 &f| &6$" + cost);
				} else {
					double amount = eco.balance(e.getClaimer()).orElse(0.0);
					Clan.ACTION.sendMessage(e.getClaimer(), Clan.ACTION.notEnough(cost - amount));
					e.setCancelled(true);
				}
			}
		}
	}

	@Subscribe
	public void onPunch(PlayerPunchPlayerEvent e) {

		Player attacker = e.getPlayer();
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
		ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
			War w = ClansAPI.getInstance().getArenaManager().get(a);
			if (w != null && !w.isRunning()) {
				ClansAPI.getInstance().getAssociate(e.getVictim()).ifPresent(as -> {
					if (w.getTeam(as.getClan()) != null) {
						e.getUtil().sendMessage(e.getPlayer(), "&cYou are in a waiting period. War not started yet.");
						e.setCanHurt(false);
					}
				});
			}
		});
	}

	@Subscribe(priority = Vent.Priority.HIGH)
	public void onDamage(DefaultEvent.PlayerDamagePlayer event) {
		if (event.isPhysical()) {
			if (!Bukkit.getOnlinePlayers().contains(event.getVictim())) {
				return;
			}
			Player p = event.getPlayer();
			if (!ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.world-whitelist").contains(p.getWorld().getName()))
				return;
			PlayerPunchPlayerEvent e = ClanVentBus.call(new PlayerPunchPlayerEvent(p, event.getVictim()));

			Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
			Clan.Associate associate2 = ClansAPI.getInstance().getAssociate(e.getVictim()).orElse(null);
			if (associate != null) {
				Clan at = associate.getClan();
				if (associate2 != null) {
					if (at.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().peacefulDeny());
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					Clan v = associate2.getClan();
					if (v.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().peacefulDenyOther(v.getName()));
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (at.getId().equals(v.getId())) {

						e.setCanHurt(at.isFriendlyFire());
						if (!at.isFriendlyFire()) {
							e.getUtil().sendMessage(e.getPlayer(), e.getUtil().friendlyFire());
						}
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (at.getRelation().getAlliance().has(v)) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().friendlyFire());
					}
				} else {
					if (at.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().peacefulDeny());
					}
				}
			} else {
				if (associate2 != null) {
					Clan v = associate2.getClan();
					if (v.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().peacefulDenyOther(v.getName()));
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
			if (!ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.world-whitelist").contains(event.getPlayer().getWorld().getName())) {
				return;
			}
			PlayerShootPlayerEvent e = ClanVentBus.call(new PlayerShootPlayerEvent(event.getPlayer(), event.getVictim()));

			Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
			Clan.Associate associate2 = ClansAPI.getInstance().getAssociate(e.getShot()).orElse(null);
			if (associate != null) {
				if (associate2 != null) {
					Clan a = associate.getClan();
					Clan b = associate2.getClan();
					if (a.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().peacefulDeny());
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (b.isPeaceful()) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().peacefulDenyOther(b.getName()));
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (a.getId().equals(b.getId())) {
						Clan at = ClansAPI.getInstance().getClanManager().getClan(e.getPlayer().getUniqueId());
						e.setCanHurt(at.isFriendlyFire());
						if (!at.isFriendlyFire()) {
							e.getUtil().sendMessage(e.getPlayer(), e.getUtil().friendlyFire());
						}
						if (e.canHurt()) {
							event.setCancelled(e.canHurt());
						}
						return;
					}
					if (a.getRelation().getAlliance().has(b)) {
						e.setCanHurt(false);
						e.getUtil().sendMessage(e.getPlayer(), e.getUtil().friendlyFire());
					}
				}
			}
			if (e.canHurt()) {
				event.setCancelled(e.canHurt());
			}

		}
	}

	@EventHandler
	public void onAnimate(PlayerAnimationEvent e) {
		Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(e.getPlayer().getLocation());
		if (claim != null) {
			if (claim.getClan().getMember(m -> m.getName().equals(e.getPlayer().getName())) == null) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		Entity entity = e.getRightClicked();
		Player p = e.getPlayer();
		ClansAPI.getInstance().getAssociate(p).ifPresent(associate -> {
			Clan c = associate.getClan();
			Clan.Associate test = ClansAPI.getInstance().getAssociate(entity.getUniqueId()).orElse(null);
			if (test != null) {
				if (test.getClan().equals(associate.getClan())) {
					// TODO: check for item on removal.
					ItemStack item = p.getInventory().getItemInMainHand();
					if (item.getType() == Material.STICK) {
						if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
							if (StringUtils.use(StringUtils.use("&r[&bRemover stick&r]").translate()).containsIgnoreCase(item.getItemMeta().getDisplayName())) {
								test.remove();
								item.setAmount(Math.max(0, item.getAmount() - 1));
								if (item.getAmount() == 0) {
									p.getInventory().remove(item);
								}
								c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-leave"), test.getName()));
							}
						}
					}
				}
			} else {
				if (!(entity instanceof Tameable)) return;
				if (!((Tameable) entity).isTamed()) return;
				if (((Tameable) entity).getOwner() == null) return;
				if (((Tameable) entity).getOwner().getName() == null) return;
				if (!((Tameable) entity).getOwner().getName().equals(associate.getName())) return;
				ItemStack item = p.getInventory().getItemInMainHand();
				if (item.getType() == Material.BLAZE_ROD) {
					if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
						if (StringUtils.use(StringUtils.use("&r[&6Tamer stick&r]").translate()).containsIgnoreCase(item.getItemMeta().getDisplayName())) {
							item.setAmount(Math.max(0, item.getAmount() - 1));
							if (item.getAmount() == 0) {
								p.getInventory().remove(item);
							}
							// do entity stuff
							int count = 0;
							for (Clan.Associate a : c.getMembers()) {
								if (StringUtils.use(a.getName()).containsIgnoreCase(((Tameable) entity).getOwner().getName() + "'s " + entity.getName())) {
									count++;
								}
							}
							InvasiveEntity conversion = InvasiveEntity.wrapNonAssociated(entity, count == 0 ? ((Tameable) entity).getOwner().getName() + "'s " + entity.getName() : ((Tameable) entity).getOwner().getName() + "'s " + entity.getName() + " x" + count);

							Clan.Associate newAssociate = c.newAssociate(conversion);

							if (newAssociate != null) {
								c.add(newAssociate);
								// TODO: make ClanBroadcastMessageEvent
								c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-join"), newAssociate.getName()));
							}
						}
					}
				}
			}
		});
	}

	@Subscribe
	public void onPlayerKillPlayer(PlayerKillPlayerEvent e) {
		Player p = e.getVictim();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (!Bukkit.getOnlinePlayers().contains(p)) {
			return;
		}
		Player killer = e.getPlayer();
		if (killer != null) {

			ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
				a.getClan().givePower(0.11);
				OrdinalProcedure.process(a, 50);
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
			e.getCommand().ifPresent(cmd -> ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {

				War w = ClansAPI.getInstance().getArenaManager().get(a);
				if (w != null) {
					if (w.isRunning()) {
						for (String c : ClansAPI.getDataInstance().getWarBlockedCommands()) {
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
			}));
		}
	}

	@Subscribe(priority = Vent.Priority.HIGH, processCancelled = true)
	public void onPlayerJoin(DefaultEvent.Join e) {

		Player p = e.getPlayer();

		LOANABLE_TASK.join(p);

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(LabyrinthUser.get(p.getName()).getId()).orElse(null);

		if (ClanAddonQuery.getAddon("Kingdoms") != null) {
			Schedule.sync(() -> {
				Mailer mail = new Mailer(p);
				List<String> logo = ReservedLogoCarrier.SUMMER.get();
				int size = ChatColor.stripColor(logo.get(0)).length();
				mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();
				FancyLogoAppendage appendage = ClansAPI.getDataInstance().appendStringsToLogo(logo, message -> message.hover("&eThe cheese is good."));
				for (BaseComponent[] b : appendage.append(new TextChunk("Kingdoms is here,").bind(new ToolTip.Text("&eThe time for progression is here!")).bind(new ToolTip.Text("&fUse this addon to unlock")).bind(new ToolTip.Text("&fspecial rewards!")),
						                                  new TextChunk("Try it using &6/clan kingdom.").bind(new ToolTip.Command("/c kingdom")).bind(new ToolTip.Text("&6Click me &fto execute.")),
						                                  new TextChunk("- &6Sanctum Team")).get()) {
					mail.chat(b).deploy();
				}
				mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();
			}).waitReal(10);
		}

		boolean scoreboard = Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17");
		if (scoreboard) {
			if (associate != null) {
				if (associate.isValid()) {
					if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
						if (associate.getClan().getPalette().isGradient()) {
							Clan c = associate.getClan();
							ClanDisplayName.set(associate, ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
						} else {
							ClanDisplayName.set(associate, ClansAPI.getDataInstance().formatDisplayTag(associate.getClan().getPalette().toString(), associate.getClan().getName()));

						}
					} else {
						ClanDisplayName.remove(associate);
					}
				}
			} else {
				ClanDisplayName.remove(p);
			}
		}
		if (p.isOp()) {
			if (ClansAPI.getDataInstance().isUpdate()) {
				Clan.ACTION.sendMessage(p, "&b&oUpdated configuration to the latest plugin version.");
			}
			ClansUpdate check = new ClansUpdate(ClansAPI.getInstance().getPlugin());
			Schedule.async(() -> {
				try {
					if (check.hasUpdate()) {
						Clan.ACTION.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬oO[&fUpdate&b&l&m]Oo▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						Clan.ACTION.sendMessage(p, "&eNew version: &3Clans [Pro] &f" + check.getLatest());
						Clan.ACTION.sendMessage(p, "&e&oDownload: &f&n" + check.getResource());
						Clan.ACTION.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					}
				} catch (Exception ignored) {
				}
			}).run();
		}
	}

	@Subscribe
	public void onPlayerLeave(DefaultEvent.Leave e) {
		final Player p = e.getPlayer();
		LOANABLE_TASK.leave(p);
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null) {
			if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
				ClanDisplayName.remove(p);
			}
			War current = ClansAPI.getInstance().getArenaManager().get(associate);
			if (current != null) {
				Mailer m = Mailer.empty(ClansAPI.getInstance().getPlugin()).prefix().start(ClansAPI.getInstance().getPrefix().joined()).finish();
				if (current.isRunning()) {
					if (current.getQueue().unque(associate)) {
						m.announce(player -> true, associate.getNickname() + "&c has left the battlefield.").deploy();
						Schedule.sync(() -> {
							if (current.getQueue().getAssociates().length == 0) {
								m.announce(player -> true, "&cThere is no one left in the arena. War in &7#&6" + current.getId() + " &chas reset.").deploy();
								current.stop();
								current.reset();
								return;
							}
							if (current.getQueue().count(associate.getClan()) == 0) {
								int alive = 0;
								for (Clan c : current.getQueue().getTeams()) {
									int count = current.getQueue().count(c);
									if (count != 0) {
										alive++;
									}
								}
								if (alive <= 1) {
									m.announce(player -> true, "&cEveryone has left the battlefield. War in &7#&6" + current.getId() + " &cconcluded with winning team &b" + current.getMostPoints().getKey().name()).deploy();
									current.stop();
									current.reset();
								} else {
									current.getQueue().unque(associate);
								}
							}
						}).run();
					}
				} else {
					if (current.getQueue().getAssociates().length <= ClansAPI.getDataInstance().getConfigInt("Clans.war.que-needed") + 1) {
						if (current.avoid()) {
							current.reset();
							m.announce(player -> true, "&cEvery queued member has left the game. War in &7#&6" + current.getId() + " &cfailed to start.").deploy();
						}
					} else {
						Schedule.sync(() -> current.getQueue().unque(associate)).applyAfter(() -> {
							if (current.getQueue().count(associate.getClan()) == 0) {
								int alive = 0;
								for (Clan c : current.getQueue().getTeams()) {
									int count = current.getQueue().count(c);
									if (count != 0) {
										alive++;
									}
								}
								if (alive <= 1) {
									m.announce(player -> true, "&cEveryone has left the battlefield. War in &7#&6" + current.getId() + " &cconcluded with winning team &b" + current.getMostPoints().getKey().name()).deploy();
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
		Optional.ofNullable(ClansAPI.getDataInstance().getResident(p)).ifPresent(r -> ClansAPI.getDataInstance().removeClaimResident(r));
		ClansAPI.getDataInstance().removeWildernessInhabitant(p);
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent e) {
		if (ClansAPI.getInstance().getClaimManager().isInClaim(e.getTo())) {
			e.setCanCreatePortal(ClansAPI.getDataInstance().isTrue("Clans.land-claiming.portals-in-claims"));
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
					Mailer m = Mailer.empty(ClansAPI.getInstance().getPlugin()).prefix().start(ClansAPI.getInstance().getPrefix().joined()).finish();
					m.announce(player -> player.hasPermission("clanspro.admin"), "The spawn location for team " + t.name() + " is missing!").deploy();
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
		if (ClansAPI.getInstance().getClaimManager().getClaim(event.getBlock().getLocation()) != null) {
			ClaimInteractEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new ClaimInteractEvent(event.getPlayer(), event.getBlockClicked().getLocation(), ClaimInteractEvent.Type.USE)).run();
			if (e.isCancelled()) {
				//e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
				final Material bucketType = event.getBucket();
				if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.debug")) {
					Schedule.sync(() -> {
						event.getBlockClicked().setType(Material.AIR);
						event.getPlayer().getInventory().getItemInMainHand().setType(bucketType);
						event.getPlayer().updateInventory();
					}).run();
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		if (ClansAPI.getInstance().getClaimManager().getClaim(event.getBlock().getLocation()) != null) {
			ClaimInteractEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new ClaimInteractEvent(event.getPlayer(), event.getBlockClicked().getLocation(), ClaimInteractEvent.Type.USE)).run();
			if (e.isCancelled()) {
				//e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
				final Material bucketType = event.getBucket();
				final Material type = event.getBlockClicked().getType();
				if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.debug")) {
					Schedule.sync(() -> {
						event.getBlockClicked().setType(type);
						event.getPlayer().getInventory().getItemInMainHand().setType(bucketType);
						event.getPlayer().updateInventory();
					}).run();
				}
				event.setCancelled(true);
			}
		}
	}

}
