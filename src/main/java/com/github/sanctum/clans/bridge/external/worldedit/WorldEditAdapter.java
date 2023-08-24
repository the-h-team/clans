package com.github.sanctum.clans.bridge.external.worldedit;

import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An adapting interface that encapsulates basic worldedit functionality
 */
@Service.Tag("WorldEdit")
public interface WorldEditAdapter {

	/**
	 * Create a new clipboard using the provided points, fresh clipboards need copied. Use the {@link WorldEditClipboardAdapter#copy()} method.
	 *
	 * @param loc1 The max point of the region.
	 * @param loc2 The min point of the region.
	 * @return A brand-new clipboard instance containing the provided parameters
	 */
	@NotNull WorldEditClipboardAdapter newClipboard(@NotNull Location loc1, @NotNull Location loc2);

	/**
	 * Get a world edit player object for handling selections and clipboards.
	 *
	 * @param player The player to use
	 * @return a world edit player.
	 */
	@NotNull WorldEditPlayerAdapter getPlayer(@NotNull Player player);

	/**
	 * Load a saved schematic from file for further manipulation.
	 *
	 * @param file The file location of the schematic
	 * @return A schematic file if one exists.
	 */
	@Nullable WorldEditSchematicAdapter loadSchematic(@NotNull File file);

	/**
	 * @return true if worldedit interface has an available hook.
	 */
	default boolean isValid() {
		return !(this instanceof DefaultWorldEditAdapter);
	}

	static WorldEditAdapter getInstance() {
		return ServiceFactory.getInstance().getService(WorldEditAdapter.class);
	}

}
