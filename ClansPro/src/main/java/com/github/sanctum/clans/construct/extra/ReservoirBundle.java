package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ReservoirBundle {

	static final PantherMap<Entity, Reservoir> map = new PantherEntryMap<>();

	public static final class Check {

		public static boolean isReservoir(@NotNull Entity entity) {
			if (entity instanceof ArmorStand) {
				// TODO: Check for visuals above reservoir via persistent data.
			}
			// handle when they look directly at entity
			return Reservoir.get(entity) != null;
		}

	}

}
