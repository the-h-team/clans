package com.github.sanctum.clans.events.command;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class CommandInsertEvent extends ClanEventBuilder {

	private final Player sender;

	private final String[] args;

	private boolean isCommand;

	public CommandInsertEvent(Player sender, String[] args) {
		this.sender = sender;
		this.args = args;
	}

	@Override
	public ClanAction getUtil() {
		return Clan.ACTION;
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


	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
