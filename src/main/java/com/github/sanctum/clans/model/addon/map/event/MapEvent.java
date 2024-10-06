package com.github.sanctum.clans.model.addon.map.event;

import com.github.sanctum.clans.event.player.PlayerEvent;
import java.util.UUID;

public abstract class MapEvent extends PlayerEvent {


	public MapEvent(UUID player, boolean isAsync) {
		super(player, isAsync);
	}

}
