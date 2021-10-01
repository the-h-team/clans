package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.bridge.internal.kingdoms.RoundTable;
import com.github.sanctum.clans.events.ClanEventBuilder;

public class RoundTableQuestCompletionEvent extends ClanEventBuilder {

	private final RoundTable table;
	private final Quest achievement;

	public RoundTableQuestCompletionEvent(RoundTable table, Quest achievement) {
		this.table = table;
		this.achievement = achievement;
	}

	public RoundTable getTable() {
		return table;
	}

	public Quest getQuest() {
		return achievement;
	}
}
