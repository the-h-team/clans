package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.labyrinth.library.Teleport;

/**
 * Called when a clan associate attempts to teleport to somewhere.
 */
public class AssociateTeleportEvent extends AssociateEvent {

	private Teleport.Location target;

	public AssociateTeleportEvent(Clan.Associate associate, Teleport.Location target) {
		super(associate, false);
		this.target = target;
	}

	public void setTarget(Teleport.Location target) {
		if (getTarget().isPlayer() == target.isPlayer()) {
			this.target = target;
		} else throw new IllegalArgumentException("An invalid location type was provided! Both results should be the same!");
	}

	public Teleport.Location getTarget() {
		return target;
	}
}
