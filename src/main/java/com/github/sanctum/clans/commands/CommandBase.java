package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import org.bukkit.entity.Player;

public class CommandBase extends ClanSubCommand {
	public CommandBase() {
		super("base");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.base.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("base")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("base")));
				return true;
			}
			if (associate != null) {
				Clan.ACTION.teleport(p, associate.getClan().getBase()).deploy();
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}

		return true;
	}
}
