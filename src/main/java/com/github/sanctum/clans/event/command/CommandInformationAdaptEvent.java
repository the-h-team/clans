package com.github.sanctum.clans.event.command;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import java.util.ArrayList;
import java.util.List;

/**
 * Called whenever the clan command help menu is retrieved.
 */
public class CommandInformationAdaptEvent extends ClanEvent {

	private final List<String> helpMenu;

	public CommandInformationAdaptEvent(List<String> commandArgs) {
		super(false);
		this.helpMenu = commandArgs;
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public List<String> getMenu() {
		return helpMenu;
	}

	public void insert(String line) {
		helpMenu.add(new FormattedString(line).replace("/c", "{label}").get());
	}

	public void insert(String... lines) {
		List<String> array = new ArrayList<>();
		for (String s : lines) {
			array.add(new FormattedString(s).replace("/c", "{label}").get());
		}
		helpMenu.addAll(array);
	}


}
