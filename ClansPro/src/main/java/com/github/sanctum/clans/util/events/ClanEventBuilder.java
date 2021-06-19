package com.github.sanctum.clans.util.events;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.event.custom.Vent;

/**
 * A clan event marking runtime status as explicitly synchronous
 */
public abstract class ClanEventBuilder extends Vent {

	public ClanEventBuilder() {
		super(false);
	}

	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	public StringLibrary stringLibrary() {
		return getUtil();
	}


}
