package com.github.sanctum.clans.events;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
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
		return Clan.ACTION;
	}

	public ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

	public StringLibrary stringLibrary() {
		return getUtil();
	}


}
