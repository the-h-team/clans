package com.github.sanctum.clans.event.war;

import com.github.sanctum.clans.model.Arena;
import com.github.sanctum.labyrinth.library.TimeWatch;

/**
 * Called while a clan war is waiting to start fully queued.
 */
public class WarStartEvent extends WarEvent {

	public WarStartEvent(Arena arena) {
		super(arena, State.CANCELLABLE);
	}

	public void start() {
		getWar().start();
		setCancelled(true);
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(getWar().stamp());
	}

}
