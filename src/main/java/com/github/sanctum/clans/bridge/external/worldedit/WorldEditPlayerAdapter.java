package com.github.sanctum.clans.bridge.external.worldedit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public interface WorldEditPlayerAdapter {

	/**
	 * @return
	 */
	@Nullable WorldEditClipboardAdapter getClipboard();

	/**
	 * @return
	 */
	@Nullable WorldEditRegionAdapter getSelection();

	/**
	 * @param clipboard
	 */
	void setClipboard(@NotNull WorldEditClipboardAdapter clipboard);

}
