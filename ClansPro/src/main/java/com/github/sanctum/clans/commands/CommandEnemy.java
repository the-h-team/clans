package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandEnemy extends ClanSubCommand {
	public CommandEnemy() {
		super("enemy");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy")));
				return true;
			}
			lib.sendMessage(p, lib.commandEnemy());
			return true;
		}

		if (args.length == 1) {
			Bukkit.dispatchCommand(p, "c enemy add " + args[0]);
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("add")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy")));
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
					if (args[1].equals(associate.getClan().getName())) {
						lib.sendMessage(p, lib.allianceDenial());
						return true;
					}
					if (c.getRelation().getRivalry().has(t)) {
						lib.sendMessage(p, lib.alreadyEnemies(args[1]));
						return true;
					}
					List<String> online = new ArrayList<>();
					for (Clan.Associate associate1 : t.getMembers()) {
						if (associate1.getTag().isPlayer() && associate1.getTag().getPlayer().isOnline()) {
							online.add(associate1.getName());
						}
					}
					if (ClansAPI.getDataInstance().isTrue("Clans.relations.enemy.cancel-if-empty")) {
						if (online.isEmpty()) {
							lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
							return true;
						}
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
						c.getRelation().getRivalry().add(t);
						return true;
					}
					if (c.getRelation().getAlliance().has(t)) {
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						if (t.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
							return true;
						}
						c.getRelation().getRivalry().add(t);
						return true;
					}
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("remove")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeenemy"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeenemy")));
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
					Clan t = ClansAPI.getInstance().getClanManager().getClan(HUID.parseID(args[1]).toID());
					if (args[1].equals(c.getName())) {
						lib.sendMessage(p, lib.allianceDenial());
						return true;
					}
					if (t.getRelation().getRivalry().has(c)) {
						lib.sendMessage(p, lib.noRemoval(args[1]));
						return true;
					}
					if (!c.getRelation().getRivalry().has(t)) {
						lib.sendMessage(p, lib.notEnemies(t.getName()));
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
					c.getRelation().getRivalry().remove(t);
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			return true;
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
