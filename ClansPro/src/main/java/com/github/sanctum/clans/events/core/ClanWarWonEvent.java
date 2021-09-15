package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.War;
import java.util.Map;

public class ClanWarWonEvent extends ClanWarEvent {

	private final Map.Entry<Clan, Integer> winner;
	private final Map<Clan, Integer> losers;

	public ClanWarWonEvent(War war, Map.Entry<Clan, Integer> winner, Map<Clan, Integer> losers) {
		super(war);
		this.winner = winner;
		this.losers = losers;
	}

	public Map.Entry<Clan, Integer> getWinner() {
		return winner;
	}

	public Map<Clan, Integer> getLosers() {
		return losers;
	}
}
