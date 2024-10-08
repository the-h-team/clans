package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import java.util.Collections;
import org.bukkit.entity.Player;

public class CommandNickname extends ClanSubCommand {
	public CommandNickname() {
		super("nickname");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.nickname.text"));
		setAliases(Collections.singletonList("nick"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("nickname")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("nickname")));
				return true;
			}
			lib.sendMessage(p, lib.commandNick());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("nickname")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("nickname")));
				return true;
			}
			if (associate != null) {
				if (!isAlphaNumeric(args[0])) {
					lib.sendMessage(p, lib.nameInvalid(args[0]));
					return true;
				}
				associate.setNickname(args[0]);
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}
}
