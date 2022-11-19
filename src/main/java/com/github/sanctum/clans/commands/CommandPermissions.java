package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.ClearanceLog;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.TextLib;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandPermissions extends ClanSubCommand {
	public CommandPermissions() {
		super("permissions");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.permissions.text"));
		setAliases(Collections.singletonList("perms"));
	}


	private List<String> clean(String[] args) {
		List<String> list = new ArrayList<>();
		for (String s : args) {
			char first = s.charAt(0);
			String capitalize = String.valueOf(first).toUpperCase();
			String full = capitalize + s.substring(Math.min(1, s.length() - 1)).toLowerCase();
			list.add(full);
		}
		return list;
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
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("permissions")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("permissions")));
				return true;
			}
			ClearanceLog log = associate.getClan().getPermissions();
			lib.sendMessage(p, "&eOur clan permission list:");
			Mailer m = Mailer.empty(p);
			m.chat("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
			for (Map.Entry<Clearance, Integer> e : log.stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
				Clearance perm = e.getKey();
				int required = e.getValue();
				m.chat(TextLib.getInstance().textSuggestable("&e" + String.join(" ", clean(perm.getName().split("_"))), " &f= {&a" + required + "&f}", "&eClick to edit this permission.", "c permit " + perm.getName() + " ")).deploy();
			}
			m.chat("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
			FileManager main = ClansAPI.getDataInstance().getConfig();
			String member = main.getRoot().getString("Formatting.Chat.Styles.Full.Member");
			String mod = main.getRoot().getString("Formatting.Chat.Styles.Full.Moderator");
			String admin = main.getRoot().getString("Formatting.Chat.Styles.Full.Admin");
			String owner = main.getRoot().getString("Formatting.Chat.Styles.Full.Owner");
			m.chat("&e0 &f= &2" + member + "&b, &e1 &f= &2" + mod + "&b, &e2 &f= &2" + admin + "&b, &e3 &f= &2" + owner).deploy();
			return true;
		}

		return true;
	}
}
