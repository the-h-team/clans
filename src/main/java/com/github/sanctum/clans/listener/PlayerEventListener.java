package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.ClanVentCall;
import com.github.sanctum.clans.model.addon.worldedit.WorldEditAdapter;
import com.github.sanctum.clans.model.addon.worldedit.WorldEditClipboardAdapter;
import com.github.sanctum.clans.model.addon.worldedit.WorldEditSchematicAdapter;
import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.impl.DefaultMapEntry;
import com.github.sanctum.clans.impl.DefaultRespawnCooldown;
import com.github.sanctum.clans.util.AboveHeadDisplayName;
import com.github.sanctum.clans.util.AsynchronousLoanableTask;
import com.github.sanctum.clans.util.ClansUpdate;
import com.github.sanctum.clans.util.Reservoir;
import com.github.sanctum.clans.util.ReservoirMetadata;
import com.github.sanctum.clans.event.TimerEvent;
import com.github.sanctum.clans.event.associate.AssociateClaimEvent;
import com.github.sanctum.clans.event.associate.AssociateHitReservoirEvent;
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
import com.github.sanctum.labyrinth.data.container.CollectionTask;
import com.github.sanctum.labyrinth.event.DefaultEvent;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.ItemCompost;
import com.github.sanctum.labyrinth.library.ItemMatcher;
import com.github.sanctum.labyrinth.library.ItemSync;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Procedure;
import com.github.sanctum.labyrinth.task.TaskMonitor;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.OrdinalProcedure;
import com.github.sanctum.panther.util.ProgressBar;
import com.github.sanctum.panther.util.Task;
import com.github.sanctum.panther.util.TaskChain;
import com.github.sanctum.panther.util.TaskPredicate;
import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class PlayerEventListener implements Listener {

	private final static PantherMap<Location, ArmorStand> STAND_MAP = new PantherEntryMap<>();
	public static final Procedure<Object> ARMOR_STAND_REMOVAL = Procedure.request(() -> Object.class).next(o -> STAND_MAP.values().forEach(ArmorStand::remove));
	final int time_span = ClansAPI.getDataInstance().getConfigInt("Clans.timer.time-span");
	public static final AsynchronousLoanableTask LOANABLE_TASK = new AsynchronousLoanableTask((p, task) -> {
		ClanVentBus.call(new TimerEvent(p, true) {
		});
		task.synchronize(() -> ClanVentBus.call(new TimerEvent(p, false) {
		}));
	});

	public PlayerEventListener() {
		final ItemCompost compost = LabyrinthProvider.getInstance().getItemComposter();
		compost.registerMatcher(new TokenSync());
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onInitial(TimerEvent e) {

		if (!e.isAsynchronous()) {
			Player p = e.getPlayer();
			Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

			if (associate == null) return;

			Clan c = associate.getClan();
			for (ClanCooldown clanCooldown : c.getCooldowns()) {
				if (clanCooldown.isComplete() && !clanCooldown.isMarkedForRemoval()) {
					clanCooldown.setMarkedForRemoval(true);
					ClanVentBus.call(new ClanCooldownCompleteEvent(c, clanCooldown));
					if (clanCooldown.getDescriptor() != null && !clanCooldown.getDescriptor().isEmpty()) {
						FancyMessage m = new FancyMessage();
						m.then(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
						m.hover("&3&o" + clanCooldown.getDescriptor());
						c.broadcast(m.build());
					} else {
						c.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
					}
					ClanCooldown.remove(clanCooldown);
				}
			}
			return;
		}

		Arena arena = ClansAPI.getInstance().getArenaManager().get("PRO");

		if (arena != null) {

			if (arena.isRunning()) {
				if (arena.getTimer().isComplete()) {
					if (arena.stop()) {
						Arena.Team winner = arena.getMostPoints().getKey();
						int points = arena.getMostPoints().getValue();
						Clan w = arena.getClan(winner);
						Map<Clan, Integer> map = new HashMap<>();
						for (Clan clan : arena.getQueue().getTeams()) {
							if (!clan.getName().equals(w.getName())) {
								Arena.Team t = arena.getTeam(clan);
								map.put(clan, arena.getPoints(t));
							}
						}
						TaskScheduler.of(() -> {
							WarWonEvent event = ClanVentBus.call(new WarWonEvent(arena, new DefaultMapEntry<>(w, points), map));
							if (!event.isCancelled()) {
								Mailer msg = LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(ClansAPI.getInstance().getPrefix().toString()).finish();
								Bukkit.broadcastMessage(" ");
								msg.announce(pl -> true, "&3A war between clans &b[" + Arrays.stream(arena.getQueue().getTeams()).map(Clan::getName).collect(Collectors.joining(",")) + "]&3 in arena &7#&e" + arena.getId() + " &3concluded with winner &6&l" + w.getName() + " &f(&a" + points + "&f)");
								Bukkit.broadcastMessage(" ");
							}
							arena.reset();
						}).schedule();
					}
				} else {
					TaskScheduler.of(() -> ClanVentBus.call(new WarActiveEvent(arena))).schedule();
				}
			}

		}

	}

	@Subscribe(priority = Vent.Priority.MEDIUM)
	public void onTimer(TimerEvent e) {
		if (!e.isAsynchronous()) {
			Player p = e.getPlayer();
			ClansAPI.getInstance().getAssociate(p.getName()).ifPresent(a -> {
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
						TaskScheduler.of(() -> ClanCooldown.remove(clanCooldown)).schedule();
						if (clanCooldown.getDescriptor() != null && !clanCooldown.getDescriptor().isEmpty()) {
							FancyMessage m = new FancyMessage();
							m.then(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
							m.hover("&3&o" + clanCooldown.getDescriptor());
							m.send(p).queue();
						} else {
							Clan.ACTION.sendMessage(p, MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-expired"), clanCooldown.getAction().replace("Clans:", "")));
						}
					}
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY, processCancelled = true)
	public void onViewLogo(TimerEvent e) {
		Player p = e.getPlayer();
		if (!e.isAsynchronous()) {
			if (e.getApi().isTrial()) return; // no pro, no provided holograms
			ArmorStand test = Clan.ACTION.getArmorStandInSight(p, 5);
			if (test != null) {
				LogoHolder.Carrier t = LogoHolder.getCarrier(test.getLocation());
				if (t != null) {
					Location location = new Location(t.getTop().getWorld(), t.getTop().getX(), t.getTop().getY(), t.getTop().getZ(), t.getTop().getYaw(), t.getTop().getPitch()).add(0, 0.5, 0);
					PlayerLookAtCarrierEvent event = ClanVentBus.call(new PlayerLookAtCarrierEvent(p, t, "(" + t.getId() + ") " + OrdinalProcedure.select(t, 2).cast(() -> Clan.class).getName(), time_span));
					if (!event.isCancelled()) {
						ArmorStand stand = Entities.ARMOR_STAND.spawn(location, armorStand -> {
							armorStand.setVisible(false);
							armorStand.setMarker(true);
							armorStand.setSmall(true);
							armorStand.setCustomName(event.getTitle());
							armorStand.setCustomNameVisible(true);
						});
						TaskScheduler.of(() -> {
							if (stand.isValid()) {
								stand.remove();
							}
						}).scheduleLater(event.getDespawn());
					}
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY, processCancelled = true)
	public void onViewReservoir(TimerEvent e) {
		Player p = e.getPlayer();
		if (!e.isAsynchronous()) {
			if (!LabyrinthProvider.getInstance().isNew()) return;
			if (e.getApi().isTrial()) return; // no pro no reservoir
			EnderCrystal test = Clan.ACTION.getEnderCrystalInSight(p, 5);
			if (test != null) {
				PersistentDataContainer container = test.getPersistentDataContainer();
				NamespacedKey key = new NamespacedKey(e.getApi().getPlugin(), "clans_reservoir");
				if (container.has(key, PersistentDataType.STRING)) {
					String owner = container.get(key, PersistentDataType.STRING);
					Clan c = e.getApi().getClanManager().getClan(HUID.parseID(owner).toID());
					if (c != null) {
						Reservoir r = Reservoir.of(test);
						if (r.getOwner() == null) r.adapt(c);
						Location location = new Location(test.getLocation().getWorld(), test.getLocation().getX(), test.getLocation().getY(), test.getLocation().getZ(), test.getLocation().getYaw(), test.getLocation().getPitch()).add(0, 1.5, 0);
						Location location2 = new Location(test.getLocation().getWorld(), test.getLocation().getX(), test.getLocation().getY(), test.getLocation().getZ(), test.getLocation().getYaw(), test.getLocation().getPitch()).add(0, 1.2, 0);
						Location location3 = new Location(test.getLocation().getWorld(), test.getLocation().getX(), test.getLocation().getY(), test.getLocation().getZ(), test.getLocation().getYaw(), test.getLocation().getPitch()).add(0, 1.8, 0);
						ArmorStand stand = Entities.ARMOR_STAND.spawn(location, armorStand -> {
							armorStand.setVisible(false);
							armorStand.setMarker(true);
							armorStand.setSmall(true);
							armorStand.setCustomName(new FormattedString(new ProgressBar().setProgress((int) r.getPower()).setGoal((int) r.getMaxPower()).setFullColor("&5&l").setPrefix(null).setSuffix(null).toString() + " &f(&6" + NumberFormat.getNumberInstance().format(r.getPower()) + "&f)").color().get());
							armorStand.setCustomNameVisible(true);
						});
						ArmorStand stand2 = Entities.ARMOR_STAND.spawn(location3, armorStand -> {
							armorStand.setVisible(false);
							armorStand.setMarker(true);
							armorStand.setSmall(true);
							armorStand.setCustomName(new FormattedString(c.getPalette().toString(c.getName())).color().get());
							armorStand.setCustomNameVisible(true);
						});
						ArmorStand stand3 = Entities.ARMOR_STAND.spawn(location2, armorStand -> {
							armorStand.setVisible(false);
							armorStand.setMarker(true);
							armorStand.setSmall(true);
							armorStand.setCustomName(new FormattedString("&5Reservoir").color().get());
							armorStand.setCustomNameVisible(true);
						});
						TaskScheduler.of(() -> {
							if (stand.isValid()) {
								stand.remove();
							}
							if (stand2.isValid()) {
								stand2.remove();
							}
							if (stand3.isValid()) {
								stand3.remove();
							}
						}).scheduleLater(time_span);
					}
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.PHYSICAL) {
			if (e.getClickedBlock().getState().getBlockData() instanceof Farmland) {
				Claim c = ClansAPI.getInstance().getClaimManager().getClaim(e.getClickedBlock().getLocation());
				if (c != null) {
					Clan holder = (Clan) c.getHolder();
					if (holder.stream().noneMatch(en -> en.getName().equals(e.getPlayer().getName()))) {
						e.setCancelled(true);
					}
				}
			}
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.END_CRYSTAL) {
				Clan.Associate t = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
				if (t != null) {
					Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(e.getClickedBlock().getLocation());
					if (claim != null && claim.getHolder().equals(t.getClan())) {
						if (Reservoir.get(t.getClan()) == null) {
							if (e.getClickedBlock().getType() != Material.BEDROCK) {
								ReservoirMetadata data = new ReservoirMetadata();
								data.setAssociate(t);
								final Location location = e.getClickedBlock().getLocation().add(0.5, 0, 0.5); // center the location for the ender crystal (otherwise itll spawn on the corner of the block)
								EntityEventListener.map.put(location, data);
								e.getClickedBlock().setType(Material.AIR);
								// place schematic
								WorldEditAdapter adapter = WorldEditAdapter.getInstance();
								if (adapter.isValid()) {
									// FIXME make relative to plugin dir
									final WorldEditSchematicAdapter schematic = adapter.loadSchematic(new File("plugins/Clans/Configuration/Data/reservoir.schem"));
									if (schematic != null) {
										final WorldEditClipboardAdapter clipboard = schematic.toClipboard();
										TaskChain.getSynchronous().wait(() -> clipboard.paste().toLocation(location).applyAfter(() -> Entities.ENDER_CRYSTAL.spawn(location)), t.getClan().getId() + ";reservoir-build", 300L * 20L);
									}
								} else {
									Entities.ENDER_CRYSTAL.spawn(e.getClickedBlock().getLocation());
								}
							}
						} else {
							Clan.ACTION.sendMessage(e.getPlayer(), "&cYour clan already has a reservoir powered up!");
						}
					} else {
						Clan.ACTION.sendMessage(e.getPlayer(), "&cYou can only build a clan reservoir in owned land!");
					}
				}
			}
		}
	}

	@EventHandler
	public void onHealReservoir(PlayerInteractAtEntityEvent e) {
		if (!LabyrinthProvider.getInstance().isNew()) return;
		if (ClansAPI.getInstance().isTrial()) return;
		Clan.Associate a = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
		if (a != null) {
			if (e.getRightClicked() instanceof EnderCrystal) {
				if (e.getHand().equals(EquipmentSlot.HAND)) {
					Reservoir r = Reservoir.get(e.getRightClicked());
					if (r != null) {
						Clan owner = r.getOwner();
						if (owner != null) {
							if (owner.equals(a.getClan())) {
								final ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
								final ItemCompost compost = LabyrinthProvider.getInstance().getItemComposter();
								if (itemStack.getType() == Material.IRON_NUGGET) {
									TokenSync sync = new TokenSync(itemStack.getAmount(), e.getPlayer().getInventory());
									if (compost.has(sync)) {
										double modifier = ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Clans.reservoir.power-multiplier").toPrimitive().getDouble());
										final double worth = modifier * itemStack.getAmount();
										compost.remove(sync);
										owner.givePower(worth);
										owner.broadcast("&5" + a.getNickname() + " &6obtained power for us.");
									}
									return;
								}
								if (r.isDamaged()) {
									long nearBy = r.getEntity().getNearbyEntities(8, 8, 8).stream().filter(en -> en instanceof Player && owner.getMember(m -> m.getName().equals(en.getName())) == null).count();
									if (nearBy == 0) {
										if (itemStack.getAmount() >= 1) {
											final int am = itemStack.getAmount();
											double modifier = ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Clans.reservoir.repair-multiplier." + itemStack.getType().name()).toPrimitive().getDouble()); // retrieve modifier based on item from config or pull default.
											if (modifier > 0) {
												//TODO: make it detect a crouch so you can choose to deposit individual items or all at once
												itemStack.setAmount(0);
												r.set(Math.min(owner.getPower(), r.getPower() + (am * modifier)));
												e.getPlayer().getWorld().spawnParticle(Particle.HEART, new Location(r.getEntity().getWorld(), r.getEntity().getLocation().getX(), r.getEntity().getLocation().getY(), r.getEntity().getLocation().getZ(), r.getEntity().getLocation().getYaw(), r.getEntity().getLocation().getPitch()).add(0, 0.5, 0), 1);
												owner.broadcast("&aOur reservoir was repaired by &5" + a.getNickname());
											} else {
												a.getMailer().chat("&cThis item has no value to the reservoir.").queue();
											}
										} else {
											Clan.ACTION.sendMessage(e.getPlayer(), "&cYou cannot feed the reservoir air!");
										}
									} else {
										a.getMailer().chat("&cThere are enemies too close by to perform this action.").queue();
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Subscribe
	public void onReservoirDeath(AssociateHitReservoirEvent e) {
		Reservoir r = e.getReservoir();
		Clan attacking = e.getAssociate().getClan();
		Clan victim = r.getOwner();
		if (victim != null) {
			if (attacking.equals(victim)) {
				e.getAssociate().getMailer().chat("&cYou cannot damage your own reservoir only repair it!").deploy();
				new FancyMessage("Are you attempting to destroy it?").then(" ").then("&7[").then("&6Yes").hover("&eClick to confirm.").action(() -> {
					e.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, r.getEntity().getLocation(), 1);
					e.getPlayer().getWorld().playSound(r.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 10, 1);
					r.remove();
					attacking.broadcast("&5" + e.getAssociate().getNickname() + " &ehas removed our clan reservoir.");
				}).then("&7]").send(e.getPlayer()).queue();
				e.setCancelled(true);
				return;
			}
			if (r.getPower() == 0) {
				if (victim.getPower() < 1) {
					e.getAssociate().getMailer().chat("&cAll resources here have been tapped! No power to siphon.").queue();
					e.setCancelled(true);
					return;
				}
				Task test = TaskMonitor.getLocalInstance().get("Clans;reservoir_power_loss:" + victim.getId());
				if (test == null) {
					attacking.broadcast("&aAssociate &5" + e.getAssociate().getNickname() + " &apowered down " + victim.getPalette().toString(victim.getName()) + "'s &areservoir! Hurry and collect the power its leaking!");
					PantherCollection<Integer> drain = new PantherList<>();
					for (int i = 1; i < victim.getPower() + 1; i++) {
						drain.add(1);
					}
					Integer[] ar = drain.stream().toArray(Integer[]::new);
					Task t = CollectionTask.processSilent(ar, "Clans;reservoir_power_loss:" + victim.getId(), 1, integer -> {
						victim.takePower(integer);
						attacking.givePower(integer);
						e.getPlayer().getWorld().playSound(r.getEntity().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
						e.getPlayer().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, r.getEntity().getLocation(), 1);
					});
					TaskPredicate<Task> predicate = TaskPredicate.cancelAfter(task -> {
						if (r.getEntity().getNearbyEntities(4, 4, 4).stream().noneMatch(en -> en instanceof Player && attacking.getMember(a -> a.getName().equals(en.getName())) != null)) {
							task.cancel();
							attacking.broadcast("&cPower siphoning stopped. Not close enough to reservoir.");
							victim.broadcast("&cOur power loss has stopped but our reservoir is badly damaged. Repair it with items to prevent further loss.");
							return false;
						}
						return true;
					});
					t.listen(predicate);
					TaskScheduler.of(t).scheduleTimer(t.getKey(), 0, 20);
					victim.broadcast("&4&lBreach &7» &cOur reservoir was powered down! Hurry and repair it before we lose too much power!");
					Location location = r.getEntity().getLocation();
					Item.CustomFirework firework = Item.CustomFirework.from(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()).add(0, 2.5, 0));
					firework.addEffects(b -> b.trail(true).with(FireworkEffect.Type.CREEPER).withColor(Color.ORANGE, Color.FUCHSIA, Color.MAROON, Color.OLIVE).withFade(Color.PURPLE).flicker(true)).build().detonate();
				} else {
					e.getAssociate().getMailer().chat("&cThis reservoir is already being drained!").queue();
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onClaim(AssociateClaimEvent e) {
		EconomyProvision eco = EconomyProvision.getInstance();
		if (eco.isValid()) {
			if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.charge")) {
				if (e.getClan().getClaims().length + 1 > e.getClan().getClaimLimit()) return;
				String MODE = Optional.ofNullable(ClansAPI.getDataInstance().getConfigString("Clans.land-claiming.mode")).orElse("STATIC");
				double cost = ClansAPI.getDataInstance().getConfig().read(f -> f.getDouble("Clans.land-claiming.amount"));
				String percent = ClansAPI.getDataInstance().getConfig().read(co -> co.getNode("Clans.land-claiming.percent").toPrimitive().getString());
				String s1 = "0." + percent;
				double per = Double.parseDouble(s1);
				switch (MODE.toLowerCase(Locale.ROOT)) {
					case "percentage":
						BigDecimal pc = BigDecimal.valueOf(e.getClan().getClaims().length).multiply(BigDecimal.valueOf(cost)).multiply(BigDecimal.valueOf(per));
						if (eco.has(pc, e.getClaimer()).orElse(false)) {
							boolean test = eco.withdraw(pc, e.getClaimer(), e.getClaimer().getWorld().getName()).orElse(false);
							if (test) {
								Clan.ACTION.sendMessage(e.getClaimer(), "&a+1 &f| &6$" + pc.doubleValue());
							}
						} else {
							double amount = eco.balance(e.getClaimer()).orElse(0.0);
							Clan.ACTION.sendMessage(e.getClaimer(), Clan.ACTION.notEnough(pc.doubleValue() - amount));
							e.setCancelled(true);
						}
						break;
					case "add":
						BigDecimal dc = BigDecimal.valueOf(e.getClan().getClaims().length).multiply(BigDecimal.valueOf(cost));
						if (eco.has(dc, e.getClaimer()).orElse(false)) {
							boolean test = eco.withdraw(dc, e.getClaimer(), e.getClaimer().getWorld().getName()).orElse(false);
							if (test) {
								Clan.ACTION.sendMessage(e.getClaimer(), "&a+1 &f| &6$" + dc.doubleValue());
							}
						} else {
							double amount = eco.balance(e.getClaimer()).orElse(0.0);
							Clan.ACTION.sendMessage(e.getClaimer(), Clan.ACTION.notEnough(dc.doubleValue() - amount));
							e.setCancelled(true);
						}
						break;
					case "static":
						if (eco.has(BigDecimal.valueOf(cost), e.getClaimer()).orElse(false)) {
							boolean test = eco.withdraw(BigDecimal.valueOf(cost), e.getClaimer(), e.getClaimer().getWorld().getName()).orElse(false);
							if (test) {
								Clan.ACTION.sendMessage(e.getClaimer(), "&a+1 &f| &6$" + cost);
							}
						} else {
							double amount = eco.balance(e.getClaimer()).orElse(0.0);
							Clan.ACTION.sendMessage(e.getClaimer(), Clan.ACTION.notEnough(cost - amount));
							e.setCancelled(true);
						}
						break;
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onPunch(PlayerPunchPlayerEvent e) {

		Player attacker = e.getPlayer();
		Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("Clans-war-respawn-" + e.getVictim().getUniqueId());
		if (test != null) {
			if (!test.isComplete()) {
				if (test.getSeconds() == 0) {
					LabyrinthProvider.getInstance().remove(test);
					return;
				}
				e.getUtil().sendMessage(attacker, "&cYou must wait &6&l" + test.getSeconds() + " &cseconds before doing this to me.");
				e.setCanHurt(false);
			} else {
				LabyrinthProvider.getInstance().remove(test);
			}
			return;
		}

		ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
			Arena w = ClansAPI.getInstance().getArenaManager().get(a);
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
						e.setCanHurt(a.isFriendlyFire());
						if (!a.isFriendlyFire()) {
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
			if (((Clan) claim.getHolder()).getMember(m -> m.getName().equals(e.getPlayer().getName())) == null) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		Entity entity = e.getRightClicked();
		Player p = e.getPlayer();
		if (e.getHand().equals(EquipmentSlot.HAND)) {
			ClansAPI.getInstance().getAssociate(p).ifPresent(associate -> {
				Clan c = associate.getClan();
				Clan.Associate test = ClansAPI.getInstance().getAssociate(entity.getUniqueId()).orElse(null);
				if (test != null) {
					if (test.getClan().equals(associate.getClan())) {
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
					Tameable tameable = (Tameable) entity;
					if (!tameable.isTamed()) return;
					if (tameable.getOwner() == null) return;
					if (tameable.getOwner().getName() == null) return;
					if (!tameable.getOwner().getName().equals(associate.getName())) return;
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
			if (ClansAPI.getDataInstance().isTrue("Clans.reservoir.players-drop-tokens")) {
				ItemStack rew = Items.edit().setType(Material.IRON_NUGGET).setTitle("&3[&6Token&3]").setAmount(1).build();
				LabyrinthProvider.getInstance().getItemComposter().add(rew, killer);
			}
			ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
				a.getClan().givePower(0.11);
				OrdinalProcedure.process(a, 50);
			});

			if (associate != null) {
				associate.getClan().takePower(0.11);
				Arena arena = ClansAPI.getInstance().getArenaManager().get(associate);
				if (arena != null) {
					if (arena.isRunning()) {
						ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
							Arena.Team t = arena.getTeam(a.getClan());
							int points = arena.getPoints(t);
							arena.setPoints(t, points + 1);
							a.getClan().broadcast("&aWe just scored 1 point");
						});
						e.setClearDrops(true);
						e.setKeepInventory(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onProcess(PlayerCommandPreprocessEvent e) {
		ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {

			Arena w = ClansAPI.getInstance().getArenaManager().get(a);
			if (w != null) {
				if (w.isRunning()) {
					for (String c : LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(ClansAPI.getInstance().getLocalPrintKey()).getStringList("blocked_commands_war")) {
						if (Arrays.equals(c.split(" "), e.getMessage().split(" "))) {
							Clan.ACTION.sendMessage(e.getPlayer(), "&cYou cannot do this while in a match! Use &6/c surrender &ror &6truce &cto call a vote.");
							e.setCancelled(true);
						}
					}
				} else {
					if (!StringUtils.use(e.getMessage()).containsIgnoreCase("c surrender", "c forfeit", "c war teleport")) {
						Clan.ACTION.sendMessage(e.getPlayer(), "&cYou cannot do this while queued for a match! Use &6/c surrender &cto safely leave queue.");
						e.setCancelled(true);
					}
				}
			}
		});
	}

	@Subscribe(priority = Vent.Priority.HIGH, processCancelled = true)
	public void onPlayerJoin(DefaultEvent.Join e) {

		Player p = e.getPlayer();

		LOANABLE_TASK.join(p);

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(Bukkit.getOfflinePlayer(p.getUniqueId())).orElse(null);

		boolean canDisplay = !LabyrinthProvider.getInstance().isLegacy();
		if (canDisplay) {
			if (associate != null) {
				if (associate.isValid()) {
					if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
						if (associate.getClan().getPalette().isGradient()) {
							Clan c = associate.getClan();
							AboveHeadDisplayName.set(associate, ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
						} else {
							AboveHeadDisplayName.set(associate, ClansAPI.getDataInstance().formatDisplayTag(associate.getClan().getPalette().toString(), associate.getClan().getName()));
						}
					} else {
						AboveHeadDisplayName.remove(associate);
					}
				}
			} else {
				AboveHeadDisplayName.remove(p);
			}
		}
		if (Clan.ACTION.test(p, "clans.admin").deploy()) {
			if (ClansAPI.getDataInstance().isTrue("Clans.check-version")) {
				if (ClansAPI.getDataInstance().updateConfigs()) {
					Clan.ACTION.sendMessage(p, "&b&oUpdated configuration to the latest plugin version.");
				}
				ClansUpdate check = new ClansUpdate(ClansAPI.getInstance().getPlugin());
				TaskScheduler.of(() -> {
					try {
						if (check.hasUpdate()) {
							TaskScheduler.of(() -> {
								Clan.ACTION.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬oO[&fUpdate&b&l&m]Oo▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
								Clan.ACTION.sendMessage(p, "&eNew version: &3Clans [Pro] &f" + check.getLatest());
								Clan.ACTION.sendMessage(p, "&e&oDownload: &f&n" + check.getResource());
								Clan.ACTION.sendMessage(p, "&b&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
							}).schedule();
						}
					} catch (Exception ignored) {
					}
				}).scheduleAsync();
			}
		}
	}

	@Subscribe
	public void onPlayerLeave(DefaultEvent.Leave e) {
		final Player p = e.getPlayer();
		LOANABLE_TASK.leave(p);
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (associate != null) {
			if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
				AboveHeadDisplayName.remove(p);
			}
			Arena current = ClansAPI.getInstance().getArenaManager().get(associate);
			if (current != null) {
				Mailer m = Mailer.empty(ClansAPI.getInstance().getPlugin()).prefix().start(ClansAPI.getInstance().getPrefix().toString()).finish();
				if (current.isRunning()) {
					if (current.getQueue().unque(associate)) {
						m.announce(player -> true, associate.getNickname() + "&c has left the battlefield.").deploy();
						TaskScheduler.of(() -> {
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
						}).schedule();
					}
				} else {
					if (current.getQueue().getAssociates().length <= ClansAPI.getDataInstance().getConfigInt("Clans.arena.que-needed") + 1) {
						if (current.avoid()) {
							current.reset();
							m.announce(player -> true, "&cEvery queued member has left the game. War in &7#&6" + current.getId() + " &cfailed to start.").deploy();
						}
					} else {
						TaskScheduler.of(() -> current.getQueue().unque(associate)).schedule().next(() -> {
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
						}).schedule();
					}
				}
			}
		}
		Optional.ofNullable(ClansAPI.getInstance().getClaimManager().getResidentManager().getResident(p)).ifPresent(r -> ClansAPI.getInstance().getClaimManager().getResidentManager().remove(r));
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
			Arena arena = ClansAPI.getInstance().getArenaManager().get(associate);
			if (arena != null && arena.isRunning()) {
				Arena.Team t = arena.getTeam(associate.getClan());
				if (t.getSpawn() != null) {
					Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("Clans-war-respawn-" + p.getUniqueId());
					if (test != null) {
						LabyrinthProvider.getInstance().remove(test);
					}
					new DefaultRespawnCooldown(p.getUniqueId()).save();
					e.setRespawnLocation(t.getSpawn());
					for (Entity s : p.getNearbyEntities(20, 20, 20)) {
						if (s instanceof Player) {
							Player pl = (Player) s;
							Clan.Associate a = ClansAPI.getInstance().getAssociate(pl).orElse(null);
							if (a != null) {
								if (arena.getTeam(a.getClan()) != null) {
									if (arena.getTeam(a.getClan()) != t) {
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
					Mailer m = Mailer.empty(ClansAPI.getInstance().getPlugin()).prefix().start(ClansAPI.getInstance().getPrefix().toString()).finish();
					m.announce(player -> Clan.ACTION.test(player, "clans.admin.alert").deploy(), "The spawn location for team " + t.name() + " is missing!").deploy();
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
			PlayerKillPlayerEvent e = new ClanVentCall<>(new PlayerKillPlayerEvent(p, target)).run();

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
			ClaimInteractEvent e = new ClanVentCall<>(new ClaimInteractEvent(event.getPlayer(), event.getBlockClicked().getLocation(), ClaimInteractEvent.Type.USE)).run();
			if (e.isCancelled()) {
				//e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
				final Material bucketType = event.getBucket();
				if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.debug")) {
					TaskScheduler.of(() -> {
						event.getBlock().setType(Material.AIR);
						event.getPlayer().getInventory().getItemInMainHand().setType(bucketType);
						event.getPlayer().updateInventory();
					}).schedule();
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		if (ClansAPI.getInstance().getClaimManager().getClaim(event.getBlock().getLocation()) != null) {
			ClaimInteractEvent e = new ClanVentCall<>(new ClaimInteractEvent(event.getPlayer(), event.getBlockClicked().getLocation(), ClaimInteractEvent.Type.USE)).run();
			if (e.isCancelled()) {
				//e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
				final Material bucketType = event.getBucket();
				final Material type = event.getBlockClicked().getType();
				if (ClansAPI.getDataInstance().isTrue("Clans.land-claiming.debug")) {
					TaskScheduler.of(() -> {
						event.getBlock().setType(type);
						event.getPlayer().getInventory().getItemInMainHand().setType(bucketType);
						event.getPlayer().updateInventory();
					}).schedule();
				}
				event.setCancelled(true);
			}
		}
	}

	static final class TokenSync extends ItemSync<TokenSync> implements ItemMatcher {

		final String NAME = "&3[&6Token&3]";

		public TokenSync() {
			this(0, null);
		}

		public TokenSync(int amount, Inventory inv) {
			super(TokenSync.class, inv, amount);
		}

		@Override
		public boolean comparesTo(@NotNull ItemStack item) {
			return item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains(StringUtils.use(NAME).translate()) && item.getType() == Material.IRON_NUGGET;
		}
	}
}
