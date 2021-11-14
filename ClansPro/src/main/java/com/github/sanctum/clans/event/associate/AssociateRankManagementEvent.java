package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.RankPriority;
import com.github.sanctum.clans.construct.api.Clan;

/**
 * Called when a clan associate is either promoted or demoted.
 */
public class AssociateRankManagementEvent extends AssociateEvent {

	private final RankPriority from;
	private RankPriority to;

	public AssociateRankManagementEvent(Clan.Associate associate, RankPriority goal) {
		super(associate, false);
		this.from = associate.getPriority();
		this.to = goal;
	}

	public boolean isPromotion() {
		return from.toInt() < to.toInt();
	}

	public void setTo(RankPriority to) {
		this.to = to;
	}

	public RankPriority getTo() {
		return to;
	}

	public RankPriority getFrom() {
		return from;
	}
}
