package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import org.bukkit.entity.Player;

public class CommandBiography extends ClanSubCommand {
	public CommandBiography() {
		super("bio");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.bio.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (args.length == 0) {
			lib.sendMessage(p, "&cExpected arguments!");
			return true;
		}
		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bio")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
				return true;
			}
			if (associate != null) {
				associate.setBio(args[0]);
			}
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bio")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
				return true;
			}
			if (associate != null) {
				associate.setBio(args[0] + " " + args[1]);
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}
		String args0 = args[0];
		StringBuilder rsn = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			rsn.append(args[i]).append(" ");
		int stop = rsn.length() - 1;
		if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bio")).deploy()) {
			lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
			return true;
		}
		if (associate != null) {
			associate.setBio(rsn.substring(0, stop));
		} else {
			lib.sendMessage(p, lib.notInClan());
		}
		return true;
	}
}
