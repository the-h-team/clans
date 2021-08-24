package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class ClanCreatedEvent extends ClanEventBuilder {

	private final UUID owner;

	private final String name;

	public ClanCreatedEvent(UUID owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	public String getClanName() {
		return name;
	}

	public OfflinePlayer getMaker() {
		return Bukkit.getOfflinePlayer(owner);
	}

	public DefaultClan getClan() {
		return (DefaultClan) ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(name));
	}

}
