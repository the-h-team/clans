package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.events.ClanEventBuilder;

public class ClanLeaveEvent extends ClanEventBuilder {

	private final Clan.Associate associate;

	public ClanLeaveEvent(Clan.Associate associate) {
		this.associate = associate;
	}

	public Clan.Associate getAssociate() {
		return associate;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
