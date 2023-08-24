package com.github.sanctum.clans.bridge.external.worldedit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldEditPlayerAdapter {

	@Nullable WorldEditClipboardAdapter getClipboard();

	@Nullable WorldEditRegionAdapter getSelection();

	void setClipboard(@NotNull WorldEditClipboardAdapter clipboard);

}
