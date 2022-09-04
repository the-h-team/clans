package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.ClanVentCall;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.LogoHolder;
import com.github.sanctum.clans.construct.extra.ShieldTamper;
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
				if (event.getItem() != null) {
					if (StringUtils.use(event.getItem().getType().name()).containsIgnoreCase("bucket")) return;
				}
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
					Claim.getResident(event.getPlayer()).addBroken(b, type, data);
				}
			}
		}
		if (event.isCancelled()) return;
		if (StringUtils.use(b.getType().name()).containsIgnoreCase("sign")) {
			Sign sign = (Sign) b.getState();
			if (sign.getLine(1).equals(StringUtils.use("&6&lLogo").translate())) {
				LogoHolder.Carrier test = LogoHolder.getCarrier(b.getLocation());
				if (test != null) {
					Clan.ACTION.sendMessage(event.getPlayer(), "&aClan logo removed.");
					if (test.getHolder() != null) {
						test.getHolder().remove(test);
					}
					Set<ArmorStand> doubleCheck = LogoHolder.getStands(b.getLocation().add(0.5, 0, 0.5));
					doubleCheck.forEach(Entity::remove);
					test.getHolder().save();
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
					Claim.getResident(event.getPlayer()).addPlaced(event.getBlock());
				}
			}
		}
		if (event.isCancelled()) return;
		if (StringUtils.use(event.getBlock().getType().name()).containsIgnoreCase("fence")) {
			LogoHolder.Carrier test = LogoHolder.getCarrier(event.getBlock().getLocation());
			if (test != null) {
				Clan.ACTION.sendMessage(event.getPlayer(), "&aClan logo removed.");
				if (test.getHolder() != null) {
					test.getHolder().remove(test);
				}
				Set<ArmorStand> doubleCheck = LogoHolder.getStands(event.getBlock().getLocation().add(0.5, 0, 0.5));
				doubleCheck.forEach(Entity::remove);
				test.getHolder().save();
			}
		}
	}

	@EventHandler
	public void onSign(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[Clan]")) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Sign s = (Sign) e.getBlock().getState();
				if (!Clearance.LOGO_DISPLAY.test(a)) {
					e.setLine(0, StringUtils.use("&4[Clan]").translate());
					Clan.ACTION.sendMessage(e.getPlayer(), "&cFailed to display logo, " + Clan.ACTION.noClearance());
					return;
				}
				if (LogoHolder.getCarrier(s.getLocation().add(0.5, 0, 0.5)) != null) {
					e.setLine(0, StringUtils.use("&4[Clan]").translate());
					Clan.ACTION.sendMessage(e.getPlayer(), "&cToo close to another hologram! Max logo hologram's per chunk is 1.");
					return;
				}
				List<String> list = a.getClan().getLogo();
				if (list != null) {
					int length = ChatColor.stripColor(StringUtils.use(list.get(0)).translate()).length();
					if (length > 16) {
						e.setLine(0, StringUtils.use("&4[Clan]").translate());
						Clan.ACTION.sendMessage(e.getPlayer(), "&cMaximum logo hologram size is 16x16, ours is too big.");
						return;
					}
					if (EconomyProvision.getInstance().isValid()) {
						final EconomyProvision provision = EconomyProvision.getInstance();
						final BigDecimal am = BigDecimal.valueOf(125);
						if (provision.has(am, e.getPlayer()).orElse(false)) {
							provision.withdraw(am, e.getPlayer()).orElse(false);
							e.setLine(0, StringUtils.use("&l[Clan]").translate());
							e.setLine(1, StringUtils.use("&6&lLogo").translate());
							e.setLine(2, a.getClan().getId().toString());
							Clan.ACTION.sendMessage(e.getPlayer(), "&aClan logo now on display &r(&e#&2" + a.getClan().newCarrier(e.getBlock().getLocation()).getId() + "&r)");
							a.getClan().save();
						} else {
							e.setLine(0, StringUtils.use("&4[Clan]").translate());
							Clan.ACTION.sendMessage(e.getPlayer(), "&cFailed to display logo, not enough money.");
						}
					} else {
						e.setLine(0, StringUtils.use("&l[Clan]").translate());
						e.setLine(1, StringUtils.use("&6&lLogo").translate());
						e.setLine(2, a.getClan().getId().toString());
						Clan.ACTION.sendMessage(e.getPlayer(), "&aClan logo now on display &r(&e#&2" + a.getClan().newCarrier(e.getBlock().getLocation()).getId() + "&r)");
						a.getClan().save();
					}
				}
			});
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
		final ClanAction action = Clan.ACTION;
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
		if (associate != null && associate.isValid()) {
			switch (e.getInteraction()) {
				case USE:
					if (e.getBlock().getType().isInteractable()) {
						if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
							if (!e.getPlayer().hasPermission("clanspro.admin")) {
								if (!((Clan) e.getClaim().getHolder()).getRelation().getAlliance().has(associate.getClan())) {
									e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
									e.setCancelled(true);
								}
							}
						} else {
							if (!Clearance.LAND_USE_INTRACTABLE.test(associate)) {
								Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
								e.setCancelled(true);
							}
						}
					}
					if (!e.isCancelled() && StringUtils.use(e.getPlayer().getInventory().getItemInMainHand().getType().name()).containsIgnoreCase("bucket")) {
						if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
							if (!e.getPlayer().hasPermission("clanspro.admin")) {
								if (!((Clan) e.getClaim().getHolder()).getRelation().getAlliance().has(associate.getClan())) {
									e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
									e.setCancelled(true);
								}
							}
						} else {
							if (!Clearance.LAND_USE_INTRACTABLE.test(associate)) {
								Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
								e.setCancelled(true);
							}
						}
					}
					break;
				case BREAK:
				case BUILD:
					if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
						if (!e.getPlayer().hasPermission("clanspro.admin")) {
							if (!((Clan) e.getClaim().getHolder()).getRelation().getAlliance().has(associate.getClan())) {
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
			switch (e.getInteraction()) {
				case USE:
					if (e.getBlock().getType().isInteractable()) {
						if (!e.getPlayer().hasPermission("clanspro.admin")) {
							e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
							e.setCancelled(true);
						}
					}
					if (!e.isCancelled() && StringUtils.use(e.getPlayer().getInventory().getItemInMainHand().getType().name()).containsIgnoreCase("bucket")) {
						if (!e.getPlayer().hasPermission("clanspro.admin")) {
							e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(((Clan) e.getClaim().getHolder()).getName()), ((Clan) e.getClaim().getHolder()).getName()));
							e.setCancelled(true);
						}
					}
					break;
				case BREAK:
				case BUILD:
					if (!e.getPlayer().hasPermission("clanspro.admin")) {
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
