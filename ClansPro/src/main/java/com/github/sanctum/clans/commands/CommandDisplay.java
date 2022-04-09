package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import org.bukkit.entity.Player;

public class CommandDisplay extends ClanSubCommand {
	public CommandDisplay() {
		super("display");
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
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("display"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("display")));
				return true;
			}
			lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("display"));
			return true;
		}

		if (args.length == 1) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("display"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("display")));
				return true;
			}
			if (Clearance.MANAGE_NICK_NAME.test(associate)) {
				if (ClansAPI.getInstance().isNameBlackListed(args[0])) {
					lib.sendMessage(p, "&4This name is not allowed!");
					return false;
				}
				associate.getClan().setNickname(args[0]);
				associate.getClan().broadcast("Our new nickname has been updated to '" + associate.getClan().getNickname() + "'");
			} else {
				lib.sendMessage(p, lib.noClearance());
			}
			return true;
		}

		if (args.length == 2) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("display"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("display")));
				return true;
			}
			if (Clearance.MANAGE_NICK_NAME.test(associate)) {
				if (ClansAPI.getInstance().isNameBlackListed(args[0]) || ClansAPI.getInstance().isNameBlackListed(args[1])) {
					lib.sendMessage(p, "&4This name is not allowed!");
					return false;
				}
				associate.getClan().setNickname(args[0] + " " + args[1]);
				associate.getClan().broadcast("Our new nickname has been updated to '" + associate.getClan().getNickname() + "'");
			} else {
				lib.sendMessage(p, lib.noClearance());
			}
			return true;
		}

		return true;
	}
}
