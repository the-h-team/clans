package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.backend.ClanFileBackend;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandTop extends ClanSubCommand {
	public CommandTop() {
		super("top");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.top.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("top")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("top")));
				return true;
			}
			Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.POWER, p, 1);
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("top")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("top")));
				return true;
			}
			switch (args[0].toLowerCase()) {
				case "money":
					Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.MONEY, p, 1);
					break;
				case "power":
					Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.POWER, p, 1);
					break;
				case "wins":
					Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.WINS, p, 1);
					break;
				case "kd":
					Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.KILLS, p, 1);
					break;
				default:
					lib.sendMessage(p, lib.pageUnknown());
					break;
			}
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("top")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("top")));
				return true;
			}
			try {
				switch (args[0].toLowerCase()) {
					case "money":
						Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.MONEY, p, Integer.parseInt(args[1]));
						break;
					case "power":
						Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.POWER, p, Integer.parseInt(args[1]));
						break;
					case "wins":
						Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.WINS, p, Integer.parseInt(args[1]));
						break;
					case "kd":
						Clan.ACTION.getLeaderboard(ClanFileBackend.LeaderboardType.KILLS, p, Integer.parseInt(args[1]));
						break;
					default:
						lib.sendMessage(p, lib.pageUnknown());
						break;
				}
			} catch (NumberFormatException ignored) {
			}
			return true;
		}


		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "wins", "money", "power", "kd")
				.get();
	}
}
