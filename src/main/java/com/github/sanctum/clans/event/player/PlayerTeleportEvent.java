package com.github.sanctum.clans.event.player;

import com.github.sanctum.labyrinth.library.Teleport;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate attempts to teleport to somewhere.
 */
public class PlayerTeleportEvent extends PlayerEvent {

	private Teleport.Location target;

	public PlayerTeleportEvent(Player player, Teleport.Location target) {
		super(player.getUniqueId(), false);
		this.target = target;
	}

	public void setTarget(Teleport.Location target) {
		if (getTarget().isPlayer() == target.isPlayer()) {
			this.target = target;
		} else
			throw new IllegalArgumentException("An invalid location type was provided! Both results should be the same!");
	}

	public Teleport.Location getTarget() {
		return target;
	}
}
