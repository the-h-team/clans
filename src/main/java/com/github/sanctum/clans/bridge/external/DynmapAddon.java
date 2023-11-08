package com.github.sanctum.clans.bridge.external;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQueue;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.external.dynmap.DynmapClanMarkerSet;
import com.github.sanctum.clans.bridge.external.dynmap.DynmapCommand;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.clans.event.associate.AssociateUnClaimEvent;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class DynmapAddon extends ClanAddon {

	DynmapClanMarkerSet integration;

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
		return "Allows clans to temporarily display owned claims publicly.";
	}

	@Override
	public @NotNull String getVersion() {
		return "2.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {
		integration = new DynmapClanMarkerSet().initialize();
		DynmapCommand command = new DynmapCommand("globe", integration);
		getContext().stage(command);
		getContext().stage(command);
	}

	@Override
	public void onEnable() {

		ClanVentBus.MEDIUM_PRIORITY.subscribeTo(CommandInformationAdaptEvent.class, "clans;dynmap-info_adapt", (e, subscription) -> {
			ClanAddon cycle = ClanAddonQueue.getInstance().get("Dynmap");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/c &bglobe &ashow &8[all]");
			e.insert("&7|&e) &6/c &bglobe &chide &8[all]");
		}).queue();

		ClanVentBus.MEDIUM_PRIORITY.subscribeTo(AssociateObtainLandEvent.class, "clans;dynmap-land_obtain", (event, subscription) -> {
			event.getPlayer().performCommand("c globe show");
		}).queue();

		ClanVentBus.HIGHEST_PRIORITY.subscribeTo(AssociateUnClaimEvent.class, "clans;dynmap-land_loss", (event, subscription) -> {
			if (!event.isCancelled()) {
				event.getPlayer().performCommand("c globe hide");
			}
		}).queue();

	}

	@Override
	public void onDisable() {

		ClanVentBus mediumPriority = ClanVentBus.MEDIUM_PRIORITY;
		mediumPriority.unsubscribeFrom(CommandInformationAdaptEvent.class, "clans;dynmap-info_adapt").deploy();
		mediumPriority.unsubscribeFrom(CommandInformationAdaptEvent.class, "clans;dynmap-land_obtain").deploy();
		mediumPriority.unsubscribeFrom(CommandInformationAdaptEvent.class, "clans;dynmap-land_loss").deploy();


	}

	public DynmapClanMarkerSet getMarkerSet() {
		return integration;
	}

}
