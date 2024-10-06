package com.github.sanctum.clans.model.addon.kingdoms.event;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.associate.AssociateEvent;

public class KingdomCreationEvent extends AssociateEvent {

	private String name;

	public KingdomCreationEvent(Clan.Associate associate, String name) {
		super(associate, false);
		this.name = name;
	}

	public String getKingdomName() {
		return this.name;
	}

	public void setKingdomName(String name) {
		this.name = name;
	}

}
