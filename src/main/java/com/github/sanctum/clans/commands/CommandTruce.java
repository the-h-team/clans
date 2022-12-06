package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import org.bukkit.entity.Player;

public class CommandTruce extends ClanSubCommand {
	public CommandTruce() {
		super("truce");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.truce.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("truce")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("truce")));
				return true;
			}
			if (associate != null) {
				War current = ClansAPI.getInstance().getArenaManager().get(associate);
				if (current != null && current.isRunning()) {
					GUI.ARENA_TRUCE.get(current).open(p);
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
