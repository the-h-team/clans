package com.github.sanctum.clans.construct.extra.cooldown;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.data.DataManager;

public class CooldownFriendlyFire extends ClanCooldown {

	private final String clanId;

	public CooldownFriendlyFire(String clanId) {
		this.clanId = clanId;
	}

	@Override
	public String getId() {
		return clanId;
	}

	@Override
	public String getAction() {
		return "Clans:friendly-fire";
	}

	@Override
	public void setCooldown() {
		abp("ff-switch", ClansAPI.getData().getInt("Clans.mode-change.timer.cooldown-in-seconds"));
	}

	@Override
	public long getCooldown() {
		return DataManager.FileType.CLAN_FILE.get(clanId).getConfig().getLong("cooldown.ff-switch");
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
