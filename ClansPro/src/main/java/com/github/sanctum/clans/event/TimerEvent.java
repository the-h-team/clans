package com.github.sanctum.clans.event;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.player.PlayerEvent;
import com.github.sanctum.panther.event.Vent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * A both asynchronous & synchronous concurrent timer abstraction.
 *
 * <pre>
 * (Manual checks required via {@link Vent#isAsynchronous()}!)
 *
 * The ability to listen to this event should be possible at all times!
 * The sole purpose for this abstraction is to run in the background on another thread.
 */
public abstract class TimerEvent extends PlayerEvent {

	private final Player cached;

	public TimerEvent(Player target, boolean isAsync) {
		super(target.getUniqueId(), isAsync);
		this.cached = target;
	}

	@Override
	public OfflinePlayer getOfflinePlayer() {
		return cached;
	}

	@Override
	public Player getPlayer() {
		return cached;
	}

	@Override
	public Clan getClan() {
		return null;
	}
}
