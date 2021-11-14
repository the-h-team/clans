package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.LogoHolder;
import com.github.sanctum.clans.construct.extra.ShieldTamper;
import com.github.sanctum.clans.event.claim.ClaimInteractEvent;
import com.github.sanctum.clans.event.claim.RaidShieldEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class BlockEventListener implements Listener {


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
					test.getLines().forEach(LogoHolder.Carrier.Line::destroy);
					if (test.getHolder() != null) {
						test.getHolder().remove(test);
					}
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
	}

	@EventHandler
	public void onSign(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[Clan]")) {
			ClansAPI.getInstance().getAssociate(e.getPlayer()).ifPresent(a -> {
				Sign s = (Sign) e.getBlock().getState();
				if (!Clearance.LOGO_DISPLAY.test(a)) {
					s.setLine(0, StringUtils.use("&4[Clan]").translate());
					s.update();
					Clan.ACTION.sendMessage(e.getPlayer(), "&cFailed to display logo, " + Clan.ACTION.noClearance());
					return;
				}
				new FancyMessage().then(ClansAPI.getInstance().getPrefix().joined()).then(" ").then("This will cost &6$125&r,").then(" ").then("&2accept").action(() -> {
					if (LogoHolder.getCarrier(s.getLocation()) != null) {
						s.setLine(0, StringUtils.use("&4[Clan]").translate());
						s.update();
						Clan.ACTION.sendMessage(e.getPlayer(), "&cToo close to another hologram! Try somewhere else");
						return;
					}
					List<String> list = a.getClan().getLogo();
					if (list != null) {
						int length = ChatColor.stripColor(StringUtils.use(list.get(0)).translate()).length();
						if (length > 16) {
							s.setLine(0, StringUtils.use("&4[Clan]").translate());
							s.update();
							Clan.ACTION.sendMessage(e.getPlayer(), "&cMaximum logo hologram size is 16x16, ours is too big.");
							return;
						}

					}
					if (EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(125), e.getPlayer()).orElse(false)) {
						s.setLine(0, StringUtils.use("&l[Clan]").translate());
						s.setLine(1, StringUtils.use("&6&lLogo").translate());
						s.setLine(2, a.getClan().getId().toString());
						s.update();
						Clan.ACTION.sendMessage(e.getPlayer(), "&aClan logo now on display w/ id " + a.getClan().newCarrier(e.getBlock().getLocation()).getId());
					} else {
						s.setLine(0, StringUtils.use("&4[Clan]").translate());
						s.update();
						Clan.ACTION.sendMessage(e.getPlayer(), "&cFailed to display logo, not enough money.");
					}
				}).hover("&2&oClick to purchase this hologram.").then(" ").then("the charges?").send(e.getPlayer()).deploy();
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
		World world = Bukkit.getWorld(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.raid-shield.main-world"));
		if (world == null) {
			world = Bukkit.getWorlds().get(0);
		}
		if (Clan.ACTION.isNight(world, e.getStartTime(), e.getStopTime())) {
			if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
				ClansAPI.getInstance().getShieldManager().setEnabled(false);
				if (e.getShieldOn().equals("{0} &a&lRAID SHIELD ENABLED")) {
					e.setShieldOff(ClansAPI.getDataInstance().getConfig().getRoot().getString("Clans.raid-shield.messages.disabled"));
				}
				if (ClansAPI.getDataInstance().isTrue("Clans.raid-shield.send-messages")) {
					Bukkit.broadcastMessage(Clan.ACTION.color(MessageFormat.format(e.getShieldOff(), Clan.ACTION.getPrefix())));
				}
			}
		}
		if (!Clan.ACTION.isNight(world, e.getStartTime(), e.getStopTime())) {
			if (!ClansAPI.getInstance().getShieldManager().isEnabled()) {
				ClansAPI.getInstance().getShieldManager().setEnabled(true);
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
		if (ClansAPI.getInstance().getClaimManager().isInClaim(e.getLocation())) {
			Clan.Associate associate = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
			if (associate != null && associate.isValid()) {
				if (!e.getClaim().getOwner().getTag().getId().equals(associate.getClan().getId().toString())) {
					if (!e.getPlayer().hasPermission("clanspro.claim.bypass")) {
						if (!e.getClaim().getClan().getRelation().getAlliance().has(associate.getClan())) {
							e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
							e.setCancelled(true);
						}
					}
				} else {
					if (e.getInteraction() == ClaimInteractEvent.Type.USE) {
						if (!Clearance.LAND_USE_INTRACTABLE.test(associate)) {
							Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
							e.setCancelled(true);
						}
					} else {
						if (!Clearance.LAND_USE.test(associate)) {
							Clan.ACTION.sendMessage(e.getPlayer(), Clan.ACTION.noClearance());
							e.setCancelled(true);
						}
					}
				}
			} else {
				if (!e.getPlayer().hasPermission("clanspro.claim.bypass")) {
					e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			ClaimInteractEvent e = new Vent.Call<>(Vent.Runtime.Synchronous, new ClaimInteractEvent(p, event.getEntity().getLocation(), ClaimInteractEvent.Type.USE)).run();
			if (e.isCancelled()) {
				if (event.getEntity().getType() != EntityType.TRIDENT) {
					e.getUtil().sendMessage(e.getPlayer(), MessageFormat.format(e.getUtil().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
					event.getEntity().remove();
				}
			}
		}
	}


}
