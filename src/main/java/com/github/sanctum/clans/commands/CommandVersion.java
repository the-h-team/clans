package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.util.StringLibrary;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public class CommandVersion extends ClanSubCommand {
	public CommandVersion() {
		super("version");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (args.length == 0) {
			CompletableFuture.runAsync(() -> {

				p.sendMessage(" ");
				String info = ClansAPI.getInstance().isUpdated() ? "is up to date." : "needs updated.";
				p.sendMessage(StringUtils.use("&b&oThis server is using pro version &r" + ClansAPI.getInstance().getPlugin().getDescription().getVersion() + " &b&owith &6Labyrinth &b&oversion &r" + LabyrinthProvider.getInstance().getPluginInstance().getDescription().getVersion() + " &b&oand &e" + info).translate());
				p.sendMessage(" ");

			}).join();

			return true;
		}


		return true;
	}
}
