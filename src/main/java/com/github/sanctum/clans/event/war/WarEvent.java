package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.model.Arena;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.ClanEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The parent abstraction for all clan war related events.
 */
public abstract class WarEvent extends ClanEvent {

	private final Arena arena;

	public WarEvent(Arena arena, @NotNull State state) {
		super(null, state, false);
		this.arena = arena;
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public Arena getWar() {
		return arena;
	}
}
