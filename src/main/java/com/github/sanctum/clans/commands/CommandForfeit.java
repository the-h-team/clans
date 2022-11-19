package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.library.Cooldown;
import java.util.Collections;
import org.bukkit.entity.Player;

public class CommandForfeit extends ClanSubCommand {
	public CommandForfeit() {
		super("forfeit");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.forfeit.text"));
		setAliases(Collections.singletonList("surrender"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("forfeit")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("forfeit")));
				return true;
			}
			if (associate != null) {
				War current = ClansAPI.getInstance().getArenaManager().get(associate);
				if (current != null && current.isRunning()) {
					GUI.ARENA_SURRENDER.get(current).open(p);
				} else {
					if (current != null) {
						if (current.getQueue().getAssociates().length == 1) {
							Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown("war-" + current.getId() + "-start");
							if (test != null) {
								current.stop();
								current.reset();
								LabyrinthProvider.getInstance().remove(test);
							} else {
								ClansAPI.getInstance().getArenaManager().leave(associate);
							}
							return true;
						} else {
							ClansAPI.getInstance().getArenaManager().leave(associate);
						}
					}
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}
			return true;
		}


		return true;
	}
}
