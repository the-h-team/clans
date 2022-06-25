package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandUnclaim extends ClanSubCommand {
	public CommandUnclaim() {
		super("unclaim");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("unclaim")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaim")));
				return true;
			}
			if (associate != null) {
				if (Claim.ACTION.isEnabled()) {
					if (Clearance.MANAGE_LAND.test(associate)) {
						Claim.ACTION.unclaim(p);
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
					return true;
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}

		if (args.length == 1) {
			if (Claim.ACTION.isEnabled()) {
				if (args[0].equalsIgnoreCase("all")) {
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("unclaimall")).deploy()) {
						lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaimall")));
						return true;
					}
					if (associate != null) {
						if (Clearance.MANAGE_ALL_LAND.test(associate)) {
							Claim.ACTION.unclaimAll(p);
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
			} else {
				lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
				return true;
			}
			return true;
		}


		return true;
	}

	@Override
	public List<String> tab(Player p, String label, String[] args) {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "all")
				.get();
	}
}
