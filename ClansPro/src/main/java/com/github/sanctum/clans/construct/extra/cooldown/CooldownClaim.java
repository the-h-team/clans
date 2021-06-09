package com.github.sanctum.clans.construct.extra.cooldown;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.data.DataManager;

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
		return DataManager.FileType.CLAN_FILE.get(clanId).getConfig().getLong("cooldown.unclaim-limit");
	}

	@Override
	public String fullTimeLeft() {
		return DefaultClan.action.format(DefaultClan.action.format(DefaultClan.action.format(DefaultClan.action.format(ClansAPI.getData().getMessage("cooldown-active"), "%d", String.valueOf(getDaysLeft())), "%h", String.valueOf(getHoursLeft())), "%m", String.valueOf(getMinutesLeft())), "%s", String.valueOf(getSecondsLeft()));
	}

	@Override
	public ClanCooldown getInstance() {
		return this;
	}
}
