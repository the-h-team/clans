package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.events.ClanEventBuilder;

public abstract class ClanWarEvent extends ClanEventBuilder {

	private final War war;

	public ClanWarEvent(War war) {
		this.war = war;
	}

	public War getWar() {
		return war;
	}
}
