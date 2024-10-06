package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.impl.DefaultClan;
import org.bukkit.entity.Player;

/**
 * Called when a user attempts to join a clan.
 */
public class PlayerJoinClanEvent extends PlayerEvent {

	private final DefaultClan clan;

	public PlayerJoinClanEvent(Player joining, DefaultClan clan) {
		super(joining.getUniqueId(), false);
		this.clan = clan;
	}

	@Override
	public Clan getClan() {
		return clan;
	}

}
