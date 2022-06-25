package com.github.sanctum.clans.bridge.internal.traits.structure;

import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.Deployable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Trait extends Nameable {

	@NotNull String getName();

	@Note("If this is a custom default trait, make sure this will always return new instances!")
	@NotNull Ability[] getAbilities();

	@Nullable Ability getAbility(@NotNull String name);

	void add(@NotNull Ability ability);

	@Note("Converts this possible default trait into a usable user trait.")
	default @NotNull UserTrait toUserTrait() {
		return new UserTrait(this);
	}

	interface Ability extends Nameable {

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
