package com.github.sanctum.link.dynmap;

import com.github.sanctum.dynmap.DynmapListener;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.EventCycle;
import java.util.Collection;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class DynmapCycle extends EventCycle {

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
		additions.add(new DynmapListener());
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}
}
