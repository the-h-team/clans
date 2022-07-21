package com.github.sanctum.clans.bridge.internal.kingdoms.event;

import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.associate.AssociateEvent;
import org.bukkit.entity.Player;

public class KingdomCreatedEvent extends AssociateEvent {

	private final Player player;
	private final Kingdom kingdom;

	public KingdomCreatedEvent(Player player, Kingdom kingdom) {
		super(ClansAPI.getInstance().getAssociate(player).get(), player.getUniqueId(), State.IMMUTABLE, false);
		this.player = player;
		this.kingdom = kingdom;
	}

	public Player getPlayer() {
		return player;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}
}
