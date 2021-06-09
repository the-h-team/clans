package com.github.sanctum.link.cycles;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.stashes.StashCommand;
import com.github.sanctum.stashes.StashListener;

public class StashesCycle extends EventCycle {

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
		return "Stashes";
	}

	@Override
	public String getDescription() {
		return "An addon that compliments w/ a limited private storage space.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest", "ms5984"};
	}

	@Override
	public void onLoad() {
		register(new StashListener());
		register(new StashCommand());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}

}
