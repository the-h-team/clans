package com.github.sanctum.clans.events.command;

import com.github.sanctum.clans.events.ClanEventBuilder;
import java.util.ArrayList;
import java.util.List;

public class CommandHelpInsertEvent extends ClanEventBuilder {

	private final List<String> helpMenu;

	public CommandHelpInsertEvent(List<String> commandArgs) {
		this.helpMenu = commandArgs;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
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
