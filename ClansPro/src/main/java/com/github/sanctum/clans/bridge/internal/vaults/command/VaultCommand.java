package com.github.sanctum.clans.bridge.internal.vaults.command;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.VaultsAddon;
import com.github.sanctum.clans.bridge.internal.vaults.events.VaultOpenEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Permission;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultCommand extends ClanSubCommand {
	public VaultCommand(String label) {
		super(label);
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		int length = args.length;
		if (length == 0) {

			if (ClansAPI.getInstance().getClanID(p.getUniqueId()) != null) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

				if (associate == null) {

					return true;
				}
				if (!Permission.MANAGE_VAULT.test(associate)) {
					Clan.ACTION.sendMessage(p, Clan.ACTION.noClearance());
					return true;
				}
				Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
				Menu pull = VaultsAddon.getVault(clan.getName());
				VaultOpenEvent event = ClanVentBus.call(new VaultOpenEvent(clan, p, pull, pull.getInventory().getElement().getViewers()));
				if (!event.isCancelled()) {
					event.open();
				}
			} else {
				Clan.ACTION.sendMessage(p, Clan.ACTION.notInClan());
			}
			return true;
		}
		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		if (args.length == 1) {
			return getBaseCompletion(args);
		}
		return null;
	}
}
