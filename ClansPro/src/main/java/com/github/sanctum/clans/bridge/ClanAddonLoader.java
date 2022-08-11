package com.github.sanctum.clans.bridge;

import com.github.sanctum.panther.util.Deployable;
import java.io.File;
import java.io.IOException;

public interface ClanAddonLoader {

	ClanAddon loadAddon(File jar) throws IOException, InvalidAddonException;

	Deployable<Void> enableAddon(ClanAddon addon);

	Deployable<Void> disableAddon(ClanAddon addon);

}
