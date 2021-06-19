package com.github.sanctum.kingdoms;

import com.github.sanctum.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.link.EventCycle;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Progressable {

	protected static final List<Progressable> PROGRESSABLES = new LinkedList<>();

	abstract public @NotNull String getName();

	abstract public int getLevel();

	abstract public @NotNull List<KingdomAchievement> getAchievements();

	abstract public @Nullable KingdomAchievement getAchievement(String title);

	abstract public boolean loadAchievement(KingdomAchievement achievement);

	abstract public void loadAchievement(KingdomAchievement... achievement);

	abstract public void save(EventCycle cycle);

	public static List<Progressable> getProgressables() {
		return Collections.unmodifiableList(PROGRESSABLES);
	}
}
