package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.model.Clan;
import org.bukkit.entity.Player;

/**
 * Called when a player punches another online player
 */
public class PlayerPunchPlayerEvent extends PlayerEvent {

	private final Player victim;

	private boolean cancelled;

	public PlayerPunchPlayerEvent(Player p, Player target) {
		super(p.getUniqueId(), false);
		this.victim = target;
	}

	public Player getVictim() {
		return victim;
	}

	public boolean canHurt() {
		return cancelled;
	}

	public void setCanHurt(boolean b) {
		if (b)
			this.cancelled = false;
		if (!b)
			this.cancelled = true;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		setCanHurt(cancelled);
	}

	@Override
	public Clan getClan() {
		return null;
	}

}
