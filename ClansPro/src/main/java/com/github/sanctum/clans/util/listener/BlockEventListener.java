package com.github.sanctum.clans.util.listener;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.misc.ShieldTamper;
import com.github.sanctum.clans.util.InteractionType;
import com.github.sanctum.clans.util.events.clans.ClaimInteractEvent;
import com.github.sanctum.clans.util.events.clans.RaidShieldEvent;
import com.github.sanctum.labyrinth.task.Schedule;
import java.text.MessageFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class BlockEventListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onRaidShield(RaidShieldEvent e) {

		if (!e.isCancelled()) {

			String world = ClansAPI.getData().getMain().getConfig().getString("Clans.raid-shield.main-world");
			if (world == null || Bukkit.getWorld(world) == null) {
				world = Bukkit.getWorlds().get(0).getName();
			}
			if (DefaultClan.action.isNight(world, e.getStartTime(), e.getStopTime())) {
				if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
					ClansAPI.getInstance().getShieldManager().setEnabled(false);
					if (e.getShieldOn().equals("{0} &a&lRAID SHIELD ENABLED")) {
						e.setShieldOff(ClansAPI.getData().getMain().getConfig().getString("Clans.raid-shield.messages.disabled"));
					}
					if (ClansAPI.getData().getEnabled("Clans.raid-shield.send-messages")) {
						Bukkit.broadcastMessage(DefaultClan.action.color(MessageFormat.format(e.getShieldOff(), DefaultClan.action.getPrefix())));
					}
				}
			}
			if (!DefaultClan.action.isNight(world, e.getStartTime(), e.getStopTime())) {
				if (!ClansAPI.getInstance().getShieldManager().isEnabled()) {
					ClansAPI.getInstance().getShieldManager().setEnabled(true);
					if (e.getShieldOn().equals("{0} &a&lRAID SHIELD ENABLED")) {
						e.setShieldOn(ClansAPI.getData().getMain().getConfig().getString("Clans.raid-shield.messages.enabled"));
					}
					if (ClansAPI.getData().getEnabled("Clans.raid-shield.send-messages")) {
						Bukkit.broadcastMessage(DefaultClan.action.color(MessageFormat.format(e.getShieldOn(), DefaultClan.action.getPrefix())));
					}
				}
			}
		}

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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClaimInteractDefault(ClaimInteractEvent e) {
		if (ClansAPI.getInstance().getClaimManager().isInClaim(e.getLocation())) {
			if (ClansAPI.getInstance().getAssociate(e.getPlayer()).isPresent()) {
				if (!e.getClaim().getOwner().equals(e.getUtil().getClanID(e.getPlayer().getUniqueId()))) {
					if (!e.getPlayer().hasPermission("clanspro.claim.bypass")) {
						if (!e.getClaim().getClan().getAllyList().contains(e.getUtil().getClanID(e.getPlayer().getUniqueId()))) {
							e.setCancelled(true);
						}
					}
				}
			} else {
				if (!e.getPlayer().hasPermission("clanspro.claim.bypass")) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			ClaimInteractEvent e = new ClaimInteractEvent(p, event.getEntity().getLocation(), InteractionType.USE);
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				if (event.getEntity().getType() != EntityType.TRIDENT) {
					e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
					event.getEntity().remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation(), InteractionType.BREAK);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
			event.setCancelled(e.isCancelled());
		} else {
			if (Claim.getResident(event.getPlayer()) != null) {
				Schedule.sync(() -> Claim.getResident(event.getPlayer()).addBroken(event.getBlock())).run();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		ClaimInteractEvent e = new ClaimInteractEvent(event.getPlayer(), event.getBlock().getLocation(), InteractionType.BUILD);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			e.stringLibrary().sendMessage(e.getPlayer(), MessageFormat.format(e.stringLibrary().notClaimOwner(e.getClaim().getClan().getName()), e.getClaim().getClan().getName()));
			event.setCancelled(e.isCancelled());
		} else {
			if (Claim.getResident(event.getPlayer()) != null) {
				Schedule.sync(() -> Claim.getResident(event.getPlayer()).addPlaced(event.getBlock())).run();
			}
		}
	}


}
