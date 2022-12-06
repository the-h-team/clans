package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.claim.ClaimEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate has successfully claimed new land.
 */
public class AssociateLoseLandEvent extends ClaimEvent {

	private final Player claimer;
	private final Chunk chunk;

	public AssociateLoseLandEvent(Chunk chunk) {
		super(dummy, null);
		this.claimer = null;
		this.chunk = chunk;
	}

	public AssociateLoseLandEvent(Player claimer, Chunk chunk) {
		super(ClansAPI.getInstance().getAssociate(claimer).get(), null);
		this.claimer = claimer;
		this.chunk = chunk;
	}

	@Override
	public Player getPlayer() {
		return claimer;
	}

	@Override
	public Clan getClan() {
		return ((Clan) getClaim().getHolder());
	}

	public Chunk getChunk() {
		return chunk;
	}
}
