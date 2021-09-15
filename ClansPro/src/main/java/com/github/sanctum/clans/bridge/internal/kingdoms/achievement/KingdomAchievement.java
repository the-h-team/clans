package com.github.sanctum.clans.bridge.internal.kingdoms.achievement;

import com.github.sanctum.clans.bridge.internal.kingdoms.Progressable;
import com.github.sanctum.clans.bridge.internal.kingdoms.Reward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface KingdomAchievement {

	@NotNull String getTitle();

	@NotNull String getDescription();

	@Nullable Progressable getParent();

	double getRequirement();

	double getProgression();

	Reward<?> getReward();

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

	void setParent(Progressable k);

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
