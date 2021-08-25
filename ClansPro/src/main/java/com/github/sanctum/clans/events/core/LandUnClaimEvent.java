package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class LandUnClaimEvent extends ClanEventBuilder {

	private final Player remover;

	private final Claim land;

	public LandUnClaimEvent(Player remover, Claim land) {
		this.remover = remover;
		this.land = land;
	}

	public Claim getLand() {
		return land;
	}

	public Player getRemover() {
		return remover;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}


}
