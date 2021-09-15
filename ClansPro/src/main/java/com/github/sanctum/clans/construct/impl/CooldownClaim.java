package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;

public class CooldownClaim extends ClanCooldown {

	private final String clanId;

	public CooldownClaim(String clanId) {
		this.clanId = clanId;
	}

	@Override
	public String getId() {
		return clanId;
	}

	@Override
	public String getAction() {
		return "Clans:unclaim-limit";
	}

	@Override
	public void setCooldown() {
		abp("unclaim-limit", ClansAPI.getData().getInt("Clans.land-claiming.over-powering.cooldown.length"));
	}

	@Override
	public long getCooldown() {
		return DataManager.FileType.CLAN_FILE.get(clanId).getRoot().getLong("cooldown.unclaim-limit");
	}

	@Override
	public String fullTimeLeft() {
		return Clan.ACTION.format(Clan.ACTION.format(Clan.ACTION.format(Clan.ACTION.format(ClansAPI.getData().getMessageResponse("cooldown-active"), "%d", String.valueOf(getDaysLeft())), "%h", String.valueOf(getHoursLeft())), "%m", String.valueOf(getMinutesLeft())), "%s", String.valueOf(getSecondsLeft()));
	}

	@Override
	public ClanCooldown getInstance() {
		return this;
	}
}
