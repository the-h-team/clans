package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.event.ClanEvent;

public class ClanCooldownCompleteEvent extends ClanEvent {

	private final Clan clan;
	private final ClanCooldown clanCooldown;

	public ClanCooldownCompleteEvent(Clan clan, ClanCooldown cooldown) {
		super(false);
		this.clan = clan;
		this.clanCooldown = cooldown;
		setState(CancelState.OFF);
	}

	public ClanCooldown getCooldown() {
		return clanCooldown;
	}

	@Override
	public Clan getClan() {
		return clan;
	}
}
