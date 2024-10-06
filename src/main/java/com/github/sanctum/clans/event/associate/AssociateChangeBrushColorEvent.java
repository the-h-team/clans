package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.Clan;

/**
 * Called when a clan associate is editing their insignia and they're changing colors.
 */
public class AssociateChangeBrushColorEvent extends AssociateEvent {

	private String color;

	public AssociateChangeBrushColorEvent(Clan.Associate associate, String color) {
		super(associate, false);
		this.color = color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}
}
