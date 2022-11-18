package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.library.StringUtils;
import org.bukkit.entity.Player;

public class CommandPlayers extends ClanSubCommand {
	public CommandPlayers() {
		super("players");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("players")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("players")));
				return true;
			}
			Clan.ACTION.getPlayerboard(p, 1);
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("players")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("players")));
				return true;
			}
			if (StringUtils.use(args[0]).isInt()) {
				Clan.ACTION.getPlayerboard(p, Integer.parseInt(args[0]));
			} else {
				lib.sendMessage(p, lib.pageUnknown());
			}
			return true;
		}


		return true;
	}
}
