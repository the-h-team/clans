package com.github.sanctum.clans.event.arena;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.Arena;
import java.util.Map;

/**
 * Called when a clan war has been won.
 */
public class ArenaWonEvent extends ArenaEvent {

	private final Map.Entry<Clan, Integer> winner;
	private final Map<Clan, Integer> losers;

	public ArenaWonEvent(Arena arena, Map.Entry<Clan, Integer> winner, Map<Clan, Integer> losers) {
		super(arena, State.CANCELLABLE);
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
