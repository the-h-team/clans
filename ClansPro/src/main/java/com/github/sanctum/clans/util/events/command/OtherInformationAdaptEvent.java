package com.github.sanctum.clans.util.events.command;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.ArrayList;
import java.util.List;

public class OtherInformationAdaptEvent extends ClanEventBuilder {

	private final List<String> info;

	private final String clanID;

	public OtherInformationAdaptEvent(List<String> commandArgs, String clanID) {
		this.info = commandArgs;
		this.clanID = clanID;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public List<String> getInsertions() {
		return info;
	}

	public Clan getClan() {
		return DefaultClan.action.getClan(clanID);
	}

	public void insert(String line) {
		info.add(stringLibrary().color(line));
	}

	public void insert(String... lines) {
		List<String> array = new ArrayList<>();
		for (String s : lines) {
			array.add(stringLibrary().color(s));
		}
		info.addAll(array);
	}


}
