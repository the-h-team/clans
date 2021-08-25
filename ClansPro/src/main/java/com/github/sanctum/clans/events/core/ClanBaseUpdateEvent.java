package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClanBaseUpdateEvent extends ClanEventBuilder {

	private final Player player;

	private final Location location;

	public ClanBaseUpdateEvent(Player player, Location location) {
		this.player = player;
		this.location = location;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
