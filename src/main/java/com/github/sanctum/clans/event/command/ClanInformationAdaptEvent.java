package com.github.sanctum.clans.event.command;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.panther.util.HUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Called whenever standard format clan information is retrieved.
 */
public class ClanInformationAdaptEvent extends ClanEvent {

	private final List<String> info;
	private final Type type;
	private final String clanID;

	public ClanInformationAdaptEvent(List<String> commandArgs, String clanID, Type type) {
		super(false);
		this.type = type;
		this.info = commandArgs;
		this.clanID = clanID;
	}

	public Type getType() {
		return type;
	}

	@Override
	public Clan getClan() {
		return ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(clanID));
	}

	public List<String> getInsertions() {
		return info;
	}

	public void insert(String line) {
		info.add(getUtil().color(line));
	}

	public void insert(String... lines) {
		List<String> array = new ArrayList<>();
		for (String s : lines) {
			array.add(getUtil().color(s));
		}
		info.addAll(array);
	}

	public enum Type {
		PERSONAL, OTHER
	}


}
