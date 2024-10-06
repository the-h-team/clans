package com.github.sanctum.clans.event.associate;

import com.github.sanctum.clans.model.ClansAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Called when a clan associate attempts to update the base location.
 */
public class AssociateUpdateBaseEvent extends AssociateEvent {

	private final Player player;

	private Location location;

	public AssociateUpdateBaseEvent(Player player, Location location) {
		super(ClansAPI.getInstance().getAssociate(player).get(), false);
		this.player = player;
		this.location = location;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
