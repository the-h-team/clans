package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.internal.kingdoms.impl.LocalFileQuest;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.panther.file.MemorySpace;
import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A memory space dedicated to mission progression/completion and reward metadata bundled with the {@link com.github.sanctum.clans.bridge.internal.KingdomAddon}
 */
public interface Quest extends MemorySpace {

	@NotNull String getTitle();

	@NotNull String getDescription();

	/**
	 * Get the progression tracking parent for this quest, in most cases this is a kingdom but this could return the roundtable aswell.
	 *
	 * @return The parent progressive on this quest object.
	 */
	@Nullable Progressive getParent();

	double getRequirement();

	double getProgression();

	LabyrinthUser getCompleter();

	Reward<?> getReward();

	Set<Player> getActiveUsers();

	double progress(double amount);

	double unprogress(double amount);

	void save();

	void delete();

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
