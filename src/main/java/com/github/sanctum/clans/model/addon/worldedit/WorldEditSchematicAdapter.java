package com.github.sanctum.clans.model.addon.worldedit;

import java.io.File;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface WorldEditSchematicAdapter {

	@NotNull WorldEditClipboardAdapter toClipboard();

	@NotNull WorldEditClipboardAdapter toClipboard(@NotNull Location origin);

	void save(@NotNull File location);

}
