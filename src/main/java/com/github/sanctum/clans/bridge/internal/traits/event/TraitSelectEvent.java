package com.github.sanctum.clans.bridge.internal.traits.event;

import com.github.sanctum.clans.bridge.internal.traits.structure.Trait;
import com.github.sanctum.clans.event.ClanEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TraitSelectEvent extends ClanEvent {

	final List<Trait> traits = new ArrayList<>();

	public TraitSelectEvent(Trait... traits) {
		super(false);
		this.traits.addAll(Arrays.asList(traits));
	}

	public void add(@NotNull Trait trait) {
		this.traits.add(trait);
	}

	public List<Trait> getTraits() {
		return traits;
	}
}
