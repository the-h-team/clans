package com.github.sanctum.clans.event.command;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.ClanEvent;
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
		helpMenu.add(getUtil().color(line));
	}

	public void insert(String... lines) {
		List<String> array = new ArrayList<>();
		for (String s : lines) {
			array.add(getUtil().color(s));
		}
		helpMenu.addAll(array);
	}


}
