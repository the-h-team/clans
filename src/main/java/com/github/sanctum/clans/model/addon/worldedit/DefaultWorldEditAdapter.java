package com.github.sanctum.clans.model.addon.worldedit;

import com.github.sanctum.labyrinth.api.Service;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultWorldEditAdapter implements WorldEditAdapter, Service {
	@Override
	public @NotNull WorldEditClipboardAdapter newClipboard(@NotNull Location loc1, @NotNull Location loc2) {
		return null;
	}

	@Override
	public @NotNull WorldEditPlayerAdapter getPlayer(@NotNull Player player) {
		return null;
	}

	@Override
	public @Nullable WorldEditSchematicAdapter loadSchematic(@NotNull File file) {
		return null;
	}
}
