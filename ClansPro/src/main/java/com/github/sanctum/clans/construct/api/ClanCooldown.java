package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class ClanCooldown {

	boolean markedForRemoval;

	public abstract String getId();

	public abstract String getAction();

	public abstract void setCooldown();

	public abstract long getCooldown();

	public ClanCooldown getAttached() {
		return this;
	}

	public boolean isComplete() {
		Long a = getCooldown();
		Long b = System.currentTimeMillis();
		int compareNum = a.compareTo(b);
		return compareNum <= 0;
	}

	public boolean isMarkedForRemoval() {
		return markedForRemoval;
	}

	public void setMarkedForRemoval(boolean markedForRemoval) {
		this.markedForRemoval = markedForRemoval;
	}

	protected long getTimePassed() {
		return (System.currentTimeMillis() - getCooldown()) / 1000;
	}

	protected int getTimeLeft() {
		return Integer.parseInt(String.valueOf(getTimePassed()).replace("-", ""));
	}

	public int getDaysLeft() {
		return (int) TimeUnit.SECONDS.toDays(getTimeLeft());
	}

	public long getHoursLeft() {
		return TimeUnit.SECONDS.toHours(getTimeLeft()) - (getDaysLeft() * 24);
	}

	public long getMinutesLeft() {
		return TimeUnit.SECONDS.toMinutes(getTimeLeft()) - (TimeUnit.SECONDS.toHours(getTimeLeft()) * 60);
	}

	public long getSecondsLeft() {
		return TimeUnit.SECONDS.toSeconds(getTimeLeft()) - (TimeUnit.SECONDS.toMinutes(getTimeLeft()) * 60);
	}

	public String fullTimeLeft() {
		return "(S)" + getSecondsLeft() + " : (M)" + getMinutesLeft() + " : (H)" + getHoursLeft() + " : (D)" + getDaysLeft();
	}

	public void save() {
		if (!ClansAPI.getDataInstance().getCooldowns().contains(this)) {
			ClansAPI.getDataInstance().addCooldown(this);
		}
	}

	/**
	 * Convert's seconds into milliseconds for final time conversions.
	 *
	 * @param seconds The amount of time to convert.
	 * @return The milliseconds needed for conversion.
	 */
	protected long abv(long seconds) {
		return System.currentTimeMillis() + (seconds * 1000);
	}

	protected void abp(String key, long seconds) {
		FileManager clan = ClansAPI.getDataInstance().getClanFile(ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(getId())));
		clan.write(t -> t.set("cooldown." + key, abv(seconds)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClanCooldown)) return false;
		ClanCooldown clanCooldown = (ClanCooldown) o;
		return Objects.equals(getAction(), clanCooldown.getAction());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	public static ClanCooldown getById(String id) {
		return ClansAPI.getDataInstance().getCooldowns().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}

	public static void remove(ClanCooldown c) {
		ClansAPI.getDataInstance().removeCooldown(c);
	}


}
