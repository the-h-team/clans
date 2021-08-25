package com.github.sanctum.clans.internal.kingdoms.achievement;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PersistentAchievement implements KingdomAchievement {

	private double progression;

	private boolean complete;

	private final double requirement;

	private final String title;

	private final Set<Player> players;

	private final String description;

	public PersistentAchievement(String title, String description, double progression, double requirement) {
		this.title = title;
		this.players = new HashSet<>();
		this.description = description;
		this.progression = progression;
		this.requirement = requirement;
	}

	@Override
	public @NotNull String getTitle() {
		return this.title;
	}

	@Override
	public @NotNull String getDescription() {
		return this.description;
	}

	@Override
	public double getRequirement() {
		return this.requirement;
	}

	@Override
	public double getProgression() {
		return this.progression;
	}

	@Override
	public double progress(double amount) {
		if (this.progression + amount > this.requirement) {
			return 0;
		}
		return this.progression += amount;
	}

	@Override
	public double unprogress(double amount) {
		double d = Math.max(0, this.progression - amount);
		this.progression = d;
		return d;
	}

	@Override
	public void saveProgress(String path) {
		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");
		FileManager file = cycle.getFile("Achievements", "Data");

		file.getConfig().set(path + "." + getTitle() + ".info", this.getDescription());

		file.getConfig().set(path + "." + getTitle() + ".progression", this.getProgression());

		file.getConfig().set(path + "." + getTitle() + ".requirement", this.getRequirement());

		file.saveConfig();

	}

	@Override
	public void delete(String path) {
		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");
		FileManager file = cycle.getFile("Achievements", "Data");
		file.getConfig().set(path, null);
		file.saveConfig();
	}

	@Override
	public double getPercentage() {
		return Math.round(this.progression * 100 / this.requirement * 100.0) / 100.0;
	}

	@Override
	public boolean activated(Player p) {
		return this.players.contains(p);
	}

	@Override
	public boolean activate(Player p) {
		if (!this.players.contains(p)) {
			return this.players.add(p);
		}
		return false;
	}

	@Override
	public boolean deactivate(Player p) {
		if (this.players.contains(p)) {
			return this.players.remove(p);
		}
		return false;
	}

	@Override
	public boolean isComplete() {

		if (getPercentage() >= 100) {
			if (!this.complete) {
				this.complete = true;
			}
		}

		return this.complete;
	}

}
