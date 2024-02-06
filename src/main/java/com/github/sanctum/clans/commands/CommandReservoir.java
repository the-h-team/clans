package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.util.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandReservoir extends ClanSubCommand {
	public CommandReservoir() {
		super("reservoir");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			if (associate != null) {
				GUI.RESERVOIR.get(associate.getClan()).open(p);
			} else {
				sendMessage(p, lib.notInClan());
			}
			return true;
		}
		return true;
	}

	@Override
	public List<String> tab(Player p, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.get();
	}
}
