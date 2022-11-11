package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class CommandHelp extends ClanSubCommand {
	public CommandHelp() {
		super("help");
		setAliases(Collections.singletonList("?"));
	}

	private List<String> helpMenu(String label) {
		List<String> help = new ArrayList<>();
		FileManager msg = ClansAPI.getInstance().getFileList().get("Messages", "Configuration");
		for (String s : msg.getRoot().getNode("Commands").getKeys(false)) {
			help.add(msg.getRoot().getString("Commands." + s + ".text"));
		}
		CommandInformationAdaptEvent e = ClanVentBus.call(new CommandInformationAdaptEvent(help));
		return e.getMenu().stream().map(s -> new FormattedString(s).replace("{label}", "/" + label).color().get()).collect(Collectors.toList());
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			List<String> list = new LinkedList<>(helpMenu(getLastLabel()));
			EasyPagination<String> pagination = new EasyPagination<>(p, list);
			pagination.limit(lib.menuSize());
			pagination.setHeader((player, chunks) -> chunks.then(lib.menuBorder()));
			pagination.setFormat((s, integer, chunks) -> chunks.then(s));
			pagination.setFooter((player, chunks) -> chunks.then(lib.menuBorder()));
			lib.sendMessage(p, new FormattedString(lib.menuTitle()).replace("{label}", "/" + getLastLabel()).get());
			pagination.send(1);
		}

		if (args.length == 1) {
			if (StringUtils.use(args[0]).isInt()) {
				int pa = Integer.parseInt(args[0]);
				List<String> list = new LinkedList<>(helpMenu(getLastLabel()));
				EasyPagination<String> pagination = new EasyPagination<>(p, list);
				pagination.limit(lib.menuSize());
				pagination.setHeader((player, chunks) -> chunks.then(lib.menuBorder()));
				pagination.setFormat((s, integer, chunks) -> chunks.then(s));
				pagination.setFooter((player, chunks) -> chunks.then(lib.menuBorder()));
				lib.sendMessage(p, new FormattedString(lib.menuTitle()).replace("{label}", "/" + getLastLabel()).get());
				pagination.send(Math.min(Math.max(1, pa), pagination.size()));
			} else {
				lib.sendMessage(p, lib.pageUnknown());
				return true;
			}
			return true;
		}


		return true;
	}
}
