package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.extra.TeleportationTarget;

/**
 * Called when a clan associate attempts to teleport to somewhere.
 */
public class AssociateTeleportEvent extends AssociateEvent {

	private TeleportationTarget target;

	public AssociateTeleportEvent(Clan.Associate associate, TeleportationTarget target) {
		super(associate, false);
		this.target = target;
	}

	public void setTarget(TeleportationTarget target) {
		if (getTarget().isPlayer() == target.isPlayer()) {
			this.target = target;
		} else throw new IllegalArgumentException("An invalid location type was provided! Both results should be the same!");
	}

	public TeleportationTarget getTarget() {
		return target;
	}
}
