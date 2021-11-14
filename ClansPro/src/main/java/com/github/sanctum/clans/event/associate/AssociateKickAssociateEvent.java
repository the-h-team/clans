package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;

/**
 * Called when a clan associate successfully attempts to kick another associate from the clan.
 */
public class AssociateKickAssociateEvent extends AssociateEvent {

	private final Clan.Associate associate;

	public AssociateKickAssociateEvent(Clan.Associate associate, Clan.Associate kicker) {
		super(associate, false);
		this.associate = kicker;
	}

	public Clan.Associate getKicker() {
		return associate;
	}
}
