package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class ClanJoinEvent extends ClanEventBuilder {

	private final Player joining;

	private final DefaultClan clan;

	public ClanJoinEvent(Player joining, DefaultClan clan) {
		this.joining = joining;
		this.clan = clan;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public Player getJoining() {
		return joining;
	}

	public DefaultClan getClan() {
		return clan;
	}

}
