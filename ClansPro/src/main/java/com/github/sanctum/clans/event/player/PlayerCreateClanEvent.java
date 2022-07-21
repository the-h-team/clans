package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.construct.api.Clan;
import java.util.UUID;

/**
 * Called when a user attempts to create a clan.
 */
public class PlayerCreateClanEvent extends PlayerEvent {

	private final UUID owner;

	private final String name;

	private final String password;

	public PlayerCreateClanEvent(UUID owner, String name, String password) {
		super(owner, false);
		this.owner = owner;
		this.name = name;
		this.password = password;
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

}
