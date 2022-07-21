package com.github.sanctum.clans.bridge.internal.traits.structure;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserTrait implements Trait {

	final String name;
	final PantherCollection<Ability> abilities = new PantherList<>();

	public UserTrait(@NotNull Trait trait) {
		this.name = trait.getName();
		this.abilities.addAll(Arrays.asList(trait.getAbilities()));
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	public @NotNull Ability[] getAbilities() {
		return abilities.stream().toArray(Ability[]::new);
	}

	@Override
	public @Nullable Ability getAbility(@NotNull String name) {
		return abilities.stream().filter(a -> a.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@Override
	public void add(@NotNull Ability ability) {
		Ability t = getAbility(ability.getName());
		if (t != null) abilities.remove(t);
		abilities.add(ability);
	}
}
