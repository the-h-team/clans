package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandPromote extends ClanSubCommand {
	public CommandPromote() {
		super("promote");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("promote")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
				return true;
			}
			lib.sendMessage(p, lib.commandPromote());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("promote")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
				return true;
			}
			FileManager main = ClansAPI.getDataInstance().getConfig();
			String adminRank = main.getRoot().getString("Formatting.Chat.Styles.Full.Admin");
			String ownerRank = main.getRoot().getString("Formatting.Chat.Styles.Full.Owner");
			if (associate != null) {
				if (Clearance.MANAGE_POSITIONS.test(associate)) {
					UUID tid = Clan.ACTION.getId(args[0]).deploy();
					if (tid == null) {
						lib.sendMessage(p, lib.playerUnknown(args[0]));
						return true;
					}
					Clan.Associate member = associate.getClan().getMember(m -> m.getId().equals(tid));
					if (member == null) return true;
					if (member.getPriority().toLevel() >= 2) {
						lib.sendMessage(p, lib.alreadyMax(adminRank, ownerRank));
						return true;
					}
					Clan.ACTION.promote(tid).deploy();
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
