package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import org.bukkit.entity.Player;

public class CommandMembers extends ClanSubCommand {
	public CommandMembers() {
		super("members");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {

			if (associate != null) {
				Clan.ACTION.getClanboard(p, 1);
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}

		if (args.length == 1) {

			if (associate != null) {
				try {
					int page = Integer.parseInt(args[0]);
					Clan.ACTION.getClanboard(p, page);
				} catch (NumberFormatException e) {
					lib.sendMessage(p, lib.pageUnknown());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}
}
