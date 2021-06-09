package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClanCreatedEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;

	private final UUID owner;

	private final String name;

	public ClanCreatedEvent(UUID owner, String name) {
		this.owner = owner;
		this.name = name;
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

	public OfflinePlayer getMaker() {
		return Bukkit.getOfflinePlayer(owner);
	}

	public DefaultClan getClan() {
		return (DefaultClan) DefaultClan.action.getClan(ClansAPI.getInstance().getClanID(name));
	}

}
