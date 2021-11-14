package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.labyrinth.data.service.Check;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * An object container for either a {@link Player} or {@link Location}
 */
public class TeleportationTarget {

	private final Object target;

	public TeleportationTarget(Object target) throws IllegalArgumentException {
		this.target = target;
		boolean test = false;
		if (Location.class.isAssignableFrom(target.getClass())) test = true;
		if (Player.class.isAssignableFrom(target.getClass())) test = true;
		Check.argument(test, "Teleportation target invalid! Expected: [Player, Location] Got: [" + target.getClass().getSimpleName() + "]");
	}

	public boolean isPlayer() {
		return target instanceof Player;
	}

	public boolean isLocation() {
		return target instanceof Location;
	}

	public Location getAsLocation() {
		return (Location) target;
	}

	public Player getAsPlayer() {
		return (Player) target;
	}

}
