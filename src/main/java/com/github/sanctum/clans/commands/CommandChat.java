package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.util.StringLibrary;
import org.bukkit.entity.Player;

public class CommandChat extends ClanSubCommand {
	public CommandChat() {
		super("chat");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.chat.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("chat")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("chat")));
				return true;
			}
			if (associate != null) {
				if (associate.getChannel().getId().equals("GLOBAL")) {
					associate.setChannel("CLAN");
					lib.sendMessage(p, lib.commandChat("CLAN"));
					return true;
				}
				if (associate.getChannel().getId().equals("CLAN")) {
					associate.setChannel("ALLY");
					lib.sendMessage(p, lib.commandChat("ALLY"));
					return true;
				}
				if (associate.getChannel().getId().equals("ALLY")) {
					associate.setChannel("GLOBAL");
					lib.sendMessage(p, lib.commandChat("GLOBAL"));
					return true;
				}
				associate.setChannel("GLOBAL");
				lib.sendMessage(p, lib.commandChat("GLOBAL"));
			}
			return true;
		}


		return true;
	}
}
