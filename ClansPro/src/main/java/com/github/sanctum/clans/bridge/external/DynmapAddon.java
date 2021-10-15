package com.github.sanctum.clans.bridge.external;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.external.dynmap.DynmapSubscription;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class DynmapAddon extends ClanAddon {

	@Override
	public boolean isPersistent() {
		return Bukkit.getPluginManager().isPluginEnabled("dynmap");
	}

	@Override
	public @NotNull String getName() {
		return "Dynmap";
	}

	@Override
	public @NotNull String getDescription() {
		return "Allows clans to share land publicly on Dynmap renders (Renderings non-persistent).";
	}

	@Override
	public @NotNull String getVersion() {
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
