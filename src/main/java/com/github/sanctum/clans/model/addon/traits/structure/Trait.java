package com.github.sanctum.clans.model.addon.traits.structure;

import com.github.sanctum.labyrinth.interfacing.Identifiable;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.util.Deployable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Trait extends Identifiable {

	@NotNull String getName();

	@Note("If this is a custom default trait, make sure this will always return new instances!")
	@NotNull Ability[] getAbilities();

	@Nullable Ability getAbility(@NotNull String name);

	void add(@NotNull Ability ability);

	@Note("Converts this possible default trait into a usable user trait.")
	default @NotNull UserTrait toUserTrait() {
		return new UserTrait(this);
	}

	interface Ability extends Identifiable {

		@NotNull String getName();

		@NotNull String getDescription();

		int getLevel();

		int getMaxLevel();

		void setLevel(int level);

		Deployable<Ability> run(@NotNull Object... args);

		default boolean isMaxLevel() {
			return getLevel() >= getMaxLevel();
		}

	}

}
