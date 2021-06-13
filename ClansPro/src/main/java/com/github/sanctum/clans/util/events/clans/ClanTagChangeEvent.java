package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class ClanTagChangeEvent extends ClanEventBuilder implements Cancellable {

	private final Player changer;

	private final Clan clan;

	private String toName;

	private final String fromName;

	public ClanTagChangeEvent(Player changer, Clan clan, String fromName, String toName) {
		this.changer = changer;
		this.fromName = fromName;
		this.toName = toName;
		this.clan = clan;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getFromName() {
		return fromName;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public Player getNameChanger() {
		return changer;
	}

	public Clan getClan() {
		return clan;
	}

}
