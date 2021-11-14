package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.associate.AssociateEvent;

public class ClanKickAssociateEvent extends AssociateEvent {
	public ClanKickAssociateEvent(Clan.Associate associate) {
		super(associate, false);
	}
}
