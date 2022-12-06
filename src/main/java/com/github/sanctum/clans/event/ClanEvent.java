package com.github.sanctum.clans.event;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.panther.event.Vent;
import org.jetbrains.annotations.NotNull;

/**
 * The parent abstraction for all clan events.
 */
public abstract class ClanEvent extends Vent {

	private final Clan clan;
	private final String name;

	public ClanEvent(boolean isAsync) {
		super((Host) ClansAPI.getInstance().getPlugin(), isAsync);
		this.clan = null;
		this.name = null;
	}

	public ClanEvent(Clan clan, boolean isAsync) {
		super((Host) ClansAPI.getInstance().getPlugin(), isAsync);
		this.clan = clan;
		this.name = clan.getName();
	}

	public ClanEvent(Clan clan, @NotNull State state, boolean isAsync) {
		super((Host) ClansAPI.getInstance().getPlugin(), state, isAsync);
		this.clan = clan;
		if (clan != null) {
			this.name = clan.getName();
		} else this.name = null;
	}

	/**
	 * Get the clan involved in this event.
	 *
	 * @return the clan or null.
	 */
	public Clan getClan() {
		return clan;
	}

	/**
	 * @return the name of this clan or null if no clan is present.
	 */
	public String getName() {
		return this.name;
	}

	public ClanAction getUtil() {
		return Clan.ACTION;
	}

	public ClansAPI getApi() {
		return ClansAPI.getInstance();
	}

}
