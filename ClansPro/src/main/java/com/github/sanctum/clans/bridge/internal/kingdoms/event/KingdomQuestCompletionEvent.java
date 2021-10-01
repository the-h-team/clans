package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.events.ClanEventBuilder;

public class KingdomQuestCompletionEvent extends ClanEventBuilder {

	private final Kingdom kingdom;
	private final Quest achievement;

	public KingdomQuestCompletionEvent(Kingdom kingdom, Quest achievement) {
		this.kingdom = kingdom;
		this.achievement = achievement;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}

	public Quest getQuest() {
		return achievement;
	}
}
