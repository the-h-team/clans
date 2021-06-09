package com.github.sanctum.link.cycles;

import com.github.sanctum.borders.BorderListener;
import com.github.sanctum.borders.FlagsCommand;
import com.github.sanctum.borders.TerritoryCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.EventCycle;

public class BorderCycle extends EventCycle {

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
		return "Borders";
	}

	@Override
	public String getDescription() {
		return "A Clans [Free] ported addon, allowing users to view chunk borders.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {
		register(new BorderListener());
		register(new FlagsCommand());
		register(new TerritoryCommand());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}
}
