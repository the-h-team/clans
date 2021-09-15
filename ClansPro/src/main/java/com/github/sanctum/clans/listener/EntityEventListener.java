package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.construct.api.ClansAPI;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

}
