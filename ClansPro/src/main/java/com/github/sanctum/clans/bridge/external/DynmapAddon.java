package com.github.sanctum.clans.bridge.external;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.external.dynmap.DynmapSubscription;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.Bukkit;

public class DynmapAddon extends ClanAddon {

	@Override
	public boolean isStaged() {
		return Bukkit.getPluginManager().isPluginEnabled("dynmap");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Dynmap";
	}

	@Override
	public String getDescription() {
		return "Allows clans to share land publicly on Dynmap renders (Renderings non-persistent).";
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
		getContext().stage(new DynmapSubscription());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}
}
