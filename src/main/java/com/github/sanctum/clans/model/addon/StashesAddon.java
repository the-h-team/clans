package com.github.sanctum.clans.model.addon;

import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.model.addon.stashes.StashMenu;
import com.github.sanctum.clans.model.addon.stashes.command.StashCommand;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.panther.event.Vent;
import org.jetbrains.annotations.NotNull;

public class StashesAddon extends Clan.Addon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon." + getName() + ".enabled");
	}

	@Override
	public @NotNull String getName() {
		return "Stashes";
	}

	@Override
	public @NotNull String getDescription() {
		return "A small clan storage space only accessible in the base chunk.";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest", "ms5984"};
	}

	@Override
	public void onLoad() {
		getContext().stage(new StashCommand("stash"));
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.HIGH, (e, subscription) -> {
			Clan.Addon cycle = ClanAddonRegistry.getInstance().get("Stashes");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fstash")
			;
		});

	}

	@Override
	public void onDisable() {

	}

	public static Menu getStash(String clanName) {
		return MenuType.SINGULAR.get(m -> m.getKey().map((clanName + "-stash")::equals).orElse(false)) != null ? MenuType.SINGULAR.get(m -> m.getKey().map((clanName + "-stash")::equals).orElse(false)) : new StashMenu(clanName);
	}

}
