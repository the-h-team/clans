package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LandClaimedEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private final Player claimer;

	private final Claim land;

	public LandClaimedEvent(Player claimer, Claim land) {
		this.claimer = claimer;
		this.land = land;
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

	public Claim getLand() {
		return land;
	}

	public Player getClaimer() {
		return claimer;
	}

}
