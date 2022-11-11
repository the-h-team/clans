package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.ClanEvent;

public class KingdomQuestCompletionEvent extends ClanEvent {

	private final Kingdom kingdom;
	private final Quest achievement;

	public KingdomQuestCompletionEvent(Kingdom kingdom, Quest achievement) {
		super(false);
		this.kingdom = kingdom;
		this.achievement = achievement;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}

	public Quest getQuest() {
		return achievement;
	}

	@Override
	public Clan getClan() {
		return getKingdom().getMembers().get(0);
	}
}
