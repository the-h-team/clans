package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandRemenemy extends ClanSubCommand {
	public CommandRemenemy() {
		super("remenemy");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 1) {
			Bukkit.dispatchCommand(p, "c enemy remove " + args[0]);
			return true;
		}
		return true;
	}
}
