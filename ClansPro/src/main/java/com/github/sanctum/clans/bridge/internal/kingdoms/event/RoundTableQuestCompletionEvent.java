package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Quest;
import com.github.sanctum.clans.bridge.internal.kingdoms.RoundTable;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.ClanEvent;

public class RoundTableQuestCompletionEvent extends ClanEvent {

	private final RoundTable table;
	private final Quest achievement;

	public RoundTableQuestCompletionEvent(RoundTable table, Quest achievement) {
		super(false);
		this.table = table;
		this.achievement = achievement;
	}

	public RoundTable getTable() {
		return table;
	}

	public Quest getQuest() {
		return achievement;
	}

	@Override
	public Clan getClan() {
		return null;
	}
}
