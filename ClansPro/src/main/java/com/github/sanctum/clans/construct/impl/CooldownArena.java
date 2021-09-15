package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.labyrinth.library.Cooldown;

public class CooldownArena extends Cooldown {

	private final long time;
	private final String id;

	public CooldownArena(String id, int seconds) {
		this.id = id;
		this.time = abv(seconds);
	}

	@Override
	public String getId() {
		return this.id;
	}


	@Override
	public long getCooldown() {
		return this.time;
	}
}
