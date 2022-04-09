package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import java.text.MessageFormat;
import org.bukkit.entity.Player;

public class CommandMessage extends ClanSubCommand {
	public CommandMessage() {
		super("message");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
				return true;
			}
			lib.sendMessage(p, lib.commandMessage());
			return true;
		}

		if (args.length == 1) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
				return true;
			}
			Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
			if (associate != null)
				clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + args[0]);
			return true;
		}

		if (args.length == 2) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
				return true;
			}
			if (associate != null) {
				Clan clan = associate.getClan();
				clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + args[0] + " " + args[1]);
			}
			return true;
		}
		StringBuilder rsn = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			rsn.append(args[i]).append(" ");
		int stop = rsn.length() - 1;
		if (associate != null) {
			Clan clan = associate.getClan();
			clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + rsn.substring(0, stop));
		} else {
			lib.sendMessage(p, lib.notInClan());
		}
		return true;
	}
}
