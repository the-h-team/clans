package com.github.sanctum.clans.bridge.internal;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.vaults.VaultMenu;
import com.github.sanctum.clans.bridge.internal.vaults.events.VaultOpenEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Permission;
import com.github.sanctum.clans.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.events.command.CommandInsertEvent;
import com.github.sanctum.clans.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.entity.Player;

public class VaultsAddon extends ClanAddon {

	@Override
	public boolean isStaged() {
		return ClansAPI.getData().isTrue("Addon." + getName() + ".enabled");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Vaults";
	}

	@Override
	public String getDescription() {
		return "An addon that grants public clan storage usage.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest", "ms5984"};
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Vaults");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fvault");
		});


		ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Vaults");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			if (!e.getArgs(1).contains("vault")) {
				e.add(1, "vault");
			}

		});

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			ClanAddon cycle = ClanAddonQuery.getAddon("Vaults");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getSender();
			int length = e.getArgs().length;
			String[] args = e.getArgs();
			if (length == 1) {
				if (args[0].equalsIgnoreCase("vault")) {

					if (ClansAPI.getInstance().getClanID(p.getUniqueId()) != null) {
						Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

						if (associate == null) {

							e.setReturn(true);
							return;
						}
						if (!Permission.MANAGE_VAULT.test(associate)) {
							Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
							e.setReturn(true);
							return;
						}
						Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
						Menu pull = getVault(clan.getName());
						VaultOpenEvent event = ClanVentBus.call(new VaultOpenEvent(clan, p, pull, pull.getInventory().getElement().getViewers()));
						if (!event.isCancelled()) {
							event.open();
						}
					} else {
						e.stringLibrary().sendMessage(p, e.stringLibrary().notInClan());
					}
					e.setReturn(true);
				}
			}

		});

	}

	@Override
	public void onDisable() {

	}

	public static Menu getVault(String clanName) {
		return MenuType.SINGULAR.get(m -> m.getKey().map(clanName::equals).orElse(false)) != null ? MenuType.SINGULAR.get(m -> m.getKey().map(clanName::equals).orElse(false)) : new VaultMenu(clanName);
	}

}
