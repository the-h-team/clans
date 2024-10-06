package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.ClearanceOverride;
import com.github.sanctum.clans.model.RankRegistry;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.library.Mailer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
			lib.sendMessage(p, "&eOur clan permission list:");
			Mailer m = Mailer.empty(p);
			m.chat("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
			ClearanceOverride override = associate.getClan().getPermissiveHandle();
			RankRegistry registry = RankRegistry.getInstance();
			for (Clan.Rank r : registry.getRanks()) {
				List<Clearance> list = override.getRaw(r);
				List<Clan.Rank> inheritance = override.getInheritance(r);
				FancyMessage clearances = new FancyMessage();
				int i = 0;
				for (Clearance c : list) {
					clearances.then("&f- ").then("&a" + c.getName()).hover("&fClick to modify.").action(() -> {
						FancyMessage modify = new FancyMessage();
						modify.then("Modifying permission " + c.getName() + ":").then("\n");
						modify.then("&7[&aAdd&7]").hover("Click to add this permission to another rank").suggest("/c permit " + c.getName() + " ").then(" &f: ").then("&7[&cRemove&7]").hover("Click to remove this permission from another rank").suggest("/c revoke " + c.getName() + " ");
						modify.send(p).deploy();
					});
					if (i < list.size() - 1) {
						clearances.then("\n");
					}
					i++;
				}
				FancyMessage inher = new FancyMessage();
				inher.then("&7Inheritance: &6[&f&o");
				int index = 0;
				for (Clan.Rank rank : inheritance) {
					inher.then(rank.getName()).hover("Click to remove").suggest("/c forget " + rank.getName() + " " + r.getName());
					if (index < inheritance.size() - 1) {
						inher.then(", ");
					}
					index++;
				}
				inher.then("&6]");
				FancyMessage ra = new FancyMessage();
				ra.then("&7Rank &3" + r.getName() + "&f:");
				ra.send(p).deploy();
				inher.send(p).deploy();
				clearances.send(p).deploy();
			}
			m.chat("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
			FancyMessage msg = new FancyMessage();
			for (Clan.Rank r : registry.getRanks()) {
				msg.then("&e" + r.getLevel() + " &f= &2" + r.getName() + " ");
			}
			m.chat(msg.build()).deploy();
			return true;
		}

		return true;
	}
}
