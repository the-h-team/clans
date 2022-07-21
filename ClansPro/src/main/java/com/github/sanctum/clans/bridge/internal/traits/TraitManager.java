package com.github.sanctum.clans.bridge.internal.traits;

import com.github.sanctum.clans.bridge.internal.traits.structure.TraitHolder;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TraitManager {

	static TraitManager instance;
	final PantherMap<UUID, TraitHolder> traits = new PantherEntryMap<>();

	public TraitHolder get(@NotNull OfflinePlayer player) {
		return traits.get(player.getUniqueId());
	}

	public TraitHolder getOrCreate(@NotNull OfflinePlayer player) {
		return traits.computeIfAbsent(player.getUniqueId(), new TraitHolder(player));
	}

	public PantherCollection<TraitHolder> getAll() {
		return traits.values();
	}

	public TraitHolder put(@NotNull UUID id, @NotNull Supplier<TraitHolder> supplier) {
		return traits.put(id, supplier.get());
	}

	public static TraitManager getInstance() {
		return instance != null ? instance : (instance = new TraitManager());
	}
}
