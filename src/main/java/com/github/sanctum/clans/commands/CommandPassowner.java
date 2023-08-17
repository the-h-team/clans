package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.util.StringLibrary;
import java.util.UUID;
import org.bukkit.entity.Player;

public class CommandPassowner extends ClanSubCommand {
	public CommandPassowner() {
		super("passowner");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.passowner.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("passowner")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("passowner")));
				return true;
			}
			lib.sendMessage(p, lib.commandPassowner());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("passowner")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("passowner")));
				return true;
			}
			if (associate != null) {
				UUID target = Clan.ACTION.getId(args[0]).deploy();
				if (target != null) {

					if (associate.getRank().getLevel() == 3) {
						if (!associate.getClan().transferOwnership(ClansAPI.getInstance().getAssociate(target).get())) {
							sendMessage(p, lib.playerUnknown("clan member"));
						} else {
							associate.getClan().broadcast("&eClan ownership was transferred to associate " + args[0]);
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}

				} else {
					lib.sendMessage(p, lib.playerUnknown(args[0]));
					return true;
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}


		return true;
	}
}
