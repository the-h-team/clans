package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.LogoHolder;
import org.bukkit.entity.Player;

/**
 * Called when a players cursor moves over the hitbox of a logo carrier
 */
public class PlayerLookAtCarrierEvent extends PlayerEvent {
	private String id;
	private int despawn;
	private final LogoHolder.Carrier logo;

	public PlayerLookAtCarrierEvent(Player player, LogoHolder.Carrier logo, String id, int despawnTime) {
		super(player.getUniqueId(), false);
		this.logo = logo;
		this.despawn = despawnTime;
		this.id = id;
	}

	public String getTitle() {
		return id;
	}

	public int getDespawn() {
		return despawn;
	}

	public LogoHolder.Carrier getCarrier() {
		return logo;
	}

	@Override
	public Clan getClan() {
		return getApi().getAssociate(getPlayer()).map(Clan.Associate::getClan).orElse(null);
	}

	public void setDespawn(int despawn) {
		this.despawn = despawn;
	}

	public void setTitle(String title) {
		this.id = title;
	}

}
