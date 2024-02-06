package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.impl.DefaultRank;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.container.PantherSet;
import java.io.InputStream;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankRegistry {

	static RankRegistry instance;
	Clan.Rank highest, lowest;
	final PantherCollection<Clan.Rank> positions = new PantherSet<>();
	final PantherCollection<Clearance> clearances = new PantherSet<>();

	RankRegistry() {
	}

	public void register(@NotNull Clan.Rank position) {
		this.positions.add(position);
	}

	public void unregister(@NotNull Clan.Rank position) {
		this.positions.remove(position);
	}

	public void register(@NotNull Clearance clearance) {
		this.clearances.add(clearance);
	}

	public void unregister(@NotNull Clearance clearance) {
		this.clearances.remove(clearance);
		ClansAPI.getInstance().getClanManager().getClans().forEach(c -> getRanks().forEach(r -> c.getPermissiveHandle().remove(clearance, r)));
	}

	public void clear() {
		this.positions.clear();
		this.clearances.clear();
	}

	public void load() {
		FileManager ranksFile = ClansAPI.getInstance().getFileList().get("Ranks", "Configuration");
		if (!ranksFile.getRoot().exists()) {
			InputStream ranksStream = ClansAPI.getInstance().getPlugin().getResource("config/Ranks.yml");
			if (ranksStream == null) throw new IllegalStateException("Unable to load Ranks.yml from the jar!");
			FileList.copy(ranksStream, ranksFile.getRoot().getParent());
		}
		ranksFile.getRoot().reload();
		ranksFile.read(c -> {
			int ordinal = 0;
			for (String section : c.getKeys(false)) {
				DefaultRank position = new DefaultRank(this, c.getNode(section), ordinal);
				register(position);
				ordinal++;
			}
			return null;
		});
		this.positions.forEach(p -> {
			if (p instanceof DefaultRank) {
				((DefaultRank)p).loadInheritance();
			}
		});
	}

	public void order() {
		this.highest = positions.stream().sorted(Comparator.comparingInt(Clan.Rank::getLevel)).collect(PantherCollectors.toList()).getLast();
		this.lowest = positions.stream().sorted(Comparator.comparingInt(Clan.Rank::getLevel)).collect(PantherCollectors.toList()).getFirst();
	}

	public @Nullable Clan.Rank getRank(@NotNull String name) {
		return positions.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
	}

	public @Nullable Clan.Rank getRank(int ordinal) {
		return positions.stream().filter(p -> p.getLevel() == ordinal).findFirst().orElse(null);
	}

	public @NotNull("The owner rank should never be null!") Clan.Rank getHighest() {
		return this.highest;
	}

	public @NotNull() Clan.Rank getLowest() {
		return this.lowest;
	}

	public PantherCollection<Clan.Rank> getRanks() {
		return positions;
	}

	public PantherCollection<Clearance> getClearances() {
		return clearances;
	}

	public static RankRegistry getInstance() {
		return instance != null ? instance : (instance = new RankRegistry());
	}
}
