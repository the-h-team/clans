package com.github.sanctum.clans.events.damage;

import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class PlayerShootPlayerEvent extends ClanEventBuilder {

	private final Player shooter;

	private final Player shot;

	private boolean cancelled;

	public PlayerShootPlayerEvent(Player p, Player target) {
		this.shooter = p;
		this.shot = target;
	}

	public Player getShooter() {
		return shooter;
	}

	public Player getShot() {
		return shot;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
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
}
