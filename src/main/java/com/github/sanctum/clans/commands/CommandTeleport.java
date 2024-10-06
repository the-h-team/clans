package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Teleport;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandTeleport extends ClanSubCommand {

	public CommandTeleport() {
		super("teleport");
		setAliases(Collections.singletonList("tp"));
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.teleport.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate passociate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("teleport")).deploy()) {
			lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("teleport")));
			return true;
		}

		if (args.length == 1) {
			if (passociate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			Player search = Bukkit.getPlayer(args[0]);
			if (search != null) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(search).orElse(null);
				if (associate != null && associate.getClan().equals(passociate.getClan())) {
					Teleport teleport = passociate.newTeleport(associate.getAsPlayer().getPlayer());
					if (teleport != null) {
						teleport.teleport();
					}
				}
			} else {
				lib.sendMessage(p, lib.playerUnknown(args[0]));
			}
			return true;
		}
		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, () -> getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, () -> {
					List<String> list = new ArrayList<>();
					ClansAPI.getInstance().getAssociate(player).ifPresent(associate -> list.addAll(associate.getClan().getMembers().stream().map(Clan.Associate::getName).collect(Collectors.toList())));
					return list;
				})
				.get();
	}

}
