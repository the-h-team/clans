package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.ClanVentCall;
import com.github.sanctum.clans.model.backend.ClanFileBackend;
import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.LogoHolder;
import com.github.sanctum.clans.util.ShieldTamper;
import com.github.sanctum.clans.event.claim.ClaimInteractEvent;
import com.github.sanctum.clans.event.claim.RaidShieldEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.DefaultEvent;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class BlockEventListener implements Listener {

	@Subscribe
	public void onInteract(DefaultEvent.Interact event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (ClansAPI.getInstance()
					.getClaimManager().getClaim(event.getBlock().get().getLocation()) != null) {
				ClaimInteractEvent e = ClanVentBus.call(new ClaimInteractEvent(event.getPlayer(), event.getBlock().get().getLocation(), ClaimInteractEvent.Type.USE));
				if (e.isCancelled()) {
					event.setCancelled(true);
					event.setResult(Event.Result.DENY);
				}
			}
		}
	}

	@Subscribe
	public void onBreak(DefaultEvent.BlockBreak event) {
		final Block b = event.getBlock();
		if (ClansAPI.getInstance().getClaimManager().getClaim(b.getLocation()) != null) {
			ClaimInteractEvent e = ClanVentBus.call(new ClaimInteractEvent(event.getPlayer(), b, b.getLocation(), ClaimInteractEvent.Type.BREAK));
			if (e.isCancelled()) {
				event.setCancelled(e.isCancelled());
			} else {
				if (Claim.getResident(event.getPlayer()) != null) {
					final Material type = b.getState().getType();
					final Byte data = b.getState().getRawData();
					Claim.getResident(event.getPlayer()).getInfo().addBroken(b, type, data);
				}
			}
		}
	}

	@Subscribe
	public void onBuild(DefaultEvent.BlockPlace event) {
		if (ClansAPI.getInstance().getClaimManager().getClaim(event.getBlock().getLocation()) != null) {
			ClaimInteractEvent e = ClanVentBus.call(new ClaimInteractEvent(event.getPlayer(), event.getBlock(), event.getBlock().getLocation(), ClaimInteractEvent.Type.BUILD));
			if (e.isCancelled()) {
				event.setCancelled(e.isCancelled());
			} else {
				if (Claim.getResident(event.getPlayer()) != null) {
					Claim.getResident(event.getPlayer()).getInfo().addPlaced(event.getBlock());
				}
			}
		}
	}

	@Subscribe(priority = Vent.Priority.LOW)
	public void onAdjust(RaidShieldEvent e) {
		ShieldTamper edit = ClansAPI.getInstance().getShieldManager().getTamper();
		if (edit.isOff()) {
			e.setCancelled(true);
		} else {
			if (edit.getUpTime() != 0) {
				e.setStartTime(edit.getUpTime());
				e.setStopTime(edit.getDownTime());
			}
		}
	}

	@Subscribe(priority = Vent.Priority.READ_ONLY)
	public void onShield(RaidShieldEvent e) {
		final ClansAPI api = e.getApi();
		final ClanFileBackend action = Clan.ACTION;
		final World world = Optional.ofNullable(Bukkit.getWorld(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.raid-shield.main-world"))).orElse(Bukkit.getWorlds().get(0));
		if (action.isNight(world, e.getStartTime(), e.getStopTime())) {
			if (api.getShieldManager().isEnabled()) {
				api.getShieldManager().setEnabled(false);
				if (ClansAPI.getDataInstance().getConfigString("Clans.raid-shield.mode").equals("TEMPORARY")) {
					api.getClanManager().getClans().forEach(c -> {
						for (Claim claim : c.getClaims()) {
							claim.setActive(false);
						}
					});
				}
				if (e.getShieldOn().equals("{0} &a&lRAID SHIELD ENABLED")) {
					e.setShieldOff(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.raid-shield.messages.disabled"));
				}
				if (ClansAPI.getDataInstance().isTrue("Clans.raid-shield.send-messages")) {
					Bukkit.broadcastMessage(Clan.ACTION.color(MessageFormat.format(e.getShieldOff(), Clan.ACTION.getPrefix())));
				}
			}
		}
		if (!action.isNight(world, e.getStartTime(), e.getStopTime())) {
			if (!api.getShieldManager().isEnabled()) {
				api.getShieldManager().setEnabled(true);
				if (ClansAPI.getDataInstance().getConfigString("Clans.raid-shield.mode").equals("TEMPORARY")) {
					api.getClanManager().getClans().forEach(c -> {
						for (Claim claim : c.getClaims()) {
							claim.setActive(true);
						}
					});
				}
				if (e.getShieldOn().equals("{0} &a&lRAID SHIELD ENABLED")) {
					e.setShieldOn(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.raid-shield.messages.enabled"));
				}
				if (ClansAPI.getDataInstance().isTrue("Clans.raid-shield.send-messages")) {
					Bukkit.broadcastMessage(Clan.ACTION.color(MessageFormat.format(e.getShieldOn(), Clan.ACTION.getPrefix())));
				}
			}
		}
	}

	@Subscribe
	public void onClaimInteract(ClaimInteractEvent e) {
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
		if (!e.getClaim().isActive()) return;
		Claim.Flag f = e.getClaim().getFlag("allies-share-land");
		if (associate != null && associate.isValid()) {
			switch (e.getInteraction()) {
				case USE:
					if (e.getBlock().getType().isInteractable()) {
						if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
							// not in the same clan
							if (!e.getPlayer().hasPermission("clans.admin")) {
								if (f != null && f.isEnabled()) {
									// only deny if not ally.
									if (!((Clan) e.getClaim().getHolder()).getRelation().getAlliance().has(associate.getClan())) {
										e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
										e.setCancelled(true);
									}
								} else {
									// deny
									e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
									e.setCancelled(true);
								}
							}
						} else {
							if (!Clearance.LAND_USE_INTERACTABLE.test(associate)) {
								Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
								e.setCancelled(true);
							}
						}
					}
					if (!e.isCancelled() && StringUtils.use(e.getPlayer().getInventory().getItemInMainHand().getType().name()).containsIgnoreCase("bucket")) {
						if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
							if (!e.getPlayer().hasPermission("clans.admin")) {
								if (f != null && f.isEnabled()) {
									// only deny if not ally.
									if (!((Clan) e.getClaim().getHolder()).getRelation().getAlliance().has(associate.getClan())) {
										e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
										e.setCancelled(true);
									}
								} else {
									// deny
									e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
									e.setCancelled(true);
								}
							}
						} else {
							if (!Clearance.LAND_USE_INTERACTABLE.test(associate)) {
								Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
								e.setCancelled(true);
							}
						}
					}
					break;
				case BREAK:
				case BUILD:
					if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
						if (!e.getPlayer().hasPermission("clans.admin")) {
							if (f != null && f.isEnabled()) {
								// only deny if not ally.
								if (!((Clan) e.getClaim().getHolder()).getRelation().getAlliance().has(associate.getClan())) {
									e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
									e.setCancelled(true);
								}
							} else {
								// deny
								e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
								e.setCancelled(true);
							}
						}
					} else {
						if (!Clearance.LAND_USE.test(associate)) {
							Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
							e.setCancelled(true);
						}
					}
					break;
			}
		} else {
			// player interacting isn't in a clan just a normal player.
			switch (e.getInteraction()) {
				case USE:
					if (e.getBlock().getType().isInteractable()) {
						if (!e.getPlayer().hasPermission("clans.admin")) {
							e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
							e.setCancelled(true);
						}
					}
					if (!e.isCancelled() && StringUtils.use(e.getPlayer().getInventory().getItemInMainHand().getType().name()).containsIgnoreCase("bucket")) {
						if (!e.getPlayer().hasPermission("clans.admin")) {
							e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
							e.setCancelled(true);
						}
					}
					break;
				case BREAK:
				case BUILD:
					if (!e.getPlayer().hasPermission("clans.admin")) {
						e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
						e.setCancelled(true);
					}
					break;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			if (ClansAPI.getInstance().getClaimManager().getClaim(event.getEntity().getLocation()) != null) {
				ClaimInteractEvent e = new ClanVentCall<>(new ClaimInteractEvent(p, event.getEntity().getLocation(), ClaimInteractEvent.Type.USE)).run();
				if (e.isCancelled()) {
					if (event.getEntity().getType() != EntityType.TRIDENT) {
						e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan)e.getClaim().getHolder()).getName()), ((Clan)e.getClaim().getHolder()).getName()));
						event.getEntity().remove();
					}
				}
			}
		}
	}


}
