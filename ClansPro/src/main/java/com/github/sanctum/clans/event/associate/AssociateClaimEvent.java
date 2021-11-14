package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.ClansAPI;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate attempts to claim land.
 */
public class AssociateClaimEvent extends AssociateEvent {

	private final Player claimer;

	public AssociateClaimEvent(Player claimer) {
		super(ClansAPI.getInstance().getAssociate(claimer).get(), false);
		this.claimer = claimer;
	}

	public Player getClaimer() {
		return claimer;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
