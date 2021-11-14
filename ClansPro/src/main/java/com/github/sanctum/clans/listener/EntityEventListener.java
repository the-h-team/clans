package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.labyrinth.task.Schedule;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
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

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		Schedule.async(() -> ClansAPI.getInstance().getAssociate(e.getEntity().getUniqueId()).ifPresent(a -> {
			if (a.isEntity()) {
				a.getClan().broadcast(a.getName() + " is now dead... What a tragedy");
				Schedule.sync(() -> {
					InvasiveEntity.removeNonAssociated(a, true);
					a.getClan().remove(a);
				}).run();
			}
		})).run();
	}

}
