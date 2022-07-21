package com.github.sanctum.clans.bridge.internal;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Progressive;
import com.github.sanctum.clans.bridge.internal.kingdoms.RoundTable;
import com.github.sanctum.clans.bridge.internal.kingdoms.command.KingdomCommand;
import com.github.sanctum.clans.bridge.internal.kingdoms.listener.KingdomController;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.file.Configurable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class KingdomAddon extends ClanAddon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon.Kingdoms.enabled") && !LabyrinthProvider.getInstance().isLegacy();
	}

	@Override
	public @NotNull String getName() {
		return "Kingdoms";
	}

	@Override
	public @NotNull String getDescription() {
		return "An addon that adds quests for " + '"' + "clan progression" + '"' + " allowing users to get rewarded for playing the game.";
	}

	@Override
	public @NotNull String getVersion() {
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

		Progressive.capture(new RoundTable(this));

		FileManager kingdoms = getFile(Configurable.Type.JSON, "kingdoms", "data");
		FileManager data = getFile(Configurable.Type.JSON, "achievements", "data");
		FileManager users = getFile(Configurable.Type.JSON, "users", "data");

		if (kingdoms.getRoot().exists()) {

			if (!kingdoms.getRoot().getKeys(false).isEmpty()) {
				for (String name : kingdoms.getRoot().getKeys(false)) {
					Progressive.capture(new Kingdom(name, this));
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
