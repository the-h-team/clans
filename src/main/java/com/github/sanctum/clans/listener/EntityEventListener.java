package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.util.ReservoirMetadata;
import com.github.sanctum.clans.construct.util.Reservoir;
import com.github.sanctum.clans.event.associate.AssociateBuildReservoirEvent;
import com.github.sanctum.clans.event.associate.AssociateHitReservoirEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import java.text.MessageFormat;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EntityEventListener implements Listener {

	static final PantherMap<Location, ReservoirMetadata> map = new PantherEntryMap<>();

	@EventHandler
	public void onSpawn(EntitySpawnEvent e) {
		if (!LabyrinthProvider.getInstance().isNew()) return;
		if (e.getEntity() instanceof EnderCrystal) {
			ReservoirMetadata data = map.get(e.getLocation());
			if (data != null) {
				data.setCrystal((EnderCrystal) e.getEntity());
				AssociateBuildReservoirEvent event = ClanVentBus.call(new AssociateBuildReservoirEvent(data));
				if (!event.isCancelled()) {
					PersistentDataContainer container = data.getEnderCrystal().getPersistentDataContainer();
					container.set(new NamespacedKey(ClansAPI.getInstance().getPlugin(), "clanspro_reservoir"), PersistentDataType.STRING, data.getAssociateWhoSpawned().getClan().getId().toString());
					Reservoir r = Reservoir.of(data.getEnderCrystal());
					r.adapt(data.getAssociateWhoSpawned().getClan());
				} else {
					e.getEntity().remove();
				}
			}
		}
	}

	@EventHandler
	public void onHurtEntity(EntityDamageByEntityEvent e) {
		Claim c = ClansAPI.getInstance().getClaimManager().getClaim(e.getEntity().getLocation());
		if (c != null) {
			if ((e.getDamager() instanceof Player || (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player))) {
				Player source = e.getDamager() instanceof Player ? (Player) e.getDamager() : (Player) (((Projectile) e.getDamager()).getShooter());
				Clan.Associate a = ClansAPI.getInstance().getAssociate(source).orElse(null);
				if (a != null) {
					if (c.getOwner().equals(a.getClan())) {
						return; // stop, theyre a member they can do what they want.
					}
				}
			}
			Claim.Flag flag = c.getFlag("invincible-animals");
			if (flag != null && flag.isEnabled()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onHurtReservoir(EntityDamageByEntityEvent e) {
		if ((e.getDamager() instanceof Player || (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player)) && e.getEntity() instanceof EnderCrystal) {
			Reservoir r = Reservoir.get(e.getEntity());
			if (r != null) {
				Player source = e.getDamager() instanceof Player ? (Player) e.getDamager() : (Player) (((Projectile) e.getDamager()).getShooter());
				Clan.Associate a = ClansAPI.getInstance().getAssociate(source).orElse(null);
				if (a != null) {
					AssociateHitReservoirEvent event = new AssociateHitReservoirEvent(a, r);
					event.setDamage(e.getDamage());
					ClanVentBus.call(event);
					if (!event.isCancelled()) {
						r.take(event.getDamage());
						source.playSound(e.getDamager().getLocation(), Sound.BLOCK_GLASS_BREAK, 10, 1);
						source.playSound(e.getDamager().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 10, 1);
						e.getEntity().getWorld().spawnParticle(Particle.FLASH, e.getEntity().getLocation().add(0, 0.5, 0), 1);
					}
				} else {
					Clan.ACTION.sendMessage(source, Clan.ACTION.notInClan());
				}
				e.setCancelled(true);
			}
		} else if (e.getEntity() instanceof EnderCrystal) {
			Reservoir r = Reservoir.get(e.getEntity());
			if (r != null) {
				r.take(e.getDamage());
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExplode(ExplosionPrimeEvent e) {
		if (e.getEntity() instanceof EnderCrystal) {
			Reservoir r = Reservoir.get(e.getEntity());
			if (r != null) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExplode(EntityExplodeEvent e) {
		Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(e.getLocation());
		if (claim != null) {
			if (claim.getFlag("no-explosives").isEnabled()) {
				((Clan) claim.getHolder()).broadcast("&6A &e" + e.getEntity().getName() + " &6went off in our claim, &a" + e.blockList().size() + " &6blocks were saved.");
				e.blockList().clear();
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		TaskScheduler.of(() -> ClansAPI.getInstance().getAssociate(e.getEntity().getUniqueId()).ifPresent(a -> {
			if (a.isEntity()) {
				a.getClan().broadcast(a.getName() + " is now dead... What a tragedy");
				a.getClan().broadcast(MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("member-leave"), a.getName()));
				TaskScheduler.of(() -> InvasiveEntity.removeNonAssociated(a, true)).schedule().next(() -> a.getClan().remove(a)).schedule();
			}
		})).scheduleAsync();
	}

}
