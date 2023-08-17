package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.util.StringLibrary;
import java.text.MessageFormat;
import org.bukkit.entity.Player;

public class CommandMessage extends ClanSubCommand {
	public CommandMessage() {
		super("message");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.message.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("message")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
				return true;
			}
			lib.sendMessage(p, lib.commandMessage());
			return true;
		}

		if (associate == null) {
			lib.sendMessage(p, lib.notInClan());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("message")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
				return true;
			}
			Clan clan = associate.getClan();

			clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + args[0]);
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("message")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
				return true;
			}
			Clan clan = associate.getClan();
			clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + args[0] + " " + args[1]);
			return true;
		}
		StringBuilder rsn = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			rsn.append(args[i]).append(" ");
		int stop = rsn.length() - 1;
		Clan clan = associate.getClan();
		clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + rsn.substring(0, stop));
		return true;
	}
}
