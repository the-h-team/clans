package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClanJoinEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;

	private final Player joining;

	private final DefaultClan clan;

	public ClanJoinEvent(Player joining, DefaultClan clan) {
		this.joining = joining;
		this.clan = clan;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	@Override
	public StringLibrary stringLibrary() {
		return DefaultClan.action;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public Player getJoining() {
		return joining;
	}

	public DefaultClan getClan() {
		return clan;
	}

}
