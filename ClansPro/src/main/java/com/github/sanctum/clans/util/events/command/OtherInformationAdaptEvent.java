package com.github.sanctum.clans.util.events.command;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OtherInformationAdaptEvent extends ClanEventBuilder {

	private static final HandlerList handlers = new HandlerList();

	private final List<String> info;

	private final String clanID;

	public OtherInformationAdaptEvent(List<String> commandArgs, String clanID) {
		this.info = commandArgs;
		this.clanID = clanID;
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
