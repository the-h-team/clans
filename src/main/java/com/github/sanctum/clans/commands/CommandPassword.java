package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import org.bukkit.entity.Player;

public class CommandPassword extends ClanSubCommand {
	public CommandPassword() {
		super("password");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			sendMessage(p, lib.notInClan());
			return true;
		}

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("password")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("password")));
				return true;
			}
			lib.sendMessage(p, lib.commandPassword());
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("password")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("password")));
				return true;
			}
			Clan clan = associate.getClan();
			if (Clearance.MANAGE_PASSWORD.test(associate)) {
				if (!isAlphaNumeric(args[0])) {
					lib.sendMessage(p, lib.passwordInvalid());
					return true;
				}
				clan.setPassword(args[0]);
			} else {
				lib.sendMessage(p, lib.noClearance());
				return true;
			}
			return true;
		}

		return true;
	}
}
