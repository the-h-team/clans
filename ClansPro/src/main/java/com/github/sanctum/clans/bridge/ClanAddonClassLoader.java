package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.LabyrinthClassLoader;
import java.io.File;
import java.io.IOException;

final class ClanAddonClassLoader extends LabyrinthClassLoader<ClanAddon> {

	ClanAddonClassLoader(File file) throws IOException, InvalidAddonException {
		super(ClansAPI.getInstance().getPlugin(), file, ClansAPI.class.getClassLoader());
		if (getMainClass() == null) throw new InvalidAddonException("Processed jar not a clans addon!");
	}

	ClanAddonClassLoader(File file, ClanAddon parent) throws IOException, InvalidAddonException {
		super(ClansAPI.getInstance().getPlugin(), file, parent.getClassLoader());
		if (getMainClass() == null) throw new InvalidAddonException("Processed jar not a clans addon!");
	}

	@Override
	public String toString() {
		return "ClanAddonClassLoader{" +
				"addon=" + getMainClass() +
				'}';
	}
}
