package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.event.ClanEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The parent abstraction for all clan war related events.
 */
public abstract class WarEvent extends ClanEvent {

	private final War war;

	public WarEvent(War war, @NotNull State state) {
		super(null, state, false);
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
