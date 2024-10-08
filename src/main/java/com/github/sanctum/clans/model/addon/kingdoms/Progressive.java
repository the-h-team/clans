package com.github.sanctum.clans.model.addon.kingdoms;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.panther.file.MemorySpace;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Progressive implements MemorySpace {

	protected static final List<Progressive> PROGRESSIVES = new LinkedList<>();

	abstract public @NotNull String getName();

	abstract public int getLevel();

	abstract public @NotNull List<Quest> getQuests();

	abstract public @Nullable Quest getQuest(String title);

	abstract public void giveQuest(Quest... quests);

	abstract public void save(Clan.Addon cycle);

	public static List<Progressive> getProgressives() {
		return Collections.unmodifiableList(PROGRESSIVES);
	}

	public static void register(Progressive progressive) {
		PROGRESSIVES.add(progressive);
	}

	public final Kingdom getAsKingdom() {
		return (Kingdom) this;
	}

	public final boolean isKingdom() {
		return this instanceof Kingdom;
	}

}
