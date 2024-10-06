package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.util.Book;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandAsk extends ClanSubCommand {
	public CommandAsk() {
		super("ask");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {

		if (args.length == 0) {
			Book book = new Book(1, false).setTitle("&7[&3&lClans&7] &6&lInfo").setAuthor("Hempfest");
			FileManager config = ClansAPI.getDataInstance().getMessages();
			List<String> appendage = config.getRoot().getStringList("help-book");
			appendage.forEach(book::append);
			book.give(p);
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return null;
	}
}
