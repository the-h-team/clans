package com.github.sanctum.clans.util.events.command;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CommandHelpInsertEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private final List<String> helpMenu;

	public CommandHelpInsertEvent(List<String> commandArgs) {
		this.helpMenu = commandArgs;
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

	public List<String> getHelpMenu() {
		return helpMenu;
	}

	public void insert(String line) {
		helpMenu.add(stringLibrary().color(line));
	}

	public void insert(String... lines) {
		List<String> array = new ArrayList<>();
		for (String s : lines) {
			array.add(stringLibrary().color(s));
		}
		helpMenu.addAll(array);
	}


}
