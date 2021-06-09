package com.github.sanctum.clans.util.listener;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.ScoreTag;
import com.github.sanctum.clans.util.events.damage.PlayerPunchPlayerEvent;
import com.github.sanctum.clans.util.events.damage.PlayerShootPlayerEvent;
import com.github.sanctum.labyrinth.task.Schedule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityEventListener implements Listener {

	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		if (e.getEntity() instanceof Creeper) {
			if (ClansAPI.getInstance().getClaimManager().isInClaim(e.getEntity().getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			final Player p = (Player) e.getEntity();
			if (!Bukkit.getOnlinePlayers().contains(p)) {
				return;
			}
			if (!ClansPro.getInstance().dataManager.getMain().getConfig().getStringList("Clans.world-whitelist").contains(p.getWorld().getName())) {
				return;
			}
			if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
				if (ClansPro.getInstance().dataManager.prefixedTagsAllowed()) {
					Schedule.sync(() -> ScoreTag.set(p, ClansAPI.getData().prefixedTag(ClansAPI.getInstance().getClan(p.getUniqueId()).getColor(), ClansAPI.getInstance().getClan(p.getUniqueId()).getName()))).run();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player target = (Player) event.getEntity();
			if (!Bukkit.getOnlinePlayers().contains(target)) {
				return;
			}
			Player p = (Player) event.getDamager();
			if (!ClansPro.getInstance().dataManager.getMain().getConfig().getStringList("Clans.world-whitelist").contains(p.getWorld().getName())) {
				return;
			}
			PlayerPunchPlayerEvent e = new PlayerPunchPlayerEvent(p, target);
			Bukkit.getPluginManager().callEvent(e);

			if (!ClansPro.getInstance().dataManager.getMain().getConfig().getStringList("Clans.world-whitelist").contains(e.getAttacker().getWorld().getName()))
				return;
			ClanAssociate associate = ClansAPI.getInstance().getAssociate(e.getAttacker()).orElse(null);
			ClanAssociate associate2 = ClansAPI.getInstance().getAssociate(e.getVictim()).orElse(null);
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
		}

		if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile && (
				(Projectile) event.getDamager()).getShooter() instanceof Player) {
			Projectile pr = (Projectile) event.getDamager();
			Player p = (Player) pr.getShooter();
			Player target = (Player) event.getEntity();
			if (!Bukkit.getOnlinePlayers().contains(target)) {
				return;
			}
			if (!ClansPro.getInstance().dataManager.getMain().getConfig().getStringList("Clans.world-whitelist").contains(p.getWorld().getName())) {
				return;
			}
			PlayerShootPlayerEvent e = new PlayerShootPlayerEvent(p, target);
			Bukkit.getPluginManager().callEvent(e);

			ClanAssociate associate = ClansAPI.getInstance().getAssociate(e.getShooter()).orElse(null);
			ClanAssociate associate2 = ClansAPI.getInstance().getAssociate(e.getShot()).orElse(null);
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

}
