package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.claim.ClaimEvent;

/**
 * Called when a clan attempts to forcefully remove another clans claim.
 */
public class ClanOverpowerClanEvent extends ClaimEvent {

	private final Clan a;

	public ClanOverpowerClanEvent(Clan.Associate aggressor, Claim claim) {
		super(aggressor, claim);
		this.a = aggressor.getClan();
	}

	@Override
	public Clan getClan() {
		return this.a;
	}

	public Clan getVictim() {
		return ((Clan)getClaim().getHolder());
	}
}
