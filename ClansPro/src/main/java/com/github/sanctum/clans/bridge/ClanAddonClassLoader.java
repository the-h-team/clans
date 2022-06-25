package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.AbstractClassLoader;
import java.io.File;
import java.io.IOException;

final class ClanAddonClassLoader extends AbstractClassLoader<ClanAddon> {

	ClanAddonClassLoader(File file) throws IOException, InvalidAddonException {
		super(file, ClansAPI.class.getClassLoader());
		if (getMainClass() == null) throw new InvalidAddonException("Processed jar not a clans addon!");
	}

	ClanAddonClassLoader(File file, ClanAddon parent) throws IOException, InvalidAddonException {
		super(file, parent.getClassLoader());
		if (getMainClass() == null) throw new InvalidAddonException("Processed jar not a clans addon!");
	}

	@Override
	public String toString() {
		return "ClanAddonClassLoader{" +
				"addon=" + getMainClass() +
				'}';
	}
}
