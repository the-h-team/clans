package com.github.sanctum.vaults;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.vaults.events.VaultOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class VaultsCommand implements Listener {

	@EventHandler
	public void onCommandHelp(CommandHelpInsertEvent e) {
		e.insert("&7|&e) &6/clan &fvault");
	}

	@EventHandler
	public void onVaultTab(TabInsertEvent e) {
		if (!e.getArgs(1).contains("vault")) {
			e.add(1, "vault");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void vaultFramework(CommandInsertEvent e) {
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
	}

}
