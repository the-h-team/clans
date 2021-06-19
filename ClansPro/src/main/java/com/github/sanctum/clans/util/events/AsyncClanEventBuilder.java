package com.github.sanctum.clans.util.events;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.event.custom.Vent;

/**
 * A clan event marking runtime status as explicitly asynchronous
 */
public abstract class AsyncClanEventBuilder extends Vent {


	protected AsyncClanEventBuilder() {
		this(true);
	}

	protected AsyncClanEventBuilder(boolean isAsync) {
		super(isAsync);
	}

	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	public StringLibrary stringLibrary() {
		return getUtil();
	}


}
