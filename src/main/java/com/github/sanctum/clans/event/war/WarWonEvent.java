package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.War;
import java.util.Map;

/**
 * Called when a clan war has been won.
 */
public class WarWonEvent extends WarEvent {

	private final Map.Entry<Clan, Integer> winner;
	private final Map<Clan, Integer> losers;

	public WarWonEvent(War war, Map.Entry<Clan, Integer> winner, Map<Clan, Integer> losers) {
		super(war, State.CANCELLABLE);
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
