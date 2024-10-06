package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.util.StringLibrary;
import org.bukkit.entity.Player;

public class CommandDescription extends ClanSubCommand {
	public CommandDescription() {
		super("description");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.description.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("description")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
				return true;
			}
			if (associate != null) {
				if (Clearance.MANAGE_DESCRIPTION.test(associate)) {
					Clan c = associate.getClan();
					c.setDescription(args[0]);
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
			}
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("description")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
				return true;
			}
			if (associate != null) {
				if (Clearance.MANAGE_DESCRIPTION.test(associate)) {
					Clan c = associate.getClan();
					c.setDescription(args[0] + " " + args[1]);
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
			}
			return true;
		}
		String args0 = args[0];
		StringBuilder rsn = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			rsn.append(args[i]).append(" ");
		int stop = rsn.length() - 1;
		if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("description")).deploy()) {
			lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
			return true;
		}
		if (associate != null) {
			if (Clearance.MANAGE_DESCRIPTION.test(associate)) {
				Clan c = associate.getClan();
				String result = rsn.substring(0, stop);
				if (result.length() >= 180) {
					lib.sendMessage(p, "&cDescription cannot exceed 180 characters!");
					return true;
				}
				c.setDescription(result);
			} else {
				lib.sendMessage(p, lib.noClearance());
				return true;
			}
		} else {
			lib.sendMessage(p, lib.notInClan());
		}
		return true;
	}
}
