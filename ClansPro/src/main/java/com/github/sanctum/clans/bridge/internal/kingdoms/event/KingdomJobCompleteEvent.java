package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.clans.events.ClanEventBuilder;

public class KingdomJobCompleteEvent extends ClanEventBuilder {

	private final Kingdom kingdom;
	private final KingdomAchievement achievement;

	public KingdomJobCompleteEvent(Kingdom kingdom, KingdomAchievement achievement) {
		this.kingdom = kingdom;
		this.achievement = achievement;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}

	public KingdomAchievement getAchievement() {
		return achievement;
	}
}
