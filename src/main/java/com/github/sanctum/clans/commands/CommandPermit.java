package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.ClearanceOverride;
import com.github.sanctum.clans.model.RankRegistry;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandPermit extends ClanSubCommand {
	public CommandPermit() {
		super("permit");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.permit.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("permit")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit")));
				return true;
			}
			lib.sendMessage(p, lib.commandPermit());
			return true;
		}

		if (args.length == 1) {
			if (associate != null) {
				if (Clearance.MANAGE_PERMS.test(associate)) {
					lib.sendMessage(p, lib.commandPermit());
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("permit")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit")));
				return true;
			}
			if (associate != null) {
				try {
					Integer.parseInt(args[1]);
				} catch (NumberFormatException ig) {
					lib.sendMessage(p, "&cAn invalid rank level was provided");
					return true;
				}
				ClearanceOverride log = associate.getClan().getPermissiveHandle();
				if (Clearance.MANAGE_PERMS.test(associate)) {

					Clearance target = null;
					for (Clearance entry : Clearance.values()) {
						if (StringUtils.use(entry.getName()).containsIgnoreCase(args[0])) {
							target = entry;
							break;
						}
					}

					if (target == null) {
						lib.sendMessage(p, "&cUnknown permission.");
						return true;
					}

					int t = Integer.parseInt(args[1]);
					Clan.Rank r = RankRegistry.getInstance().getRank(t);
					if (r == null) {
						lib.sendMessage(p, "&cAn invalid rank level was provided");
						return true;
					}
					if (log.add(target, r)) {
						lib.sendMessage(p, "&aClan permission &f" + target.getName() + " &awas given to rank &6" + r.getName());
					} else {
						lib.sendMessage(p, "&cClan permission &f" + target.getName() + " &ccan't be given to rank &6" + r.getName() + " &cbecause it is already directly inherited.");
					}

				} else {
					lib.sendMessage(p, lib.noClearance());
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
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
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("permit")).deploy()) {
						return result;
					}
					result.addAll(Arrays.stream(Clearance.values()).map(Nameable::getName).map(String::toLowerCase).collect(Collectors.toList()));
					return result;
				})
				.then(TabCompletionIndex.THREE, getLabel(), TabCompletionIndex.ONE, Arrays.stream(Clearance.Level.values()).sorted(Integer::compareTo).map(String::valueOf).collect(Collectors.toList()))
				.get();
	}
}
