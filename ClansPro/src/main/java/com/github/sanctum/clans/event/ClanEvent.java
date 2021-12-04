package com.github.sanctum.clans.event;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.event.custom.Vent;

/**
 * The parent abstraction for all clan events.
 */
public abstract class ClanEvent extends Vent {

	private final Clan clan;
	private final String name;

	public ClanEvent(boolean isAsync) {
		super(isAsync);
		this.clan = null;
		this.name = null;
	}

	public ClanEvent(Clan clan, boolean isAsync) {
		super(isAsync);
		this.clan = clan;
		this.name = clan.getName();
	}

	/**
	 * Get the clan involved in this event.
	 *
	 * @return the clan or null.
	 */
	public Clan getClan() {
		return clan;
	}

	public String getClanName() {
		return this.name;
	}

	public ClanAction getUtil() {
		return Clan.ACTION;
	}

	public ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

}
