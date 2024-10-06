package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Clan;

/**
 * Called when a clan associate is either promoted or demoted.
 */
public class AssociateRankManagementEvent extends AssociateEvent {

	private final Clan.Rank from;
	private Clan.Rank to;

	public AssociateRankManagementEvent(Clan.Associate associate, Clan.Rank goal) {
		super(associate, false);
		this.from = associate.getRank();
		this.to = goal;
	}

	public boolean isPromotion() {
		return from.getLevel() < to.getLevel();
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
