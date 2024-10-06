package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.model.Arena;

/**
 * Called during active clan wars.
 */
public class WarActiveEvent extends WarEvent {

	public WarActiveEvent(Arena arena) {
		super(arena, State.IMMUTABLE);
	}
}
