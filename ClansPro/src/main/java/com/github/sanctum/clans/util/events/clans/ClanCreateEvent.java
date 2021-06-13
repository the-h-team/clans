package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;

public class ClanCreateEvent extends ClanEventBuilder implements Cancellable {

	private final UUID owner;

	private final String name;

	private final String password;

	public ClanCreateEvent(UUID owner, String name, String password) {
		this.owner = owner;
		this.name = name;
		this.password = password;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public OfflinePlayer getMaker() {
		return Bukkit.getOfflinePlayer(owner);
	}

	public String getClanName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

}
