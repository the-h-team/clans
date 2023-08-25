package com.github.sanctum.clans.bridge.external.worldedit;

import com.github.sanctum.panther.util.Applicable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapping interface for a worldedit clipboard paste operation.
 */
public interface WorldEditPasteOperation extends Applicable {

	/**
	 * @param ignoreAir
	 * @return
	 */
	@NotNull WorldEditPasteOperation ignoreAir(boolean ignoreAir);

	/**
	 * @param copyEntities
	 * @return
	 */
	@NotNull WorldEditPasteOperation copyEntities(boolean copyEntities);

	/**
	 * @param degrees
	 * @return
	 */
	@NotNull WorldEditPasteOperation rotateX(double degrees);

	/**
	 * @param degrees
	 * @return
	 */
	@NotNull WorldEditPasteOperation rotateY(double degrees);

	/**
	 * @param degrees
	 * @return
	 */
	@NotNull WorldEditPasteOperation rotateZ(double degrees);

	/**
	 * @param block
	 * @return
	 */
	default @NotNull WorldEditPasteOperation toBlock(@NotNull Block block) {
		return toLocation(block.getLocation());
	}

	/**
	 * @param location
	 * @return
	 */
	@NotNull WorldEditPasteOperation toLocation(@NotNull Location location);

}
