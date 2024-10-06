package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.interfacing.UnknownGeneric;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandRelations extends ClanSubCommand {
	public CommandRelations() {
		super("relations");
		setUsage("&7|&f) &6{label} &8(*gui)");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;

		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			lib.sendMessage(p, lib.notInClan());
			return true;
		}

		final FileManager messages = ClansAPI.getDataInstance().getMessages();

		if (args.length == 0) {
			MemoryDocket<UnknownGeneric> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations")));
			docket.setUniqueDataConverter(p, (s, player) -> new FormattedString(s).translate(player).get());
			docket.setNamePlaceholder("%player_name%");
			docket.load();
			docket.toMenu().open(p);
			return true;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("allies")) {
				MemoryDocket<Clan> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations.ally-list")));
				docket.setNamePlaceholder("%player_name%");
				docket.setDataConverter(Clan.memoryDocketReplacer());
				docket.setList(() -> associate.getClan().getRelation().getAlliance().get(Clan.class));
				docket.load();
				docket.toMenu().open(p);
			}
			if (args[0].equalsIgnoreCase("enemies")) {
				MemoryDocket<Clan> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations.enemy-list")));
				docket.setNamePlaceholder("%player_name%");
				docket.setDataConverter(Clan.memoryDocketReplacer());
				docket.setList(() -> associate.getClan().getRelation().getRivalry().get(Clan.class));
				docket.load();
				docket.toMenu().open(p);
			}
			return true;
		}

		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("add")) {
				if (args[0].equalsIgnoreCase("allies")) {
					MemoryDocket<Clan> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations.ally-add")));
					docket.setNamePlaceholder("%player_name%");
					docket.setDataConverter(Clan.memoryDocketReplacer());
					docket.setList(() -> ClansAPI.getInstance().getClanManager().getClans().stream().filter(c -> c != associate.getClan() && associate.getClan().getRelation().isNeutral(c)).collect(Collectors.toList()));
					docket.load();
					docket.toMenu().open(p);
				}
				if (args[0].equalsIgnoreCase("enemies")) {
					MemoryDocket<Clan> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations.enemy-add")));
					docket.setNamePlaceholder("%player_name%");
					docket.setDataConverter(Clan.memoryDocketReplacer());
					docket.setList(() -> ClansAPI.getInstance().getClanManager().getClans().stream().filter(c -> c != associate.getClan() && (associate.getClan().getRelation().isNeutral(c) || associate.getClan().getRelation().getAlliance().has(c))).collect(Collectors.toList()));
					docket.load();
					docket.toMenu().open(p);
				}
			} else {
				if (args[0].equalsIgnoreCase("allies")) {
					MemoryDocket<Clan> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations.ally-remove")));
					docket.setNamePlaceholder("%player_name%");
					docket.setDataConverter(Clan.memoryDocketReplacer());
					docket.setList(() -> associate.getClan().getRelation().getAlliance().get(Clan.class));
					docket.load();
					docket.toMenu().open(p);
				}
				if (args[0].equalsIgnoreCase("enemies")) {
					MemoryDocket<Clan> docket = new MemoryDocket<>(messages.read(c -> c.getNode("menu.home.relations.enemy-remove")));
					docket.setNamePlaceholder("%player_name%");
					docket.setDataConverter(Clan.memoryDocketReplacer());
					docket.setList(() -> associate.getClan().getRelation().getRivalry().get(Clan.class));
					docket.load();
					docket.toMenu().open(p);
				}
			}
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}
}
