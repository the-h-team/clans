package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.extra.EnderCrystalData;

/**
 * Called when a clan associate attempts to build a clan reservoir.
 */
public class AssociateBuildReservoirEvent extends AssociateEvent {

	final EnderCrystalData data;

	public AssociateBuildReservoirEvent(EnderCrystalData data) {
		super(data.getAssociate(), false);
		this.data = data;
	}

	public EnderCrystalData getData() {
		return data;
	}
}
