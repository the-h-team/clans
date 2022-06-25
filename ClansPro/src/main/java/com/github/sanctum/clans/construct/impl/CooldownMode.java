package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.IllegalTimeFormatException;
import com.github.sanctum.labyrinth.library.ParsedTimeFormat;

public class CooldownMode extends ClanCooldown {

	private final String clanId;

	public CooldownMode(String clanId) {
		this.clanId = clanId;
	}

	@Override
	public String getId() {
		return clanId;
	}

	@Override
	public String getAction() {
		return "Clans:mode-switch";
	}

	@Override
	public void setCooldown() {
		try {
			abp("mode-switch", ParsedTimeFormat.of(ClansAPI.getDataInstance().getConfigString("Clans.mode-change.timer.cooldown")).toSeconds());
		} catch (IllegalTimeFormatException e) {
			abp("mode-switch", 1800);
			ClansAPI.getInstance().getPlugin().getLogger().warning("The new cooldown section for mode change isn't up to date.");
		}
	}

	@Override
	public long getCooldown() {
		Clan c = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(clanId));
		if (c.getMemorySpace().isPresent()) {
			return c.getMemorySpace().get().getNode("cooldown").getNode("mode-switch").toPrimitive().getLong();
		}
		return 0L;
	}

	@Override
	public String fullTimeLeft() {
		return Clan.ACTION.format(Clan.ACTION.format(Clan.ACTION.format(Clan.ACTION.format(ClansAPI.getDataInstance().getMessageResponse("cooldown-active"), "%d", String.valueOf(getDaysLeft())), "%h", String.valueOf(getHoursLeft())), "%m", String.valueOf(getMinutesLeft())), "%s", String.valueOf(getSecondsLeft()));
	}

	@Override
	public ClanCooldown getAttached() {
		return this;
	}
}
