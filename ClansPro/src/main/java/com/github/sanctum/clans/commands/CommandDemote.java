package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandDemote extends ClanSubCommand {
	public CommandDemote() {
		super("demote");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote")));
				return true;
			}
			lib.sendMessage(p, lib.commandDemote());
			return true;
		}

		if (args.length == 1) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote")));
				return true;
			}
			if (associate != null) {
				if (Clearance.MANAGE_POSITIONS.test(associate)) {
					UUID tid = Clan.ACTION.getUserID(args[0]);
					if (tid == null) {
						lib.sendMessage(p, lib.playerUnknown(args[0]));
						return true;
					}
					Clan.Associate member = associate.getClan().getMember(m -> m.getId().equals(tid));
					if (member == null) return true;
					if (member.getPriority().toLevel() >= associate.getPriority().toLevel()) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
					Clan.ACTION.demotePlayer(tid);
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}

	@Override
	public List<String> tab(Player p, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, () -> {
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
					if (associate == null) {
						return new ArrayList<>();
					}
					return associate.getClan().stream().map(InvasiveEntity::getName).collect(Collectors.toList());
				})
				.get();
	}

}
