package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;

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
		abp("mode-switch", ClansAPI.getData().getInt("Clans.mode-change.timer.cooldown-in-seconds"));
	}

	@Override
	public long getCooldown() {
		return DataManager.FileType.CLAN_FILE.get(clanId).getConfig().getLong("cooldown.mode-switch");
	}

	@Override
	public String fullTimeLeft() {
		return Clan.ACTION.format(Clan.ACTION.format(Clan.ACTION.format(Clan.ACTION.format(ClansAPI.getData().getMessage("cooldown-active"), "%d", String.valueOf(getDaysLeft())), "%h", String.valueOf(getHoursLeft())), "%m", String.valueOf(getMinutesLeft())), "%s", String.valueOf(getSecondsLeft()));
	}

	@Override
	public ClanCooldown getInstance() {
		return this;
	}
}
