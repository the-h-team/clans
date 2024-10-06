package com.github.sanctum.clans.model.addon;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.addon.kingdoms.Kingdom;
import com.github.sanctum.clans.model.addon.kingdoms.Progressive;
import com.github.sanctum.clans.model.addon.kingdoms.command.KingdomCommand;
import com.github.sanctum.clans.model.addon.kingdoms.listener.KingdomController;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.file.Configurable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class KingdomAddon extends Clan.Addon {

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon.Kingdoms.enabled") && !LabyrinthProvider.getInstance().isLegacy() && !getApi().isTrial();
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
		getContext().stage(new KingdomCommand(this, "kingdom"));
		getContext().stage(new KingdomController(this));
	}

	@Override
	public void onEnable() {

		FileManager kingdoms = getFile(Configurable.Type.JSON, "kingdoms", "data");
		FileManager data = getFile(Configurable.Type.JSON, "achievements", "data");
		FileManager users = getFile(Configurable.Type.JSON, "users", "data");

		if (kingdoms.getRoot().exists()) {

			if (!kingdoms.getRoot().getKeys(false).isEmpty()) {
				for (String name : kingdoms.getRoot().getKeys(false)) {
					Progressive.register(new Kingdom(name, this));
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

	}
}
