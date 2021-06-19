package com.github.sanctum.link.cycles;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import com.github.sanctum.vaults.VaultContainer;
import com.github.sanctum.vaults.VaultsListener;
import com.github.sanctum.vaults.events.VaultOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class VaultsCycle extends EventCycle {

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
		register(new VaultsListener());
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Vaults");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fvault");
		});


		ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Vaults");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			if (!e.getArgs(1).contains("vault")) {
				e.add(1, "vault");
			}

		});

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Vaults");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getSender();
			int length = e.getArgs().length;
			String[] args = e.getArgs();
			if (length == 1) {
				if (args[0].equalsIgnoreCase("vault")) {

					if (DefaultClan.action.getClanID(p.getUniqueId()) != null) {
						if (DefaultClan.action.getRankPower(p.getUniqueId()) < ClansAPI.getData().getMain().getConfig().getInt("Addon.Vaults.clearance")) {
							DefaultClan.action.sendMessage(p, "&c&oYou do not have clan clearance.");
							e.setReturn(true);
							return;
						}
						Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
						Inventory pull = VaultContainer.getVault(clan.getName());
						VaultOpenEvent event = new VaultOpenEvent(clan, p, pull, pull.getViewers());
						Bukkit.getPluginManager().callEvent(event);
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

}
