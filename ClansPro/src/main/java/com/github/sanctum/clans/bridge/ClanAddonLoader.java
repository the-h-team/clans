package com.github.sanctum.clans.bridge;

import com.github.sanctum.labyrinth.library.Deployable;
import java.io.File;

public interface ClanAddonLoader {

	Class<?> forName(String name);

	ClanAddon loadAddon(File jar);

	Deployable<Void> loadJar(File jar);

	Deployable<Void> loadFolder(File folder);


}
