package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;
import org.bukkit.entity.Tameable;

/**
 * Called when a clan associate is created from a tamable entity.
 */
public class AssociateFromAnimalEvent extends AssociateEvent {
	public AssociateFromAnimalEvent(Clan.Associate associate) {
		super(associate, false);
		setState(CancelState.OFF);
	}

	public Tameable getEntity() {
		return (Tameable) getAssociate().getAsEntity();
	}

}
