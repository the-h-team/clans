package com.github.sanctum.clans.util;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReservoirRegistry {

	static final PantherMap<Entity, Reservoir> map = new PantherEntryMap<>();

	public static void load(@NotNull Entity entity, @NotNull Reservoir reservoir) {
		map.put(entity, reservoir);
	}

	public static void unload(@NotNull Entity entity) {
		map.remove(entity);
	}

	public static @Nullable Reservoir get(@NotNull Entity entity) {
		return map.get(entity);
	}

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
