package com.github.sanctum.link.cycles;

import com.github.sanctum.bounty.BountyListener;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.EventCycle;

public class BountyCycle extends EventCycle {

	@Override
	public boolean persist() {
		return !EconomyProvision.getInstance().getImplementation().equals("Default | No Economy Bridge") && ClansAPI.getData().getEnabled("Addon." + getName() + ".enabled");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Bounty";
	}

	@Override
	public String getDescription() {
		return "Just like GTA, Mark bounties on players! Earn money for you or your clan.";
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
		register(new BountyListener());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}
}
