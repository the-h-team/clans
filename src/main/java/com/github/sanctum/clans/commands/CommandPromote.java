package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.InvasiveEntity;
import com.github.sanctum.clans.model.RankRegistry;
import com.github.sanctum.clans.util.StringLibrary;
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
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.promote.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("promote")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
				return true;
			}
			lib.sendMessage(p, lib.commandPromote());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("promote")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
				return true;
			}
			FileManager main = ClansAPI.getDataInstance().getConfig();
			if (associate != null) {
				if (Clearance.MANAGE_POSITIONS.test(associate)) {
					UUID tid = Clan.ACTION.getId(args[0]).deploy();
					if (tid == null) {
						lib.sendMessage(p, lib.playerUnknown(args[0]));
						return true;
					}
					Clan.Associate member = associate.getClan().getMember(m -> m.getId().equals(tid));
					if (member == null) return true;
					Clan.Rank promotion = member.getRank().getPromotion();
					if (promotion == null || promotion.getPromotion() == null) { // -1 from highest rank, cant go any higher (owner)
						lib.sendMessage(p, lib.alreadyMax(member.getRank().getName(), RankRegistry.getInstance().getHighest().getName()));
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
