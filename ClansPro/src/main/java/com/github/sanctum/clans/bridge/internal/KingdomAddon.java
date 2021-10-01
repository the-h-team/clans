package com.github.sanctum.clans.bridge.internal;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Progressive;
import com.github.sanctum.clans.bridge.internal.kingdoms.RoundTable;
import com.github.sanctum.clans.bridge.internal.kingdoms.command.KingdomCommand;
import com.github.sanctum.clans.bridge.internal.kingdoms.listener.KingdomController;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.library.HUID;
import java.io.IOException;

public class KingdomAddon extends ClanAddon {


	@Override
	public boolean isStaged() {
		return ClansAPI.getData().isTrue("Addon.Kingdoms.enabled");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Kingdoms";
	}

	@Override
	public String getDescription() {
		return "An addon that adds quests for " + '"' + "clan progression" + '"' + " allowing users to get rewarded for playing the game.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {

		getLogger().info("- Attention all passengers, let the games begin :)");
		getContext().stage(new KingdomCommand(this, "kingdom"));
		getContext().stage(new KingdomController(this));

	}

	public static RoundTable getRoundTable() {
		return Progressive.getProgressives().stream().filter(p -> p instanceof RoundTable).map(p -> (RoundTable) p).findFirst().orElse(null);
	}

	@Override
	public void onEnable() {

		new RoundTable(this);

		FileManager kingdoms = getFile(FileType.JSON, "kingdoms", "data");
		FileManager data = getFile(FileType.JSON, "achievements", "data");
		FileManager users = getFile(FileType.JSON, "users", "data");

		if (kingdoms.getRoot().exists()) {

			if (!kingdoms.getRoot().getKeys(false).isEmpty()) {
				for (String name : kingdoms.getRoot().getKeys(false)) {
					new Kingdom(name, this);
				}
			}

		} else {
			try {
				kingdoms.getRoot().create();
				data.getRoot().create();
				users.getRoot().create();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void onDisable() {

		for (Progressive progressable : Progressive.getProgressives()) {
			progressable.save(this);
		}

		getLogger().info("- Adios amigos :D");

	}
}
