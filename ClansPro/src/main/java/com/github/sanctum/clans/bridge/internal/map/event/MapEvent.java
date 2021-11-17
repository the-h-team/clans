package com.github.sanctum.clans.bridge.internal.map.event;

import com.github.sanctum.clans.event.player.PlayerEvent;
import java.util.UUID;

public abstract class MapEvent extends PlayerEvent {


	public MapEvent(UUID player, boolean isAsync) {
		super(player, isAsync);
	}

}
