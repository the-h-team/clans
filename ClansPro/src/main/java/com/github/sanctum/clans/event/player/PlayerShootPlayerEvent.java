package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.construct.api.Clan;
import org.bukkit.entity.Player;

/**
 * Called when a player shoots another online player
 */
public class PlayerShootPlayerEvent extends PlayerEvent {

	private final Player shot;

	private boolean cancelled;

	public PlayerShootPlayerEvent(Player p, Player target) {
		super(p.getUniqueId(), false);
		this.shot = target;
	}

	public Player getShot() {
		return shot;
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public boolean canHurt() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		setCanHurt(cancelled);
	}

	public void setCanHurt(boolean b) {
		if (b)
			this.cancelled = false;
		if (!b)
			this.cancelled = true;
	}
}
