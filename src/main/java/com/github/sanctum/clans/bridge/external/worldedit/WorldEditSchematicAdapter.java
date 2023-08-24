package com.github.sanctum.clans.bridge.external.worldedit;

import java.io.File;
import org.jetbrains.annotations.NotNull;

public interface WorldEditSchematicAdapter {

	@NotNull WorldEditClipboardAdapter toClipboard();

	void save(@NotNull File location);

}
