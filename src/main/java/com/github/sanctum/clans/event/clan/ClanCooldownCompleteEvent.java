package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanCooldown;
import com.github.sanctum.clans.event.ClanEvent;

/**
 * Called when a clan only cooldown expires.
 */
public class ClanCooldownCompleteEvent extends ClanEvent {

	private final Clan clan;
	private final ClanCooldown clanCooldown;

	public ClanCooldownCompleteEvent(Clan clan, ClanCooldown cooldown) {
		super(null, State.IMMUTABLE, false);
		this.clan = clan;
		this.clanCooldown = cooldown;
	}

	public ClanCooldown getCooldown() {
		return clanCooldown;
	}

	@Override
	public Clan getClan() {
		return clan;
	}
}
