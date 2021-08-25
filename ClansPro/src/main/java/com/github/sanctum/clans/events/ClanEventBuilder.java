package com.github.sanctum.clans.events;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.event.custom.Vent;

/**
 * A clan event marking runtime status as explicitly synchronous
 */
public abstract class ClanEventBuilder extends Vent {

	public ClanEventBuilder() {
		super(false);
	}

	public ClanAction getUtil() {
		return Clan.ACTION;
	}

	public StringLibrary stringLibrary() {
		return getUtil();
	}


}
