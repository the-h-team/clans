package com.github.sanctum.clans.bridge.internal.traits;

import com.github.sanctum.clans.bridge.internal.traits.structure.TraitHolder;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthEntryMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthMap;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TraitManager {

	static TraitManager instance;
	final LabyrinthMap<UUID, TraitHolder> traits = new LabyrinthEntryMap<>();

	public TraitHolder get(@NotNull OfflinePlayer player) {
		return traits.get(player.getUniqueId());
	}

	public TraitHolder getOrCreate(@NotNull OfflinePlayer player) {
		return traits.computeIfAbsent(player.getUniqueId(), new TraitHolder(player));
	}

	public LabyrinthCollection<TraitHolder> getAll() {
		return traits.values();
	}

	public TraitHolder put(@NotNull UUID id, @NotNull Supplier<TraitHolder> supplier) {
		return traits.put(id, supplier.get());
	}

	public static TraitManager getInstance() {
		return instance != null ? instance : (instance = new TraitManager());
	}
}
