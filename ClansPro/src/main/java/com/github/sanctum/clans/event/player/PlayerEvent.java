package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.event.ClanEvent;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * The parent abstraction for all player related events.
 */
public abstract class PlayerEvent extends ClanEvent {

	private final UUID player;

	public PlayerEvent(UUID player, boolean isAsync) {
		super(isAsync);
		this.player = player;
	}

	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(player);
	}

	public Player getPlayer() {
		return getOfflinePlayer().getPlayer();
	}
}
