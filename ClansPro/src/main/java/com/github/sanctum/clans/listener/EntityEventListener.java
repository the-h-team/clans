package com.github.sanctum.clans.listener;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.text.MessageFormat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityEventListener implements Listener {

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
