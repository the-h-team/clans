package com.github.sanctum.clans.bridge.a;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.internal.borders.BorderListener;
import com.github.sanctum.clans.internal.borders.FlagsCommand;
import com.github.sanctum.clans.internal.borders.TerritoryCommand;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;

public class BorderAddon extends ClanAddon {

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
		return "Borders";
	}

	@Override
	public String getDescription() {
		return "A Clans [Free] ported addon, allowing users to view chunk borders.";
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
		register(new BorderListener());
		register(new FlagsCommand());
		register(new TerritoryCommand());
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Borders");

			if (cycle != null && !cycle.isActive()) {
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
