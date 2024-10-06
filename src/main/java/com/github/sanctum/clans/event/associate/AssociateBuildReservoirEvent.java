package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.util.ReservoirMetadata;

/**
 * Called when a clan associate attempts to build a clan reservoir.
 */
public class AssociateBuildReservoirEvent extends AssociateEvent {

	final ReservoirMetadata data;

	public AssociateBuildReservoirEvent(ReservoirMetadata data) {
		super(data.getAssociateWhoSpawned(), false);
		this.data = data;
	}

	public ReservoirMetadata getData() {
		return data;
	}
}
