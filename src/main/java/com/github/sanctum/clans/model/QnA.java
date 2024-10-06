package com.github.sanctum.clans.model;

import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface QnA {

	boolean test(Player player, String question);

	static void register(@NotNull QnA qnA) {
		InoperableSpecialMemory.QNA.add(qnA);
	}

	static @NotNull PantherCollection<QnA> getAll() {
		return ImmutablePantherCollection.of(InoperableSpecialMemory.QNA);
	}

}
