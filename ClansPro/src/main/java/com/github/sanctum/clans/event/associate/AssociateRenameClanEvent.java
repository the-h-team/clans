package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.construct.api.ClansAPI;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate attempts to rename the clan.
 */
public class AssociateRenameClanEvent extends AssociateEvent {

	private String toName;

	private final String fromName;

	public AssociateRenameClanEvent(Player changer, String fromName, String toName) {
		super(ClansAPI.getInstance().getAssociate(changer).get(), false);
		this.fromName = fromName;
		this.toName = toName;
	}

	public String getTo() {
		return toName;
	}

	public void setTo(String toName) {
		this.toName = toName;
	}

	public String getFrom() {
		return fromName;
	}

}
