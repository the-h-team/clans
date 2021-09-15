package com.github.sanctum.clans.bridge.external;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.external.dynmap.DynmapSubscription;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Collection;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class DynmapAddon extends ClanAddon {

	private final Collection<Listener> additions = new HashSet<>();

	@Override
	public boolean persist() {
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
	public Collection<Listener> getAdditions() {
		return additions;
	}

	@Override
	public void onLoad() {
		additions.add(new DynmapSubscription());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}
}
