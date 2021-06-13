package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class ClanLeaveEvent extends ClanEventBuilder {

	private final Player leaving;

	private final DefaultClan clan;

	public ClanLeaveEvent(Player leaving, DefaultClan clan) {
		this.leaving = leaving;
		this.clan = clan;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public Player getJoining() {
		return leaving;
	}

	public DefaultClan getClan() {
		return clan;
	}

}
