package com.github.sanctum.clans.bridge.external.worldedit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public interface WorldEditRegionAdapter {

	@NotNull Location getCenter(@NotNull Location location);

	@NotNull Block getHighestPoint();

	@NotNull Block getLowestPoint();

	@NotNull WorldEditClipboardAdapter toClipboard();

	/**
	 * Convert this region into a clipboard, using the provided location as the origin point.
	 * This method will ensure the region when pasted is centered to the original point of the player and or block.
	 *
	 * @param origin the location to use.
	 * @return a brand-new clipboard adapter.
	 */
	@NotNull WorldEditClipboardAdapter toClipboard(@NotNull Location origin);

	@NotNull World getWorld();

}
