package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import org.bukkit.entity.Player;

public class CommandJoin extends ClanSubCommand {
	public CommandJoin() {
		super("join");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.join.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("join")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
				return true;
			}
			lib.sendMessage(p, lib.commandJoin());
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("join")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
				return true;
			}
			Clan.ACTION.join(p.getUniqueId(), args[0], null, false).deploy();
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("join")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
				return true;
			}
			Clan.ACTION.join(p.getUniqueId(), args[0], args[1], false).deploy();
			return true;
		}

		return true;
	}
}
