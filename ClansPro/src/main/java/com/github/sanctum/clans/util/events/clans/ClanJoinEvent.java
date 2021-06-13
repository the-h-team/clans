package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
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
