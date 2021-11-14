package com.github.sanctum.clans.bridge.external;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.external.dynmap.DynmapCommand;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
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
		getContext().stage(new DynmapCommand("claim"));
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Dynmap");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fshowclaims");
			e.insert("&7|&e) &6/clan &fhideclaim");

		});

	}

	@Override
	public void onDisable() {

	}
}
