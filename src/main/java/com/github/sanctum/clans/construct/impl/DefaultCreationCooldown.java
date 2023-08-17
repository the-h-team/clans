package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanCooldown;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.file.Configurable;
import java.util.UUID;

public class DefaultCreationCooldown extends ClanCooldown {

	private final UUID Id;

	public DefaultCreationCooldown(UUID Id) {
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
	public String getDescriptor() {
		return "A period of time that does not allow group formation.";
	}

	@Override
	public void setCooldown() {
		FileManager config = ClansAPI.getInstance().getFileList().get("cooldowns", "Configuration/Data", Configurable.Type.JSON);
		config.write(t -> t.set("Data." + Id.toString() + "." + getAction().replace("Clans:", "") + ".Time-allotted", System.currentTimeMillis() + (ClansAPI.getDataInstance().getConfigInt("Clans.creation.cooldown.time") * 1000)));
	}

	@Override
	public long getCooldown() {
		FileManager config = ClansAPI.getInstance().getFileList().get("cooldowns", "Configuration/Data", Configurable.Type.JSON);
		return config.getRoot().getLong("Data." + Id.toString() + "." + getAction().replace("Clans:", "") + ".Time-allotted");
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
