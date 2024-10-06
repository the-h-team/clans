package com.github.sanctum.clans.model.addon.worldedit;

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
