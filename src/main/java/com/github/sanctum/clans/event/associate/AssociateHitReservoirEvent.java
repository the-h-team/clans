package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.util.Reservoir;

public class AssociateHitReservoirEvent extends AssociateEvent {
	final Reservoir reservoir;
	double damage;

	public AssociateHitReservoirEvent(Clan.Associate associate, Reservoir reservoir) {
		super(associate, false);
		this.reservoir = reservoir;
	}

	public Reservoir getReservoir() {
		return reservoir;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
}
