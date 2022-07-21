package com.github.sanctum.clans.event.clan;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.ClanEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Called any time the clans are re-loaded.
 */
public class ClansLoadingProcedureEvent extends ClanEvent {

	private final Set<Clan> set = new HashSet<>();

	public ClansLoadingProcedureEvent(@NotNull Collection<Clan> clans) {
		super(null, State.IMMUTABLE, true);
		set.addAll(clans);
	}

	public void insert(Clan clan) {
		set.add(clan);
	}

	public Set<Clan> getClans() {
		return Collections.unmodifiableSet(set);
	}


}
