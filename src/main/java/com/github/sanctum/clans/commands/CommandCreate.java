package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import java.util.Collections;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandCreate extends ClanSubCommand {
	public CommandCreate() {
		super("create");
		setAliases(Collections.singletonList("c"));
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.create.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("create")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			lib.sendMessage(p, lib.commandCreate());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("create")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			if (!isAlphaNumeric(args[0])) {
				lib.sendMessage(p, lib.nameInvalid(args[0]));
				return true;
			}
			if (Clan.ACTION.getAllClanNames().contains(args[0])) {
				lib.sendMessage(p, lib.alreadyMade(args[0]));
				return true;
			}
			for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getNode("Clans.name-blacklist").getKeys(false)) {
				if (Pattern.compile(Pattern.quote(args[0]), Pattern.CASE_INSENSITIVE).matcher(s).find()) {
					lib.sendMessage(p, "&c&oThis name is not allowed!");
					String response = ClansAPI.getDataInstance().getConfig().getRoot().getNode("Clans.name-blacklist").getNode(s).getNode("action").toPrimitive().getString();
					if (response != null && !response.isEmpty()) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), response.replace("{PLAYER}", p.getName()));
					}
					return true;
				}
			}
			Clan.ACTION.create(p.getUniqueId(), args[0], null, false).deploy();
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("create")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
				return true;
			}
			if (!isAlphaNumeric(args[0])) {
				lib.sendMessage(p, lib.nameInvalid(args[0]));
				return true;
			}
			if (Clan.ACTION.getAllClanNames().contains(args[0])) {
				lib.sendMessage(p, lib.alreadyMade(args[0]));
				return true;
			}
			for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getNode("Clans.name-blacklist").getKeys(false)) {
				if (Pattern.compile(Pattern.quote(args[0]), Pattern.CASE_INSENSITIVE).matcher(s).find()) {
					lib.sendMessage(p, "&c&oThis name is not allowed!");
					String response = ClansAPI.getDataInstance().getConfig().getRoot().getNode("Clans.name-blacklist").getNode(s).getNode("action").toPrimitive().getString();
					if (response != null && !response.isEmpty()) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), response.replace("{PLAYER}", p.getName()));
					}
					return true;
				}
			}
			Clan.ACTION.create(p.getUniqueId(), args[0], args[1], false).deploy();
			return true;
		}


		return true;
	}
}
