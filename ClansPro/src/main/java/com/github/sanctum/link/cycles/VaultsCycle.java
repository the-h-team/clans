package com.github.sanctum.link.cycles;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.vaults.VaultsCommand;
import com.github.sanctum.vaults.VaultsListener;

public class VaultsCycle extends EventCycle {

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
		return "Vaults";
	}

	@Override
	public String getDescription() {
		return "An addon that grants public clan storage usage.";
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
		register(new VaultsListener());
		register(new VaultsCommand());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}

}
