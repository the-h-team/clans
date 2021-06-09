package com.github.sanctum.clans.construct.extra.cooldown;

import com.github.sanctum.labyrinth.library.Cooldown;
import java.util.UUID;

public class CooldownRespawn extends Cooldown {

	private final UUID id;
	private final long time;

	public CooldownRespawn(UUID player) {
		this.id = player;
		this.time = abv(5);
	}

	@Override
	public String getId() {
		return "ClansPro-war-respawn-" + id.toString();
	}

	@Override
	public long getCooldown() {
		return time;
	}
}
