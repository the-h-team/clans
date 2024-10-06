package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Clan;

/**
 * Called when a clan associate attempts to leave their clan.
 */
public class AssociateQuitEvent extends AssociateEvent {

	public AssociateQuitEvent(Clan.Associate associate) {
		super(associate, false);
	}

}
