package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.data.container.ImmutableLabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface QnA {

	boolean test(Player player, String question);

	static void register(@NotNull QnA qnA) {
		InoperableSpecialMemory.QNA.add(qnA);
	}

	static @NotNull LabyrinthCollection<QnA> getAll() {
		return ImmutableLabyrinthCollection.of(InoperableSpecialMemory.QNA);
	}

}
