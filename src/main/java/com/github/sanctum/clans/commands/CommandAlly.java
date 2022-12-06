package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandAlly extends ClanSubCommand {
	public CommandAlly() {
		super("ally");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.ally.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("ally")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally")));
				return true;
			}
			lib.sendMessage(p, lib.commandAlly());
			return true;
		}

		if (args.length == 1) {
			Bukkit.dispatchCommand(p, "c ally add " + args[0]);
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("add")) {
				if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("ally")).deploy()) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally")));
					return true;
				}
				if (associate != null) {
					Clan c = associate.getClan();
					if (!Clearance.MANAGE_RELATIONS.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
					if (!Clan.ACTION.getAllClanNames().contains(args[1])) {
						lib.sendMessage(p, lib.clanUnknown(args[1]));
						return true;
					}
					Clan t = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[1]));
					if (args[1].equals(c.getName())) {
						lib.sendMessage(p, lib.allianceDenial());
						return true;
					}
					if (c.getRelation().getAlliance().has(t)) {
						lib.sendMessage(p, lib.alreadyAllies(args[1]));
						return true;
					}
					if (t.getRelation().getRivalry().has(c)) {
						lib.sendMessage(p, lib.alreadyEnemies(args[1]));
						return true;
					}
					if (c.getRelation().isNeutral(t)) {
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						if (t.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
							return true;
						}
						c.getRelation().getAlliance().request(t);
						return true;
					}
					if (c.isPeaceful()) {
						lib.sendMessage(p, lib.peacefulDeny());
						return true;
					}
					if (t.isPeaceful()) {
						lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
						return true;
					}
					c.getRelation().getAlliance().add(t);
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("remove")) {
				if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("removeally")).deploy()) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeally")));
					return true;
				}
				if (associate != null) {
					Clan c = associate.getClan();
					if (!Clearance.MANAGE_RELATIONS.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
					if (!Clan.ACTION.getAllClanNames().contains(args[1])) {
						lib.sendMessage(p, lib.clanUnknown(args[1]));
						return true;
					}
					Clan t = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args[1]));
					if (args[1].equals(c.getName())) {
						lib.sendMessage(p, lib.allianceDenial());
						return true;
					}

					List<String> online = new ArrayList<>();
					t.getMembers().forEach(a -> {
						if (a.getTag().isPlayer() && a.getTag().getPlayer().isOnline()) {
							online.add(a.getName());
						}
					});
					if (ClansAPI.getDataInstance().isTrue("Clans.relations.ally.cancel-if-empty")) {
						if (online.isEmpty()) {
							lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
							return true;
						}
					}

					if (c.getRelation().isNeutral(t)) {
						lib.sendMessage(p, lib.alreadyNeutral(args[1]));
						return true;
					}
					if (c.isPeaceful()) {
						lib.sendMessage(p, lib.peacefulDeny());
						return true;
					}
					if (t.isPeaceful()) {
						lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
						return true;
					}
					c.getRelation().getAlliance().remove(t);
					t.getRelation().getAlliance().remove(c);
					c.broadcast(lib.neutral(t.getName()));
					t.broadcast(lib.neutral(c.getName()));
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "add", "remove")
				.then(TabCompletionIndex.THREE, getLabel(), TabCompletionIndex.ONE, Clan.ACTION.getAllClanNames())
				.get();
	}

}
