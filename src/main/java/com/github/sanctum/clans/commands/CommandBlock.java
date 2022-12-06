package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandBlock extends ClanSubCommand {
	public CommandBlock() {
		super("block");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.block.text"));
	}

	static final PantherMap<Player, List<UUID>> blockedUsers = new PantherEntryMap<>();

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			sendMessage(p, lib.notInClan());
			return true;
		}

		if (args.length == 0) {
			lib.sendMessage(p, "&7|&e)&r Usage: &6/" + label + " block <playerName>");
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("block")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("block")));
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target != null) {
				List<UUID> a = blockedUsers.computeIfAbsent(p, new ArrayList<>());
				if (a.contains(target.getUniqueId())) {
					// already blocked
					a.remove(target.getUniqueId());
					blockedUsers.put(p, a);
					lib.sendMessage(p, target.getName() + " &a&ohas been unblocked.");
				} else {
					a.add(target.getUniqueId());
					blockedUsers.put(p, a);
					lib.sendMessage(p, target.getName() + " &c&ohas been blocked.");
					return true;
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
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.get();
	}
}
