package com.github.sanctum.clans.bridge.internal.traits.structure;

import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthList;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DefaultTrait implements Trait {
	ASSASSIN,
	BRUTE,
	NOVICE,
	TAMER
	;

	LabyrinthCollection<Supplier<Ability>> abilities = new LabyrinthList<>();

	@Override
	public @NotNull String getName() {
		switch (this) {
			case ASSASSIN:
				return "Assassin";
			case NOVICE:
				return "Novice";
			case BRUTE:
				return "Brute";
			case TAMER:
				return "Tamer";
			default:
				throw new IllegalStateException("Invalid default trait selected.");
		}
	}

	@Override
	public @NotNull Ability[] getAbilities() {
		LabyrinthCollection<Ability> abilities = new LabyrinthList<>(this.abilities.stream().map(Supplier::get).collect(Collectors.toList()));
		switch (this) {
			case ASSASSIN:
				abilities.add(AbilityPool.PICK_POCKET);
				abilities.add(AbilityPool.SELECTIVE_KILL);
				break;
			case NOVICE:
				break;
			case BRUTE:
				abilities.add(AbilityPool.FULL_STRENGTH);
				break;
			case TAMER:
				abilities.add(AbilityPool.SUMMON_WOLVES);
				break;
		}
		return abilities.stream().toArray(Ability[]::new);
	}

	@Override
	public @Nullable Ability getAbility(@NotNull String name) {
		return Arrays.stream(getAbilities()).filter(a -> a.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@Override
	public void add(@NotNull Ability ability) {
		abilities.add(() -> ability);
	}

	public void add(@NotNull Supplier<Ability> supplier) {
		abilities.add(supplier);
	}

}
