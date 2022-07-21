package com.github.sanctum.clans.bridge.internal;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.map.MapController;
import com.github.sanctum.clans.bridge.internal.map.command.MapCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.panther.event.Vent;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

public class MapAddon extends ClanAddon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon." + getName() + ".enabled");
	}

	@Override
	public @NotNull String getName() {
		return "Map";
	}

	@Override
	public @NotNull String getDescription() {
		return "Organized area mapping using chat!";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"ms5984"};
	}

	@Override
	public void onLoad() {
		getContext().stage(new MapCommand("map"));
		getContext().stage(new MapController());
	}

	@Override
	public void onEnable() {

		getServicesManager().unregisterAll(this);

		getServicesManager().register(ClansAPI.getDataInstance().isTrue("Addon.Map.enhanced"), this, ServicePriority.High);

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Map");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}
			e.insert("&7|&e) &6/clan &fmap");
		});

	}

	@Override
	public void onDisable() {

		getServicesManager().unregisterAll(this);

		getServicesManager().register(false, this, ServicePriority.High);

	}
}
