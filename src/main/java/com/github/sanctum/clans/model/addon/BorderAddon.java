package com.github.sanctum.clans.model.addon;

import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.model.addon.borders.BorderListener;
import com.github.sanctum.clans.model.addon.borders.FlagsCommand;
import com.github.sanctum.clans.model.addon.borders.TerritoryCommand;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.panther.event.Vent;
import org.jetbrains.annotations.NotNull;

public class BorderAddon extends Clan.Addon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon." + getName() + ".enabled") && !LabyrinthProvider.getInstance().isLegacy();
	}

	@Override
	public @NotNull String getName() {
		return "Borders";
	}

	@Override
	public @NotNull String getDescription() {
		return "A Clans [Free] ported addon, allowing users to view chunk borders.";
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
		getContext().stage(new BorderListener());
		getContext().stage(new FlagsCommand());
		getContext().stage(new TerritoryCommand());
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			Clan.Addon cycle = ClanAddonRegistry.getInstance().get("Borders");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fterritory &7| &8optional:&f-f &7<&8flag&7>");
			e.insert("&7|&e) &6/clan &fflags");

		});

	}

	@Override
	public void onDisable() {

	}
}
