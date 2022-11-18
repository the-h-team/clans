package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.library.StringUtils;
import org.bukkit.entity.Player;

public class CommandList extends ClanSubCommand {
	public CommandList() {
		super("list");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.list.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("list")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("list")));
				return true;
			}
			Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.NAME, p, 1);
			return true;
		}

		if (args.length == 1) {
			if (StringUtils.use(args[0]).isInt()) {
				lib.sendMessage(p, lib.pageUnknown());
				return true;
			}
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("list")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("list")));
				return true;
			}
			Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.NAME, p, Integer.parseInt(args[0]));
			return true;
		}

		return true;
	}
}
