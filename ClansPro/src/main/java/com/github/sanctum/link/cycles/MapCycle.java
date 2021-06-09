package com.github.sanctum.link.cycles;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.map.MapCommand;

public class MapCycle extends EventCycle {

	@Override
	public boolean persist() {
		return ClansAPI.getData().getEnabled("Addon." + getName() + ".enabled");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Map";
	}

	@Override
	public String getDescription() {
		return "Organized area mapping using chat!";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"ms5984"};
	}

	@Override
	public void onLoad() {
		register(new MapCommand());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}
}
