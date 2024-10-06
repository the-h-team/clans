package com.github.sanctum.clans.model;

import com.github.sanctum.panther.util.Deployable;
import java.io.File;
import java.io.IOException;

public interface ClanAddonLoader {

	Clan.Addon loadAddon(File jar) throws IOException, InvalidAddonException;

	Deployable<Void> enableAddon(Clan.Addon addon);

	Deployable<Void> disableAddon(Clan.Addon addon);

}
