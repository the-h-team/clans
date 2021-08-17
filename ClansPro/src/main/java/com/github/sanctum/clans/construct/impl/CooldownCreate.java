package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.UUID;

public class CooldownCreate extends ClanCooldown {

	private final UUID Id;

	public CooldownCreate(UUID Id) {
		this.Id = Id;
	}

	@Override
	public String getId() {
		return Id.toString();
	}

	@Override
	public String getAction() {
		return "Clans:create-limit";
	}

	@Override
	public void setCooldown() {
		FileManager config = ClansAPI.getInstance().getFileList().find("Cooldowns", "Configuration");
		config.getConfig().set("Data." + getAction().replace("Clans:", "") + ".Time-allotted", System.currentTimeMillis() + (ClansAPI.getData().getInt("Clans.creation.cooldown.time") * 1000));
		config.saveConfig();
	}

	@Override
	public long getCooldown() {
		FileManager config = ClansAPI.getInstance().getFileList().find("Cooldowns", "Configuration");
		return config.getConfig().getLong("Data." + getAction().replace("Clans:", "") + ".Time-allotted");
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
