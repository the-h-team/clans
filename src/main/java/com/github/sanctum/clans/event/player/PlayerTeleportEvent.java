package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.construct.extra.TeleportationTarget;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate attempts to teleport to somewhere.
 */
public class PlayerTeleportEvent extends PlayerEvent {

	private TeleportationTarget target;

	public PlayerTeleportEvent(Player player, TeleportationTarget target) {
		super(player.getUniqueId(), false);
		this.target = target;
	}

	public void setTarget(TeleportationTarget target) {
		if (getTarget().isPlayer() == target.isPlayer()) {
			this.target = target;
		} else
			throw new IllegalArgumentException("An invalid location type was provided! Both results should be the same!");
	}

	public TeleportationTarget getTarget() {
		return target;
	}
}
