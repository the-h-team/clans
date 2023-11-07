package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.labyrinth.library.Cooldown;
import java.util.UUID;

public class DefaultRespawnCooldown extends Cooldown {

	private final UUID id;
	private final long time;

	public DefaultRespawnCooldown(UUID player) {
		this.id = player;
		this.time = abv(5);
	}

	@Override
	public String getId() {
		return "ClansPro-war-respawn-" + id.toString();
		// ^FIXME change to start with "Clans"
		// -not doing change right now so i don't break anything
	}

	@Override
	public long getCooldown() {
		return time;
	}
}
