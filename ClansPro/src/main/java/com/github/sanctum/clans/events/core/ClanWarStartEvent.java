package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.labyrinth.library.TimeWatch;

public class ClanWarStartEvent extends ClanWarEvent {

	public ClanWarStartEvent(War war) {
		super(war);
	}

	public void start() {
		getWar().start();
		setCancelled(true);
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(getWar().stamp());
	}

}
