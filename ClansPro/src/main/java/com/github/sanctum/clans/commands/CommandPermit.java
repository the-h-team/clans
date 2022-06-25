package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.ClearanceLog;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandPermit extends ClanSubCommand {
	public CommandPermit() {
		super("permit");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("permit")).deploy()) {
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
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("permit")).deploy()) {
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
				ClearanceLog log = associate.getClan().getPermissions();
				log.set(Clearance.LAND_USE, Clearance.Level.ADMIN);
				if (Clearance.MANAGE_PERMS.test(associate)) {

					Clearance target = null;
					for (Map.Entry<Clearance, Integer> entry : log) {
						if (StringUtils.use(entry.getKey().getName()).containsIgnoreCase(args[0])) {
							target = entry.getKey();
							break;
						}
					}

					if (target == null) {
						lib.sendMessage(p, "&cUnknown permission.");
						return true;
					}

					int t = Integer.parseInt(args[1]);

					if (!Arrays.asList(Clearance.Level.values()).contains(t)) {
						lib.sendMessage(p, "&cAn invalid rank level was provided");
						return true;
					}

					log.set(target, t);
					lib.sendMessage(p, "&aClan permission &f" + target.getName() + " &arequired rank level changed to &6" + t);

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
					Optional<Clan.Associate> associate = ClansAPI.getInstance().getAssociate(p);
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("permit")).deploy()) {
						return result;
					}
					if (associate.isPresent()) {
						result.addAll(associate.get().getClan().getPermissions().stream().map(Map.Entry::getKey).map(Nameable::getName).map(String::toLowerCase).collect(Collectors.toList()));
					} else {
						result.addAll(Arrays.stream(Clearance.values()).map(Nameable::getName).map(String::toLowerCase).collect(Collectors.toList()));
					}
					return result;
				})
				.then(TabCompletionIndex.THREE, getLabel(), TabCompletionIndex.ONE, Arrays.stream(Clearance.Level.values()).sorted(Integer::compareTo).map(String::valueOf).collect(Collectors.toList()))
				.get();
	}
}
