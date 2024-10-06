package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.model.Clan;
import org.bukkit.entity.Player;

/**
 * Called when a player killers another online player
 */
public class PlayerKillPlayerEvent extends PlayerEvent {

	private final Player victim;

	private boolean keepInventory;

	private boolean clearDrops;

	public PlayerKillPlayerEvent(Player p, Player target) {
		super(p.getUniqueId(), false);
		this.victim = target;
	}

	public Player getVictim() {
		return victim;
	}

	public boolean isClearDrops() {
		return clearDrops;
	}

	public void setClearDrops(boolean clearDrops) {
		this.clearDrops = clearDrops;
	}

	public void setKeepInventory(boolean keepInventory) {
		this.keepInventory = keepInventory;
	}

	public boolean isKeepInventory() {
		return keepInventory;
	}

	@Override
	public Clan getClan() {
		return null;
	}
}
