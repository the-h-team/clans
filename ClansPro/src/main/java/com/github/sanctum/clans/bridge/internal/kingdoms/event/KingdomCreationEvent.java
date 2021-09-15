package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.events.ClanEventBuilder;

public class KingdomCreationEvent extends ClanEventBuilder {

	private final Clan.Associate associate;
	private String name;

	public KingdomCreationEvent(Clan.Associate associate, String name) {
		this.associate = associate;
		this.name = name;
	}

	public String getKingdomName() {
		return this.name;
	}

	public void setKingdomName(String name) {
		this.name = name;
	}

	public Clan.Associate getAssociate() {
		return associate;
	}
}
