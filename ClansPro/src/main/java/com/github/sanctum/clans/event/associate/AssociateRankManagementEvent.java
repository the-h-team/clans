package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.Clan;

/**
 * Called when a clan associate is either promoted or demoted.
 */
public class AssociateRankManagementEvent extends AssociateEvent {

	private final Clan.Rank from;
	private Clan.Rank to;

	public AssociateRankManagementEvent(Clan.Associate associate, Clan.Rank goal) {
		super(associate, false);
		this.from = associate.getPriority();
		this.to = goal;
	}

	public boolean isPromotion() {
		return from.toLevel() < to.toLevel();
	}

	public void setTo(Clan.Rank to) {
		this.to = to;
	}

	public Clan.Rank getTo() {
		return to;
	}

	public Clan.Rank getFrom() {
		return from;
	}
}
