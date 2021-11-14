package com.github.sanctum.clans.bridge.internal.kingdoms.listener;

import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.bridge.internal.kingdoms.Reward;
import com.github.sanctum.clans.bridge.internal.kingdoms.command.KingdomCommand;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomCreationEvent;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomQuestCompletionEvent;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.RoundTableQuestCompletionEvent;
import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.PrivateContainer;
import com.github.sanctum.clans.event.associate.AssociateQuitEvent;
import com.github.sanctum.clans.event.claim.ClaimInteractEvent;
import com.github.sanctum.clans.event.claim.ClaimResidentEvent;
import com.github.sanctum.clans.event.player.PlayerKillPlayerEvent;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.ComponentChunk;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.StructureType;
import org.bukkit.block.Block;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class KingdomController implements Listener {

	private final KingdomAddon addon;

	public KingdomController(KingdomAddon kingdomAddon) {
		this.addon = kingdomAddon;
	}

	@Subscribe(priority = Vent.Priority.HIGH)
	public void onCreation(KingdomCreationEvent e) {
		if (e.getAssociate().getClan().getPower() < ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Addon").getNode("Kingdoms").getNode("required-creation-power").toPrimitive().getDouble())) {
			double req = ClansAPI.getDataInstance().getConfig().read(c -> c.getNode("Addon").getNode("Kingdoms").getNode("required-creation-power").toPrimitive().getDouble()) - e.getAssociate().getClan().getPower();
			e.getUtil().sendMessage(e.getAssociate().getUser().toBukkit().getPlayer(), "&cWe aren't powerful enough to start a kingdom! We need " + Clan.ACTION.format(req) + " more power.");
			e.setCancelled(true);
		}
	}

	@Subscribe(priority = Vent.Priority.HIGH)
	public void onClaim(ClaimResidentEvent e) {
		Kingdom k = Kingdom.getKingdom(e.getClaim().getClan());
		if (k != null) {
			e.setClaimTitle("&6Kingdom&7: &r" + k.getName(), e.getClaimSubTitle());
		}
	}

	@EventHandler
	public void onEnterCity(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			ClansAPI.getInstance().getAssociate(e.getEntity().getUniqueId()).ifPresent(a -> {
				Kingdom test = Kingdom.getKingdom(a.getClan());
				if (test != null) {
					Quest upside = test.getQuest("Down Upside");
					if (upside != null) {
						Location nearest = e.getEntity().getWorld().locateNearestStructure(e.getEntity().getLocation(), StructureType.END_CITY, 30, false);
						if (nearest == null) return;
						if (nearest.distanceSquared(e.getEntity().getLocation()) > 5) return;
						if (upside.activated((Player) e.getEntity())) {
							upside.progress(1.0);
							a.getClan().getMembers().forEach(ass -> {
								Player online = ass.getUser().toBukkit().getPlayer();
								if (online != null) {
									addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) upside.getProgression()).intValue(), ((Number) upside.getRequirement()).intValue(), 73)).deploy();
								}
							});
							if (upside.isComplete()) {
								a.getClan().getMembers().forEach(ass -> {
									Player n = ass.getUser().toBukkit().getPlayer();
									if (n != null) {
										upside.deactivate(n);
									}
								});
							}
						}
					}
				}
			});
		}
	}

	@Subscribe
	public void onEnterCity(DefaultEvent.BlockBreak e) {
		ClansAPI.getInstance().getAssociate(e.getPlayer().getUniqueId()).ifPresent(a -> {
			Kingdom test = Kingdom.getKingdom(a.getClan());
			if (test != null) {
				Quest upside = test.getQuest("Down Upside");
				if (upside != null) {
					Location nearest = e.getBlock().getWorld().locateNearestStructure(e.getBlock().getLocation(), StructureType.END_CITY, 30, false);
					if (nearest == null) return;
					if (nearest.distanceSquared(e.getPlayer().getLocation()) > 5) return;
					if (upside.activated(e.getPlayer())) {
						upside.progress(1.0);
						a.getClan().getMembers().forEach(ass -> {
							Player online = ass.getUser().toBukkit().getPlayer();
							if (online != null) {
								addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) upside.getProgression()).intValue(), ((Number) upside.getRequirement()).intValue(), 73)).deploy();
							}
						});
						if (upside.isComplete()) {
							a.getClan().getMembers().forEach(ass -> {
								Player n = ass.getUser().toBukkit().getPlayer();
								if (n != null) {
									upside.deactivate(n);
								}
							});
						}
					}
				}
			}
		});
	}

	@Subscribe
	public void onKill(PlayerKillPlayerEvent e) {
		ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
			if (a.getClan().getMember(m -> m.getId().equals(e.getVictim().getUniqueId())) == null) {

				Claim c = ClansAPI.getInstance().getClaimManager().getClaim(e.getPlayer().getLocation());
				if (c != null) {
					if (!c.getOwner().getTag().getId().equals(a.getClan().getId().toString())) {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest kill = k.getQuest("Killer");
							if (kill != null) {
								if (kill.activated(e.getPlayer())) {
									kill.progress(1.0);
									a.getClan().getMembers().forEach(ass -> {
										Player online = ass.getUser().toBukkit().getPlayer();
										if (online != null) {
											addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) kill.getProgression()).intValue(), ((Number) kill.getRequirement()).intValue(), 73)).deploy();
										}
									});
									if (kill.isComplete()) {
										a.getClan().getMembers().forEach(ass -> {
											Player n = ass.getUser().toBukkit().getPlayer();
											if (n != null) {
												kill.deactivate(n);
											}
										});
									}
								}
							}
						}
					}
				}

			}
		});
	}

	@EventHandler
	public void onSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
			if (e.getEntity() instanceof Sheep) {
				e.getEntity().getNearbyEntities(4, 4, 4).stream().filter(ent -> ent instanceof Player).map(entity -> (Player) entity).forEach(p -> {
					ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest sheep = k.getQuest("Colorful Child");
							if (sheep == null) return;
							if (sheep.activated(p)) {
								sheep.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) sheep.getProgression()).intValue(), ((Number) sheep.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (sheep.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											sheep.deactivate(n);
										}
									});
								}
							}
						}
					});
				});
			}
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY, processCancelled = true)
	public void onJobComplete(KingdomQuestCompletionEvent e) {
		Reward<?> reward = e.getQuest().getReward();
		if (reward != null) {
			reward.give(e.getKingdom());
			e.getKingdom().forEach(c -> {
				c.broadcast("&bQuest &e" + e.getQuest().getTitle() + " &bcomplete.");
				c.broadcast(reward.getMessage());
			});
		}
	}

	@Subscribe
	public void onFarm(DefaultEvent.BlockBreak e) {
		if (!e.isCancelled()) {
			if (e.getBlock().getType() == Material.WHEAT ||
					e.getBlock().getType() == Material.POTATO ||
					e.getBlock().getType() == Material.MELON ||
					e.getBlock().getType() == Material.CARROTS ||
					e.getBlock().getType() == Material.BAMBOO ||
					e.getBlock().getType() == Material.COCOA ||
					e.getBlock().getType() == Material.BEETROOTS ||
					e.getBlock().getType() == Material.SUGAR_CANE) {
				ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
					Kingdom test = Kingdom.getKingdom(a.getClan());
					if (test != null) {
						Quest farmer = test.getQuest("The Farmer");
						if (farmer == null) return;
						if (farmer.activated(e.getPlayer())) {
							PrivateContainer container = OrdinalProcedure.select(a, 1, 420).select(32).cast(() -> PrivateContainer.class);
							if (container.get(Boolean.class, "quests.farmer." + e.getBlock().getType().name()) == null) {
								farmer.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) farmer.getProgression()).intValue(), ((Number) farmer.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (farmer.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										OrdinalProcedure.select(a, 1, 420).select(32).cast(() -> PrivateContainer.class).set("quests.farmer", null);
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											farmer.deactivate(n);
										}
									});
								}
								container.set("quests.farmer." + e.getBlock().getType().name(), true);
							}
						}
					}
				});
			}
			if (e.getBlock().getType() == Material.OBSIDIAN) {
				ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
					Kingdom test = Kingdom.getKingdom(a.getClan());
					if (test != null) {
						Quest miner = test.getQuest("The Miner");
						if (miner == null) return;
						if (miner.activated(e.getPlayer())) {
							miner.progress(1.0);
							a.getClan().getMembers().forEach(ass -> {
								Player online = ass.getUser().toBukkit().getPlayer();
								if (online != null) {
									addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) miner.getProgression()).intValue(), ((Number) miner.getRequirement()).intValue(), 73)).deploy();
								}
							});
							if (miner.isComplete()) {
								a.getClan().getMembers().forEach(ass -> {
									Player n = ass.getUser().toBukkit().getPlayer();
									if (n != null) {
										miner.deactivate(n);
									}
								});
							}
						}
					}
				});
			}
			if (e.getBlock().getType() == Material.CRYING_OBSIDIAN) {
				ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
					Kingdom test = Kingdom.getKingdom(a.getClan());
					if (test != null) {
						Quest miner = test.getQuest("The Back Breaker");
						if (miner == null) return;
						if (miner.activated(e.getPlayer())) {
							miner.progress(1.0);
							a.getClan().getMembers().forEach(ass -> {
								Player online = ass.getUser().toBukkit().getPlayer();
								if (online != null) {
									addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) miner.getProgression()).intValue(), ((Number) miner.getRequirement()).intValue(), 73)).deploy();
								}
							});
							if (miner.isComplete()) {
								a.getClan().getMembers().forEach(ass -> {
									Player n = ass.getUser().toBukkit().getPlayer();
									if (n != null) {
										miner.deactivate(n);
									}
								});
							}
						}
					}
				});
			}
		}
	}

	@EventHandler
	public void onCraft(InventoryClickEvent e) {
		ClansAPI.getInstance().getAssociate((OfflinePlayer) e.getWhoClicked()).ifPresent(a -> {
			if (e.getSlotType() == InventoryType.SlotType.RESULT) {
				if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BREAD) {
					Kingdom test = Kingdom.getKingdom(a.getClan());
					if (test != null) {
						Quest bread = test.getQuest("The Farmer");
						if (bread == null) return;
						if (bread.activated((Player) e.getWhoClicked())) {
							bread.progress(e.getCurrentItem().getAmount());
							a.getClan().getMembers().forEach(ass -> {
								Player online = ass.getUser().toBukkit().getPlayer();
								if (online != null) {
									addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) bread.getProgression()).intValue(), ((Number) bread.getRequirement()).intValue(), 73)).deploy();
								}
							});
							if (bread.isComplete()) {
								a.getClan().getMembers().forEach(ass -> {
									Player n = ass.getUser().toBukkit().getPlayer();
									if (n != null) {
										bread.deactivate(n);
									}
								});
							}
						}
					}
				}
			}
		});
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if (e.getEntity() instanceof Piglin) {
			Piglin pig = (Piglin) e.getEntity();
			if (pig.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) pig.getLastDamageCause();
				if (event.getDamager() instanceof Player) {
					Player killer = (Player) event.getDamager();
					ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest kill = k.getQuest("Tainted Beef");
							if (kill == null) return;
							if (kill.activated(killer)) {
								if (!pig.isBaby()) return;
								kill.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) kill.getProgression()).intValue(), ((Number) kill.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (kill.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											kill.deactivate(n);
										}
									});
								}
							}
						}
					});
				}
			}
		}
		if (e.getEntity() instanceof Blaze) {
			Blaze blaze = (Blaze) e.getEntity();
			if (blaze.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) blaze.getLastDamageCause();
				if (event.getDamager() instanceof Player) {
					Player killer = (Player) event.getDamager();
					ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest kill = k.getQuest("Hot Feet");
							if (kill == null) return;
							if (kill.activated(killer)) {
								kill.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) kill.getProgression()).intValue(), ((Number) kill.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (kill.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											kill.deactivate(n);
										}
									});
								}
							}
						}
					});
				}
			}
		}
		if (e.getEntity() instanceof WitherSkeleton) {
			WitherSkeleton witherSkeleton = (WitherSkeleton) e.getEntity();
			if (witherSkeleton.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) witherSkeleton.getLastDamageCause();
				if (event.getDamager() instanceof Player) {
					Player killer = (Player) event.getDamager();
					ClansAPI.getInstance().getAssociate(killer).ifPresent(a -> {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest kill = k.getQuest("Dark Soldier");
							if (kill == null) return;
							if (kill.activated(killer)) {
								kill.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) kill.getProgression()).intValue(), ((Number) kill.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (kill.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											kill.deactivate(n);
										}
									});
								}
							}
						}
					});
				}
			}
		}
		if (e.getEntity() instanceof Ghast) {
			Ghast ghast = (Ghast) e.getEntity();
			if (ghast.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) ghast.getLastDamageCause();
				if (event.getDamager() instanceof Projectile) {
					Projectile killer = (Projectile) event.getDamager();
					if (!(killer.getShooter() instanceof Player)) return;
					ClansAPI.getInstance().getAssociate((OfflinePlayer) killer.getShooter()).ifPresent(a -> {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest kill = k.getQuest("Soulless Driver");
							if (kill == null) return;
							if (kill.activated((Player) killer.getShooter())) {
								kill.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) kill.getProgression()).intValue(), ((Number) kill.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (kill.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											kill.deactivate(n);
										}
									});
								}
							}
						}
					});
				}
			}
		}
	}

	@EventHandler
	public void onFirework(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getItem() != null) {
				if (e.getItem().getType() == Material.FIREWORK_ROCKET) {
					ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
						Kingdom k = Kingdom.getKingdom(a.getClan());
						if (k != null) {
							Quest light = k.getQuest("Skylight");
							if (light == null) return;
							if (light.activated(e.getPlayer())) {
								light.progress(1.0);
								a.getClan().getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) light.getProgression()).intValue(), ((Number) light.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (light.isComplete()) {
									a.getClan().getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											light.deactivate(n);
										}
									});
								}
							}
						}
					});
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY, processCancelled = true)
	public void onJobComplete(RoundTableQuestCompletionEvent e) {
		Reward<?> reward = e.getQuest().getReward();
		if (reward != null) {
			e.getTable().forEach(a -> {
				Player online = a.getUser().toBukkit().getPlayer();
				if (online != null) {
					reward.give(a);
					new FancyMessage(ClansAPI.getInstance().getPrefix().joined() + " &bQuest &e" + e.getQuest().getTitle() + " &bcomplete.").send(online).deploy();
					new FancyMessage(ClansAPI.getInstance().getPrefix().joined()).then(" ").append(new ComponentChunk(reward.getMessage())).send(online).deploy();
				}
			});
		}
	}

	@EventHandler
	public void onBarter(InventoryClickEvent e) {
		if (e.getInventory().getType() == InventoryType.MERCHANT) {
			if (e.getWhoClicked().getWorld().getName().contains("nether")) {
				if (!(e.getWhoClicked() instanceof Player)) return;
				ClansAPI.getInstance().getAssociate((OfflinePlayer) e.getWhoClicked()).ifPresent(a -> {
					Clan c = a.getClan();
					String name = c.getValue(String.class, "kingdom");
					if (name != null) {
						Kingdom kingdom = Kingdom.getKingdom(name);
						if (kingdom != null) {
							Quest q = kingdom.getQuest("The Trade");
							if (q != null) {
								if (q.activated((Player) e.getWhoClicked())) {
									q.progress(1.0);
									c.getMembers().forEach(ass -> {
										Player online = ass.getUser().toBukkit().getPlayer();
										if (online != null) {
											addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) q.getProgression()).intValue(), ((Number) q.getRequirement()).intValue(), 73)).deploy();
										}
									});
									if (q.isComplete()) {
										c.getMembers().forEach(ass -> {
											Player n = ass.getUser().toBukkit().getPlayer();
											if (n != null) {
												q.deactivate(n);
											}
										});
									}
								}
							}
						}
					}
				});
			}
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onSpawner(DefaultEvent.BlockBreak e) {
		if (e.isCancelled()) return;
		Block b = e.getBlock();
		if (b.getType() == Material.SPAWNER) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Clan c = a.getClan();
				String name = c.getValue(String.class, "kingdom");
				if (name != null) {
					Kingdom kingdom = Kingdom.getKingdom(name);
					if (kingdom != null) {
						Quest q = kingdom.getQuest("Monsters Box");
						if (q != null) {
							if (q.activated(e.getPlayer())) {
								q.progress(1.0);
								c.getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) q.getProgression()).intValue(), ((Number) q.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (q.isComplete()) {
									c.getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											q.deactivate(n);
										}
									});
								}
							}
						}
					}
				}
			});
		}
		if (b.getType() == Material.MYCELIUM) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Clan c = a.getClan();
				String name = c.getValue(String.class, "kingdom");
				if (name != null) {
					Kingdom kingdom = Kingdom.getKingdom(name);
					if (kingdom != null) {
						Quest q = kingdom.getQuest("Dirty Hands");
						if (q != null) {
							if (q.activated(e.getPlayer())) {
								q.progress(1.0);
								c.getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) q.getProgression()).intValue(), ((Number) q.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (q.isComplete()) {
									c.getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											q.deactivate(n);
										}
									});
								}
							}
						}
					}
				}
			});
		}
		if (StringUtils.use(b.getType().name()).containsIgnoreCase("log")) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Clan c = a.getClan();
				String name = c.getValue(String.class, "kingdom");
				if (name != null) {
					Kingdom kingdom = Kingdom.getKingdom(name);
					if (kingdom != null) {
						Quest q = kingdom.getQuest("Lumberjack");
						if (q != null) {
							if (q.activated(e.getPlayer())) {
								q.progress(1.0);
								c.getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) q.getProgression()).intValue(), ((Number) q.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (q.isComplete()) {
									c.getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											q.deactivate(n);
										}
									});
								}
							}
						}
					}
				}
			});
		}
		if (StringUtils.use(b.getType().name()).containsIgnoreCase("diamond")) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Clan c = a.getClan();
				String name = c.getValue(String.class, "kingdom");
				if (name != null) {
					Kingdom kingdom = Kingdom.getKingdom(name);
					if (kingdom != null) {
						Quest q = kingdom.getQuest("Diamond Back");
						if (q != null) {
							if (q.activated(e.getPlayer())) {
								q.progress(1.0);
								c.getMembers().forEach(ass -> {
									Player online = ass.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) q.getProgression()).intValue(), ((Number) q.getRequirement()).intValue(), 73)).deploy();
									}
								});
								if (q.isComplete()) {
									c.getMembers().forEach(ass -> {
										Player n = ass.getUser().toBukkit().getPlayer();
										if (n != null) {
											q.deactivate(n);
										}
									});
								}
							}
						}
					}
				}
			});
		}

	}

	@Subscribe
	public void onClanLeave(AssociateQuitEvent e) {
		if (e.getAssociate().getPriority() == RankPriority.HIGHEST) {

			Clan c = e.getAssociate().getClan();

			String key = c.getValue(String.class, "kingdom");

			if (key != null) {

				Kingdom k = Kingdom.getKingdom(key);

				if (k.getMembers().size() == 1) {

					addon.getMailer().prefix().start(Clan.ACTION.getPrefix()).finish().announce(player -> true, "&rKingdom &6" + k.getName() + " &rhas fallen").deploy();

					k.remove(addon);

				} else {
					k.getMembers().remove(c);
				}

			}

		}
	}

	@Subscribe
	public void onInteract(ClaimInteractEvent e) {
		Player p = e.getPlayer();

		Block b = e.getBlock();

		ClansAPI API = ClansAPI.getInstance();

		if (e.getInteraction() == ClaimInteractEvent.Type.BUILD) {

			if (API.isInClan(p.getUniqueId())) {

				Clan c = API.getClanManager().getClan(p.getUniqueId());

				Kingdom k = Kingdom.getKingdom(c);

				if (k != null) {


					Quest achievement;

					Stream<Material> stream = Stream.of(Material.STONE_BRICKS, Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DIORITE, Material.ANDESITE);

					if (stream.anyMatch(m -> StringUtils.use(m.name()).containsIgnoreCase(b.getType().name()))) {

						achievement = k.getQuest("Walls");

						if (achievement == null) return;

						if (achievement.activated(p)) {

							if (!achievement.isComplete()) {

								achievement.progress(1);

								c.getMembers().forEach(a -> {
									Player online = a.getUser().toBukkit().getPlayer();
									if (online != null) {
										addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) achievement.getProgression()).intValue(), ((Number) achievement.getRequirement()).intValue(), 73)).deploy();
									}
								});


							} else {

								c.getMembers().forEach(ass -> {
									Player n = ass.getUser().toBukkit().getPlayer();
									if (n != null) {
										achievement.deactivate(n);
									}
								});

							}
						}

					}

				}

			}

		}

		if (e.getInteraction() == ClaimInteractEvent.Type.BREAK) {

			if (API.isInClan(p.getUniqueId())) {

				Clan c = API.getClanManager().getClan(p.getUniqueId());

				Kingdom k = Kingdom.getKingdom(c);

				if (k != null) {


					Quest achievement;

					Stream<Material> stream = Stream.of(Material.STONE_BRICKS, Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DIORITE, Material.ANDESITE);

					if (stream.anyMatch(m -> StringUtils.use(m.name()).containsIgnoreCase(b.getType().name()))) {

						if (StringUtils.use(b.getType().name()).containsIgnoreCase("stair", "step", "slab")) {
							achievement = k.getQuest("Gate");

							if (achievement == null) return;

							if (achievement.activated(p)) {

								if (!achievement.isComplete()) {

									achievement.unprogress(1);

									c.getMembers().forEach(a -> {
										Player online = a.getUser().toBukkit().getPlayer();
										if (online != null) {
											addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) achievement.getProgression()).intValue(), ((Number) achievement.getRequirement()).intValue(), 73)).deploy();
										}
									});
								}
							}
						} else {

							achievement = k.getQuest("Walls");

							if (achievement == null) return;

							if (achievement.activated(p)) {

								if (!achievement.isComplete()) {

									achievement.unprogress(1);

									c.getMembers().forEach(a -> {
										Player online = a.getUser().toBukkit().getPlayer();
										if (online != null) {
											addon.getMailer().accept(online).action(KingdomCommand.getProgressBar(((Number) achievement.getProgression()).intValue(), ((Number) achievement.getRequirement()).intValue(), 73)).deploy();
										}
									});


								}
							}
						}

					}

				}

			}

		}
	}

}
