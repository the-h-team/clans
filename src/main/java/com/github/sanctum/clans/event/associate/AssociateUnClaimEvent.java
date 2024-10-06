package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.event.claim.ClaimEvent;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate attempts to un-claim clan land.
 */
public class AssociateUnClaimEvent extends ClaimEvent {

	private final Player remover;

	public AssociateUnClaimEvent(Player remover, Claim land) {
		super(ClansAPI.getInstance().getAssociate(remover).get(), land);
		this.remover = remover;
	}

	public Player getRemover() {
		return remover;
	}

}
