package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class KingdomCreatedEvent extends ClanEventBuilder {

	private final Player player;
	private final Kingdom kingdom;

	public KingdomCreatedEvent(Player player, Kingdom kingdom) {
		this.player = player;
		this.kingdom = kingdom;
		setState(CancelState.OFF);
	}

	public Player getPlayer() {
		return player;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}
}
