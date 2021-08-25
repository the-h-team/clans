package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class LandPreClaimEvent extends ClanEventBuilder {

	private final Player claimer;

	public LandPreClaimEvent(Player claimer) {
		this.claimer = claimer;
	}

	public Player getClaimer() {
		return claimer;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
