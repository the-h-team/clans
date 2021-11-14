package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.ClanEvent;

/**
 * Called repeatedly while the raid shield is being used
 */
public class RaidShieldEvent extends ClanEvent {

	private int on = ClansAPI.getDataInstance().getConfig().getRoot().getInt("Clans.raid-shield.up-time");

	private int off = ClansAPI.getDataInstance().getConfig().getRoot().getInt("Clans.raid-shield.down-time");

	private String shieldOn = "{0} &a&lRAID SHIELD ENABLED";

	private String shieldOff = "{0} &c&lRAID SHIELD DISABLED";

	public RaidShieldEvent() {
		super(false);
	}

	@Override
	public Clan getClan() {
		return null;
	}

	public int getStartTime() {
		return on;
	}

	public int getStopTime() {
		return off;
	}

	public String getShieldOn() {
		return shieldOn;
	}

	public String getShieldOff() {
		return shieldOff;
	}

	public boolean shieldOn() {
		return ClansAPI.getInstance().getShieldManager().isEnabled();
	}

	public void setShieldOn(String shieldOn) {
		this.shieldOn = shieldOn;
	}

	public void setShieldOff(String shieldOff) {
		this.shieldOff = shieldOff;
	}

	public void setStartTime(int i) {
		this.on = i;
	}

	public void setStopTime(int i) {
		this.off = i;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
