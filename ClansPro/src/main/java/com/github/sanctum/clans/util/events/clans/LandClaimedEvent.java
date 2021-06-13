package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class LandClaimedEvent extends ClanEventBuilder {

	private final Player claimer;

	private final Claim land;

	public LandClaimedEvent(Player claimer, Claim land) {
		this.claimer = claimer;
		this.land = land;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public Claim getLand() {
		return land;
	}

	public Player getClaimer() {
		return claimer;
	}

}
