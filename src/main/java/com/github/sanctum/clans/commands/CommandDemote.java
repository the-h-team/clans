package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.InvasiveEntity;
import com.github.sanctum.clans.model.RankRegistry;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
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
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.demote.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("demote")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote")));
				return true;
			}
			lib.sendMessage(p, lib.commandDemote());
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("demote")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote")));
				return true;
			}
			if (associate != null) {
				if (Clearance.MANAGE_POSITIONS.test(associate)) {
					UUID tid = Clan.ACTION.getId(args[0]).deploy();
					if (tid == null) {
						lib.sendMessage(p, lib.playerUnknown(args[0]));
						return true;
					}
					Clan.Associate member = associate.getClan().getMember(m -> m.getId().equals(tid));
					if (member == null) return true;
					if (member.equals(associate) && associate.getRank().isHighest()) {
						FancyMessage msg = new FancyMessage(ClansAPI.getInstance().getPrefix().toString());
						msg.then(" ").then("&cYou own this clan, do you want to leave it? ").then("&f[").then("&aYes").hover("&aClick to disband this clan.").command("c leave").then("&f]");
						msg.send(p).queue();
						return true;
					}
					if (member.getRank().getLevel() >= associate.getRank().getLevel()) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
					Clan.Rank demotion = member.getRank().getDemotion();
					if (demotion == null) { // lowest rank, cant go any lower (member)
						lib.sendMessage(p, lib.alreadyMin(member.getRank().getName(), RankRegistry.getInstance().getLowest().getName()));
						return true;
					}
					Clan.ACTION.demote(tid).deploy();
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
