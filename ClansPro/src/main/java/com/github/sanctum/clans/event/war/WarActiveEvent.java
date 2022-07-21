package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.construct.api.War;

/**
 * Called during active clan wars.
 */
public class WarActiveEvent extends WarEvent {

	public WarActiveEvent(War war) {
		super(war, State.IMMUTABLE);
	}
}
