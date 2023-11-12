package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.ParsedTimeFormat;

public class DefaultModeSwitchCooldown extends ClanCooldown {

	private final String clanId;

	public DefaultModeSwitchCooldown(String clanId) {
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
	public String getDescriptor() {
		return "A period of time where one cannot toggle their clans pvp mode.";
	}

	@Override
	public void setCooldown() {
		ParsedTimeFormat timeFormat = ParsedTimeFormat.of(ClansAPI.getDataInstance().getConfigString("Clans.mode-change.timer.cooldown"));
		if (timeFormat != null) {
			abp("mode-switch", timeFormat.toSeconds());
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
}
