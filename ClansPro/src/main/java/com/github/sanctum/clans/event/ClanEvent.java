package com.github.sanctum.clans.event;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.event.custom.Vent;

/**
 * The parent abstraction for all clan events.
 */
public abstract class ClanEvent extends Vent {

	private Clan clan;

	public ClanEvent(boolean isAsync) {
		super(isAsync);
	}

	public ClanEvent(Clan clan, boolean isAsync) {
		this(isAsync);
		this.clan = clan;
	}

	/**
	 * Get the clan involved in this event.
	 *
	 * @return the clan or null.
	 */
	public Clan getClan() {
		return clan;
	}

	public ClanAction getUtil() {
		return Clan.ACTION;
	}

	public ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

}
