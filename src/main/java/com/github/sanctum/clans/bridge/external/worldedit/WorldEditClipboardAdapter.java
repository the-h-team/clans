package com.github.sanctum.clans.bridge.external.worldedit;

import org.jetbrains.annotations.NotNull;

public interface WorldEditClipboardAdapter {

	/**
	 * Paste all known blocks from the clipboard. (Make sure it's copied first or nothing will happen!)
	 *
	 * @return a new paste operation.
	 */
	@NotNull WorldEditPasteOperation paste();

	/**
	 * Iterate and copy the known selection to the clipboard. In other words
	 * this method will attempt to go over the regions known area and ensure
	 * its data is copied over to this object for pasting.
	 *
	 * @return this very clipboard adapter.
	 */
	@NotNull WorldEditClipboardAdapter copy();

	/**
	 * Iterate and copy the known selection to the clipboard. In other words
	 * this method will attempt to go over the regions known area and ensure
	 * its data is copied over to this object for pasting automatically setting
	 * the provided player's clipboard to this one afterwards
	 *
	 * @param playerAdapter the player to update
	 * @return this very clipboard adapter.
	 */
	@NotNull WorldEditClipboardAdapter copy(@NotNull WorldEditPlayerAdapter playerAdapter);

	/**
	 * Convert this clipboard to a schematic.
	 *
	 * @return a new schematic wrapper object.
	 */
	@NotNull WorldEditSchematicAdapter toSchematic();

	/**
	 * Get this clipboard's region.
	 *
	 * @return the region object for this clipboard containing block information.
	 */
	@NotNull com.github.sanctum.labyrinth.data.essentials.WorldEditRegionAdapter toRegion();

}
