package com.github.sanctum.clans.event.arena;

import com.github.sanctum.clans.model.Arena;

/**
 * Called during active clan wars.
 */
public class ArenaActiveEvent extends ArenaEvent {

	public ArenaActiveEvent(Arena arena) {
		super(arena, State.IMMUTABLE);
	}
}
