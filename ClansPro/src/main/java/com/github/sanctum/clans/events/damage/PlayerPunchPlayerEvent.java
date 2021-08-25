package com.github.sanctum.clans.events.damage;

import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class PlayerPunchPlayerEvent extends ClanEventBuilder {

	private final Player attacker;

	private final Player victim;

	private boolean cancelled;

	public PlayerPunchPlayerEvent(Player p, Player target) {
		this.attacker = p;
		this.victim = target;
	}

	public Player getAttacker() {
		return attacker;
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
	public String getName() {
		return getClass().getSimpleName();
	}

}
