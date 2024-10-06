package com.github.sanctum.clans.model.addon.map.command;

import com.github.sanctum.clans.model.addon.map.MapController;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand extends ClanSubCommand {
	public MapCommand(String label) {
		super(label);
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		final int length = args.length;
		if (length == 0) {
			MapController.sendMapCurrentLoc(p);
		} else {
			if (args[0].equalsIgnoreCase("on")) {
				// on logic
				if (!MapController.isToggled(p)) {
					Clan.ACTION.sendMessage(p, "&aMap enabled.");
					MapController.sendMapCurrentLoc(p);
					MapController.toggle(p);
				}
			} else if (args[0].equalsIgnoreCase("off")) {
				// off logic
				if (MapController.isToggled(p)) {
					Clan.ACTION.sendMessage(p, "&cMap disabled.");
					MapController.toggle(p);
				}
			} else {
				// receive usage
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		if (args.length == 1) {
			return getBaseCompletion(args);
		}
		return null;
	}
}
