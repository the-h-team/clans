package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.associate.AssociateEvent;

/**
 * Called when a clan removes an associate
 */
public class ClanKickAssociateEvent extends AssociateEvent {
	public ClanKickAssociateEvent(Clan.Associate associate) {
		super(associate, false);
	}
}
