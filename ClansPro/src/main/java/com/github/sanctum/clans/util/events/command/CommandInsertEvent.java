package com.github.sanctum.clans.util.events.command;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class CommandInsertEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private final Player sender;

	private final String[] args;

	private boolean isCommand;

	public CommandInsertEvent(Player sender, String[] args) {
		this.sender = sender;
		this.args = args;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
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

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public void setReturn(boolean b) {
		this.isCommand = b;
	}

	public boolean isCommand() {
		return isCommand;
	}

	public String[] getArgs() {
		return args;
	}

	public Player getSender() {
		return sender;
	}


}
