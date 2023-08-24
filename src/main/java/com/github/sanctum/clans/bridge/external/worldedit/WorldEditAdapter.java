package com.github.sanctum.clans.bridge.external.worldedit;

import com.github.sanctum.panther.recursive.ServiceFactory;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldEditAdapter {

	@NotNull WorldEditClipboardAdapter newClipboard(@NotNull Location loc1, @NotNull Location loc2);

	@NotNull WorldEditPlayerAdapter getPlayer(@NotNull Player player);

	@Nullable WorldEditSchematicAdapter loadSchematic(@NotNull File file);

	static WorldEditAdapter getInstance() {
		return ServiceFactory.getInstance().getService(WorldEditAdapter.class);
	}

}
