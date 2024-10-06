package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.gui.unity.simple.AnvilDocket;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandPrompt extends ClanSubCommand {
	public CommandPrompt() {
		super("prompt");
		setUsage("&7|&f) &6{label} &8(*gui)");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.of(args[0]);
			if (search != null) {
				Clan.Associate associate = ClansAPI.getInstance().getAssociate(search.getPlayer()).orElse(null);
				if (associate != null) {
					AnvilDocket docket = new AnvilDocket(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.member.message"));
					docket.setUniqueDataConverter(associate, Clan.Associate.memoryDocketReplacer());
					docket.load();
					docket.toMenu().open(p);
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
