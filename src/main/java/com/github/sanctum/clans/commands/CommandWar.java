package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.GUI;
import com.github.sanctum.clans.model.Arena;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.library.TextLib;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CommandWar extends ClanSubCommand {
	public CommandWar() {
		super("war");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.war.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("war")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("war")));
				return true;
			}

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			Arena current = ClansAPI.getInstance().getArenaManager().get(associate);
			if (current == null) {
				Arena joined = ClansAPI.getInstance().getArenaManager().queue(associate);
				if (joined != null) {
					if (!joined.isRunning()) {
						joined.populate();
						lib.sendMessage(p, "&aYou queued in position &7#&6" + joined.getQueue().size() + " &ain team &6" + joined.getTeam(associate.getClan()));
						lib.sendComponent(p, TextLib.getInstance().textRunnable("&eYou can teleport to the arena ", "&6&nnow", "&e, otherwise the war will &6automatically &eteleport you on start when queue reaches the required amount.", "&6&oClick to teleport.", "c war teleport"));
					} else {
						p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 10, 1);
						associate.getClan().broadcast("&a" + associate.getNickname() + " has joined the battle.");
						lib.sendMessage(p, "&aYour team needs you, hurry now!");
						Location loc = joined.getTeam(associate.getClan()).getSpawn();
						ClansAPI.getInstance().getArenaManager().hideAll(joined);
						if (loc != null) {
							p.teleport(loc);
						} else {
							lib.sendMessage(p, "&cYour teams arena spawn isn't setup properly! Contact staff for support.");
						}
					}
				} else {
					lib.sendMessage(p, "&cThere is no space on the battlefield right now.");
				}
			} else {
				lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("already-at-war"));
				if (!current.isRunning()) {
					lib.sendMessage(p, "&cAll teams are still getting ready for battle. Queue still building.");
				}
				GUI.ARENA_SPAWN.get(current).open(p);
			}
			return true;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("teleport")) {
				if (associate != null && associate.isValid()) {
					Arena w = ClansAPI.getInstance().getArenaManager().get(associate);
					if (w != null) {
						if (!w.isRunning()) {
							Arena.Team t = w.getTeam(associate.getClan());
							Location loc = t.getSpawn();
							if (loc == null) {
								lib.sendMessage(p, "&cYour team's spawn location isn't properly setup. Contact staff for support.");
								return true;
							}
							p.teleport(loc);
							lib.sendMessage(p, "&cYou won't be able to hurt anyone until the match starts.");
						} else {
							lib.sendMessage(p, "&cYou can't do this right now! You are already at war.");
						}
					}
				}
				return true;
			}
		}


		return true;
	}
}
