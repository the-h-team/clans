package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.panther.util.HUID;

public class DefaultFriendlyFireCooldown extends ClanCooldown {

	private final String clanId;

	public DefaultFriendlyFireCooldown(String clanId) {
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
	public String getDescriptor() {
		return "A time frame where one cannot toggle the friendly fire mode.";
	}

	@Override
	public void setCooldown() {
		abp("ff-switch", ClansAPI.getDataInstance().getConfigInt("Clans.mode-change.timer.cooldown-in-seconds"));
	}

	@Override
	public long getCooldown() {
		Clan c = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(clanId));
		if (c.getMemorySpace().isPresent()) {
			return c.getMemorySpace().get().getNode("cooldown").getNode("ff-switch").toPrimitive().getLong();
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
