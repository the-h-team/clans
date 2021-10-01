package com.github.sanctum.clans.bridge.internal.kingdoms;

import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Quest {

	@NotNull String getTitle();

	@NotNull String getDescription();

	@Nullable Progressive getParent();

	double getRequirement();

	double getProgression();

	Reward<?> getReward();

	Set<Player> getActiveUsers();

	double progress(double amount);

	double unprogress(double amount);

	void saveProgress(String path);

	void delete(String path);

	double getPercentage();

	boolean activated(Player p);

	boolean activate(Player p);

	boolean deactivate(Player p);

	boolean isComplete();

	void setReward(Reward<?> type, Object reward);

	void setParent(Progressive k);

	default double limit(int add) {
		if (getProgression() + add > getRequirement()) {
			return getRequirement() - (getProgression() + add);
		}
		return add;
	}

	static Quest newQuest(String name, String description, double initialProgress, double requirement) {
		return new LocalFileQuest(name, description, initialProgress, requirement);
	}

}
