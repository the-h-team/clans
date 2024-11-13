package com.github.sanctum.clans.event.arena;

import com.github.sanctum.clans.model.Arena;
import com.github.sanctum.labyrinth.library.TimeWatch;

/**
 * Called while a clan war is waiting to start fully queued.
 */
public class ArenaStartingEvent extends ArenaEvent {

	private Status status = Status.STARTING;

	public ArenaStartingEvent(Arena arena) {
		super(arena, State.CANCELLABLE);
	}

	public void start() {
		status = Status.STARTED;
		getWar().start();
	}

	public Status getStatus() {
		return status;
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(getWar().stamp());
	}

	public enum Status {
		STARTING, STARTED
	}

}
