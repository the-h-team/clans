package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.associate.AssociateEvent;
import java.util.UUID;

/**
 * Called when a user has successfully formed a new clan.
 */
public class ClanFreshlyFormedEvent extends AssociateEvent {

	private final String name;

	public ClanFreshlyFormedEvent(UUID owner, String name) {
		super(ClansAPI.getInstance().getAssociate(owner).get(), false);
		this.name = name;
	}

	@Override
	public Clan getClan() {
		return getApi().getClanManager().getClan(getApi().getClanManager().getClanID(name));
	}

}
