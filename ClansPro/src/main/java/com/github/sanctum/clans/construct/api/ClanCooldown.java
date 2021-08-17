package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class ClanCooldown {

	public abstract String getId();

	public abstract String getAction();

	public abstract void setCooldown();

	public abstract long getCooldown();

	public abstract ClanCooldown getInstance();

	public boolean isComplete() {
		Long a = getCooldown();
		Long b = System.currentTimeMillis();
		int compareNum = a.compareTo(b);
		return compareNum <= 0;
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
		if (!ClansAPI.getData().COOLDOWNS.contains(getInstance())) {
			ClansAPI.getData().COOLDOWNS.add(getInstance());
		}
	}

	/**
	 * Convert's seconds into milliseconds for final time conversions.
	 *
	 * @param seconds The amount of time to convert.
	 * @return The milliseconds needed for conversion.
	 */
	protected long abv(int seconds) {
		return System.currentTimeMillis() + (seconds * 1000);
	}

	protected void abp(String key, int seconds) {
		FileManager clan = DataManager.FileType.CLAN_FILE.get(getId());
		clan.getConfig().set("cooldown." + key, abv(seconds));
		clan.saveConfig();
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
		return ClansAPI.getData().COOLDOWNS.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}

	public static void remove(ClanCooldown c) {
		ClansAPI.getData().COOLDOWNS.removeIf(cooldown -> cooldown.equals(c));
	}


}
