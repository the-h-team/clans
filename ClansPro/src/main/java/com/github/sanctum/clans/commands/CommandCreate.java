package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import java.util.Collections;
import org.bukkit.entity.Player;

public class CommandCreate extends ClanSubCommand {
	public CommandCreate() {
		super("create");
		setAliases(Collections.singletonList("c"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("create")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			lib.sendMessage(p, lib.commandCreate());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("create")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			if (!isAlphaNumeric(args[0])) {
				lib.sendMessage(p, lib.nameInvalid(args[0]));
				return true;
			}
			if (Clan.ACTION.getAllClanNames().contains(args[0])) {
				lib.sendMessage(p, lib.alreadyMade(args[0]));
				return true;
			}
			Clan.ACTION.create(p.getUniqueId(), args[0], null, false).deploy();
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("create")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			if (!isAlphaNumeric(args[0])) {
				lib.sendMessage(p, lib.nameInvalid(args[0]));
				return true;
			}
			if (Clan.ACTION.getAllClanNames().contains(args[0])) {
				lib.sendMessage(p, lib.alreadyMade(args[0]));
				return true;
			}
			Clan.ACTION.create(p.getUniqueId(), args[0], args[1], false).deploy();
			return true;
		}


		return true;
	}
}
