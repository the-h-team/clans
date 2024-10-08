package com.github.sanctum.clans.model.addon;

import com.github.sanctum.clans.model.*;
import com.github.sanctum.clans.model.addon.vaults.VaultMenu;
import com.github.sanctum.clans.model.addon.vaults.command.VaultCommand;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.panther.event.Vent;
import org.jetbrains.annotations.NotNull;

public class VaultsAddon extends Clan.Addon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon." + getName() + ".enabled");
	}

	@Override
	public @NotNull String getName() {
		return "Vaults";
	}

	@Override
	public @NotNull String getDescription() {
		return "An addon that grants public clan storage usage.";
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
		getContext().stage(new VaultCommand("vault"));
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			Clan.Addon cycle = ClanAddonRegistry.getInstance().get("Vaults");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fvault");
		});

	}

	@Override
	public void onDisable() {

	}

	public static Menu getVault(String clanName) {
		return MenuType.SINGULAR.get(m -> m.getKey().map(clanName::equals).orElse(false)) != null ? MenuType.SINGULAR.get(m -> m.getKey().map(clanName::equals).orElse(false)) : new VaultMenu(clanName);
	}

}
