package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.GUI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandGui extends ClanSubCommand {
	public CommandGui() {
		super("gui");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.of(args[0]);
			if (search != null) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(search.getPlayer()).orElse(null);
				if (associate != null) {
					GUI.MEMBER_INFO.get(associate).open(p);
				}
			}
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}

}
