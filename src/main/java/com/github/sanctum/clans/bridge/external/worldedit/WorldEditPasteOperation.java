package com.github.sanctum.clans.bridge.external.worldedit;

import com.github.sanctum.panther.util.Applicable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public interface WorldEditPasteOperation extends Applicable {

	@NotNull WorldEditPasteOperation ignoreAir(boolean ignoreAir);

	@NotNull WorldEditPasteOperation copyEntities(boolean copyEntities);

	@NotNull WorldEditPasteOperation rotateX(double degrees);

	@NotNull WorldEditPasteOperation rotateY(double degrees);

	@NotNull WorldEditPasteOperation rotateZ(double degrees);

	default @NotNull WorldEditPasteOperation toBlock(@NotNull Block block) {
		return toLocation(block.getLocation());
	}

	@NotNull WorldEditPasteOperation toLocation(@NotNull Location location);

}
