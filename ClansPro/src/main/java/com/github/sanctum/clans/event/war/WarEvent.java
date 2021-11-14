package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.event.ClanEvent;

/**
 * The parent abstraction for all clan war related events.
 */
public abstract class WarEvent extends ClanEvent {

	private final War war;

	public WarEvent(War war) {
		super(false);
		this.war = war;
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public War getWar() {
		return war;
	}
}
