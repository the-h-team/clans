package com.github.sanctum.clans.internal.kingdoms.achievement;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface KingdomAchievement {

	@NotNull String getTitle();

	@NotNull String getDescription();

	double getRequirement();

	double getProgression();

	double progress(double amount);

	double unprogress(double amount);

	void saveProgress(String path);

	void delete(String path);

	double getPercentage();

	boolean activated(Player p);

	boolean activate(Player p);

	boolean deactivate(Player p);

	boolean isComplete();

	default double limit(int add) {
		if (getProgression() + add > getRequirement()) {
			return getRequirement() - (getProgression() + add);
		}
		return add;
	}

	static KingdomAchievement newInstance(String name, String description, double a, double b) {
		return new PersistentAchievement(name, description, a, b);
	}

}
