package com.github.sanctum.clans.event.player;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanCooldown;
import org.bukkit.entity.Player;

/**
 * Called when a player cooldown expires.
 */
public class PlayerCooldownCompleteEvent extends PlayerEvent {

	private final ClanCooldown clanCooldown;

	public PlayerCooldownCompleteEvent(Player player, ClanCooldown cooldown) {
		super(player.getUniqueId(), State.IMMUTABLE, false);
		this.clanCooldown = cooldown;
	}

	public ClanCooldown getCooldown() {
		return clanCooldown;
	}

	@Override
	public Clan getClan() {
		return getApi().getAssociate(getPlayer()).map(Clan.Associate::getClan).orElse(null);
	}
}
