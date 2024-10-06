package com.github.sanctum.clans.impl;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.RankRegistry;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.file.MemorySpace;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultRank implements Clan.Rank {

	private final String name;
	private final String symbol;
	private final List<String> inheritLoad;
	private final int level;
	private final RankRegistry registry;
	PantherCollection<Clan.Rank> inheritance = new PantherSet<>();
	PantherCollection<Clearance> clearances = new PantherSet<>();


	public DefaultRank(@NotNull RankRegistry registry, @NotNull MemorySpace memorySpace, int ordinal) {
		this.registry = registry;
		this.name = memorySpace.getNode("name").toPrimitive().getString();
		this.symbol = memorySpace.getNode("symbol").toPrimitive().getString();
		this.inheritLoad = memorySpace.getNode("inheritance").toPrimitive().getStringList();
		this.level = ordinal;
		memorySpace.getNode("permissions").toPrimitive().getStringList().forEach(s -> {
			Clearance c = Clearance.valueOf(s);
			clearances.add(c);
		});
	}

	public void loadInheritance() {
		inheritLoad.forEach(s -> {
			Clan.Rank position = registry.getRank(s);
			if (position != null) {
				inheritance.add(position);
			} else {
				ClansAPI.getInstance().getPlugin().getLogger().info("- An uknown position named " + '"' + s + '"' + " was found within the inheritance of position " + getName());
			}
		});
	}

	@Override
	public @NotNull String getSymbol() {
		return this.symbol;
	}

	@Override
	public boolean isInheritable() {
		return !this.inheritLoad.isEmpty();
	}

	@Override
	public int getLevel() {
		return this.level;
	}

	@Override
	public Clearance[] getDefaultPermissions() {
		return this.clearances.toArray(Clearance[]::new);
	}

	@Override
	public Clan.Rank[] getInheritance() {
		return this.inheritance.toArray(Clan.Rank[]::new);
	}

	@Override
	public Clan.@Nullable Rank getPromotion() {
		return registry.getRank(getLevel() + 1);
	}

	@Override
	public Clan.@Nullable Rank getDemotion() {
		return registry.getRank(getLevel() - 1);
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Clan.Rank)) return false;
		Clan.Rank p = (Clan.Rank) obj;
		return p.getName().equals(getName()) && p.getLevel() == getLevel() && p.getSymbol().equals(getSymbol());
	}

	@Override
	public String toString() {
		return getName();
	}
}
